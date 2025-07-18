import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { Message, MessageService, Toasts } from './toasts';

describe('Toast Component and Service', () => {
  describe('MessageService', () => {
    let service: MessageService;

    beforeEach(() => {
      TestBed.configureTestingModule({
        imports: [BrowserAnimationsModule],
        providers: [MessageService],
      });
      service = TestBed.inject(MessageService);
    });

    it('should create the service', () => {
      expect(service).toBeTruthy();
    });

    it('should show toast message with default options', () => {
      const id = service.show('Test message');
      expect(id).toBeTruthy();
      expect(typeof id).toBe('string');
    });

    it('should create toast messages with different types', () => {
      expect(service.success('Success')).toBeTruthy();
      expect(service.error('Error')).toBeTruthy();
      expect(service.warning('Warning')).toBeTruthy();
      expect(service.info('Info')).toBeTruthy();
    });

    it('should create toast with custom options', () => {
      const options = {
        animation: false,
        autohide: false,
        delay: 3000,
      };
      const id = service.show('Custom toast', 'info', options);
      expect(id).toBeTruthy();
    });
  });

  describe('Toasts Component', () => {
    let component: Toasts;
    let fixture: ComponentFixture<Toasts>;

    beforeEach(async () => {
      await TestBed.configureTestingModule({
        imports: [Toasts, BrowserAnimationsModule],
      }).compileComponents();

      fixture = TestBed.createComponent(Toasts);
      component = fixture.componentInstance;
      fixture.detectChanges();
    });

    it('should create', () => {
      expect(component).toBeTruthy();
    });

    it('should add toast message', () => {
      const toast: Message = {
        id: '123',
        message: 'Test Message',
        type: 'success',
        animation: true,
        autohide: true,
        delay: 5000,
      };

      component.add(toast);
      expect(component.msgs().length).toBe(1);
      expect(component.msgs()[0]).toEqual(toast);
    });

    it('should remove toast message', () => {
      const toast: Message = {
        id: '123',
        message: 'Test Message',
        type: 'success',
      };

      component.add(toast);
      expect(component.msgs().length).toBe(1);

      component.remove('123');
      expect(component.msgs().length).toBe(0);
    });

    it('should emit toastsDropped when last toast is removed', () => {
      let dropped = false;
      component.toastsDropped.subscribe(() => (dropped = true));

      const message: Message = {
        id: '123',
        message: 'Test',
        type: 'info',
      };

      component.add(message);
      component.remove('123');
      expect(dropped).toBeTrue();
    });

    it('should clear messages on destroy', () => {
      component.add({
        id: '123',
        message: 'Test',
        type: 'info',
      });

      expect(component.msgs().length).toBe(1);
      component.ngOnDestroy();
      expect(component.msgs().length).toBe(0);
    });
  });
});
