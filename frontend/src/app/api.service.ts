import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AlertDto, FermDto } from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api';

  getFerms(): Observable<FermDto[]> {
    return this.http.get<FermDto[]>(`${this.baseUrl}/ferms`);
  }

  startFerm(id: number): Observable<FermDto> {
    return this.http.post<FermDto>(`${this.baseUrl}/ferms/${id}/start`, {});
  }

  stopSubTimer(id: number, duration: number): Observable<FermDto> {
    return this.http.post<FermDto>(`${this.baseUrl}/ferms/${id}/sub-timers/${duration}/stop`, {});
  }

  getAlerts(): Observable<AlertDto[]> {
    return this.http.get<AlertDto[]>(`${this.baseUrl}/alerts`);
  }

  acknowledgeAlert(id: string): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/alerts/${id}/ack`, {});
  }
}
