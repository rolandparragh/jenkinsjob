import { TestBed } from '@angular/core/testing';
import { of } from 'rxjs';
import { AppComponent } from './app.component';
import { ApiService } from './api.service';
import { NotificationBridge } from './notification.service';

describe('AppComponent', () => {
  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AppComponent],
      providers: [
        {
          provide: ApiService,
          useValue: {
            getFerms: () => of([]),
            getAlerts: () => of([]),
            startFerm: () => of(),
            stopSubTimer: () => of(),
            acknowledgeAlert: () => of(void 0)
          }
        },
        {
          provide: NotificationBridge,
          useValue: {
            requestPermission: () => Promise.resolve('denied' as NotificationPermission),
            push: () => {}
          }
        }
      ]
    }).compileComponents();
  });

  it('should create the app', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app).toBeTruthy();
  });

  it('should have the Ferm timer title', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    expect(app.title).toEqual('Ferm Timer Board');
  });

  it('should render header text', () => {
    const fixture = TestBed.createComponent(AppComponent);
    fixture.detectChanges();
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Ferm Timer Board');
  });
});
