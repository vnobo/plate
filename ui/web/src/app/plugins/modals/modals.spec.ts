import {ComponentFixture, TestBed} from '@angular/core/testing';

import {Modals} from './modals';

describe('Modals', () => {
  let component: Modals;
  let fixture: ComponentFixture<Modals>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Modals]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Modals);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
