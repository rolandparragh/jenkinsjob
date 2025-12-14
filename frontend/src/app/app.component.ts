import { CommonModule } from '@angular/common';
import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subject, finalize, interval, takeUntil } from 'rxjs';
import { ApiService } from './api.service';
import { AlertDto, FermDto, TimerDto } from './models';
import { NotificationBridge } from './notification.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './app.component.html',
  styleUrl: './app.component.scss'
})
export class AppComponent implements OnInit, OnDestroy {
  title = 'Ferm Timer Board';
  ferms: FermDto[] = [];
  alerts: AlertDto[] = [];
  now = Date.now();
  warningThreshold = 5 * 60;
  breachThreshold = 15 * 60;
  startLoading = new Set<number>();
  stopLoading = new Set<string>();
  resetLoading = new Set<number>();
  errorMessage?: string;
  infoMessage?: string;
  notificationsEnabled = typeof Notification !== 'undefined' && Notification.permission === 'granted';
  private destroy$ = new Subject<void>();
  private readonly seenAlerts = new Set<string>();

  constructor(private readonly api: ApiService, private readonly notifier: NotificationBridge) {}

  ngOnInit(): void {
    this.loadFerms();
    this.loadAlerts();
    interval(1000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => (this.now = Date.now()));
    interval(15000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadFerms(false));
    interval(30000)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadAlerts());
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  loadFerms(showToast = true): void {
    this.api
      .getFerms()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: data => {
          this.ferms = data;
          if (showToast) {
            this.infoMessage = 'Board refreshed';
            setTimeout(() => (this.infoMessage = undefined), 2000);
          }
          this.errorMessage = undefined;
        },
        error: err => {
          this.errorMessage = err?.error?.message ?? 'Unable to load Ferm data.';
        }
      });
  }

  async enableNotifications(): Promise<void> {
    const permission = await this.notifier.requestPermission();
    this.notificationsEnabled = permission === 'granted';
  }

  loadAlerts(): void {
    this.api
      .getAlerts()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: alerts => {
          this.alerts = alerts;
          alerts.forEach(alert => {
            if (!this.seenAlerts.has(alert.id)) {
              this.seenAlerts.add(alert.id);
              if (this.notificationsEnabled) {
                this.notifier.push(alert);
              }
            }
          });
        }
      });
  }

  acknowledgeAlert(alert: AlertDto): void {
    this.api
      .acknowledgeAlert(alert.id)
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => this.loadAlerts());
  }

  startFerm(ferm: FermDto): void {
    if (this.startLoading.has(ferm.id)) {
      return;
    }
    this.startLoading.add(ferm.id);
    this.api
      .startFerm(ferm.id)
      .pipe(takeUntil(this.destroy$), finalize(() => this.startLoading.delete(ferm.id)))
      .subscribe({
        next: updated => {
          this.mergeFerm(updated);
        },
        error: err => {
          this.errorMessage = err?.error?.message ?? 'Failed to start timer.';
        }
      });
  }

  stopSubTimer(ferm: FermDto, timer: TimerDto): void {
    const key = `${ferm.id}-${timer.durationHours}`;
    if (this.stopLoading.has(key) || timer.stopped || !timer.startedAt) {
      return;
    }
    this.stopLoading.add(key);
    this.api
      .stopSubTimer(ferm.id, timer.durationHours)
      .pipe(takeUntil(this.destroy$), finalize(() => this.stopLoading.delete(key)))
      .subscribe({
        next: updated => {
          this.mergeFerm(updated);
        },
        error: err => {
          this.errorMessage = err?.error?.message ?? 'Failed to stop timer.';
        }
      });
  }

  resetAllTimers(ferm: FermDto): void {
    if (this.resetLoading.has(ferm.id) || !ferm.mainTimer.startedAt) {
      return;
    }
    this.resetLoading.add(ferm.id);
    this.api
      .resetAllTimers(ferm.id)
      .pipe(takeUntil(this.destroy$), finalize(() => this.resetLoading.delete(ferm.id)))
      .subscribe({
        next: updated => {
          this.mergeFerm(updated);
          this.errorMessage = undefined;
        },
        error: err => {
          const errorMsg = err?.error?.message || err?.message || 'Failed to reset timers.';
          this.errorMessage = errorMsg;
          console.error('Reset error:', err);
        }
      });
  }

  canStart(ferm: FermDto): boolean {
    return !ferm.mainTimer.running && !this.startLoading.has(ferm.id);
  }

  isSubStopDisabled(ferm: FermDto, timer: TimerDto): boolean {
    return !ferm.mainTimer.startedAt || timer.stopped || this.stopLoading.has(`${ferm.id}-${timer.durationHours}`);
  }

  canReset(ferm: FermDto): boolean {
    return !!ferm.mainTimer.startedAt && !this.resetLoading.has(ferm.id);
  }

  tileClass(timer: TimerDto, isSubTimer: boolean = false): string {
    if (!timer.startedAt || timer.stopped) {
      return 'gray';
    }
    const pastZero = this.secondsPastZero(timer);
    // For subtimers, turn red immediately when past zero
    if (isSubTimer && pastZero > 0) {
      return 'red';
    }
    // For main timer, use breach threshold
    if (pastZero >= this.breachThreshold) {
      return 'red';
    }
    const remaining = this.secondsRemaining(timer);
    if (remaining <= this.warningThreshold) {
      return 'yellow';
    }
    return 'green';
  }

  secondsRemaining(timer: TimerDto): number {
    if (!timer.startedAt || !timer.targetAt) {
      return timer.durationSeconds;
    }
    if (timer.stopped && timer.stoppedAt) {
      const target = new Date(timer.targetAt).getTime();
      const stoppedAt = new Date(timer.stoppedAt).getTime();
      return Math.max(0, Math.round((target - stoppedAt) / 1000));
    }
    return Math.max(0, Math.round((new Date(timer.targetAt).getTime() - this.now) / 1000));
  }

  secondsPastZero(timer: TimerDto): number {
    if (!timer.startedAt || !timer.targetAt) {
      return 0;
    }
    const reference = timer.stopped && timer.stoppedAt ? new Date(timer.stoppedAt).getTime() : this.now;
    const past = Math.round((reference - new Date(timer.targetAt).getTime()) / 1000);
    return Math.max(0, past);
  }

  formatCountdown(timer: TimerDto, isSubTimer: boolean = false): string {
    // For subtimers, when past zero, show the count-up time with "+" prefix instead of 0
    if (isSubTimer) {
      const pastZero = this.secondsPastZero(timer);
      if (pastZero > 0) {
        return '+' + this.formatDuration(pastZero);
      }
    }
    const remaining = this.secondsRemaining(timer);
    return this.formatDuration(remaining);
  }

  formatOverdue(timer: TimerDto): string {
    const past = this.secondsPastZero(timer);
    return past > 0 ? this.formatDuration(past) : '';
  }

  private formatDuration(totalSeconds: number): string {
    const seconds = Math.floor(totalSeconds % 60);
    const minutes = Math.floor((totalSeconds / 60) % 60);
    const hours = Math.floor((totalSeconds / 3600) % 24);
    const totalHours = Math.floor(totalSeconds / 3600);
    const days = Math.floor(totalSeconds / 86400);
    const hh = (days > 0 ? hours : totalHours).toString().padStart(2, '0');
    const mm = minutes.toString().padStart(2, '0');
    const ss = seconds.toString().padStart(2, '0');
    return days > 0 ? `${days}d ${hh}:${mm}:${ss}` : `${hh}:${mm}:${ss}`;
  }

  private mergeFerm(updated: FermDto): void {
    const exists = this.ferms.some(ferm => ferm.id === updated.id);
    this.ferms = exists
      ? this.ferms.map(ferm => (ferm.id === updated.id ? updated : ferm))
      : [...this.ferms, updated];
  }
}
