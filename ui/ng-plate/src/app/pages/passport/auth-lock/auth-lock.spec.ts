import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AuthLock } from './auth-lock';

describe('AuthLock', () => {
  let component: AuthLock;
  let fixture: ComponentFixture<AuthLock>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AuthLock]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AuthLock);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
