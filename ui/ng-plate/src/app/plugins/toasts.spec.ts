import { TestBed } from '@angular/core/testing';
import { MessageService } from './toasts';

// show() 走的是服务方法，没有注入上下文：裸调 takeUntilDestroyed() 会抛 NG0203
describe('MessageService', () => {
  it('shows a toast from a non-injection context', () => {
    const svc = TestBed.inject(MessageService);
    let id = '';
    expect(() => (id = svc.error('boom'))).not.toThrow();
    expect(id).toBeTruthy();
  });
});
