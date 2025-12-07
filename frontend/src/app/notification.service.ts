import { Injectable } from '@angular/core';
import { AlertDto } from './models';

@Injectable({ providedIn: 'root' })
export class NotificationBridge {
  permission: NotificationPermission = typeof Notification !== 'undefined' ? Notification.permission : 'default';

  async requestPermission(): Promise<NotificationPermission> {
    if (typeof Notification === 'undefined') {
      this.permission = 'denied';
      return this.permission;
    }
    if (this.permission === 'default') {
      this.permission = await Notification.requestPermission();
    }
    return this.permission;
  }

  push(alert: AlertDto): void {
    if (typeof Notification === 'undefined' || this.permission !== 'granted') {
      return;
    }
    new Notification(`${alert.fermName} ${alert.timerLabel}`, {
      body: alert.message,
      tag: alert.id,
      silent: false
    });
  }
}
