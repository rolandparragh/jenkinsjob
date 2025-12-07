export type TimerColor = 'GREEN' | 'YELLOW' | 'RED' | 'GRAY';

export interface TimerDto {
  label: string;
  durationHours: number;
  durationSeconds: number;
  remainingSeconds: number;
  secondsPastZero: number;
  color: TimerColor;
  running: boolean;
  stopped: boolean;
  breachActive: boolean;
  startedAt?: string;
  targetAt?: string;
  stoppedAt?: string;
}

export interface FermDto {
  id: number;
  name: string;
  mainTimer: TimerDto;
  subTimers: TimerDto[];
  running: boolean;
}

export interface AlertDto {
  id: string;
  fermId: number;
  fermName: string;
  timerLabel: string;
  severity: string;
  message: string;
  createdAt: string;
}
