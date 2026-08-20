import { Component, computed, DestroyRef, inject, input, signal } from '@angular/core';
import { HttpClient, httpResource } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { UserAuthority } from './user.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-user-authority',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <div class="mb-3">
        @if (authoritiesResource.isLoading()) {
          <div class="text-center py-3">
            <div class="spinner-border spinner-border-sm text-muted" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <span class="ms-2 text-muted">加载权限列表中...</span>
          </div>
        } @else if (authorities().length === 0) {
          <div class="text-muted text-center py-3">该用户尚未分配直接权限</div>
        } @else {
          <div class="list-group list-group-flush">
            @for (item of authorities(); track item.code) {
              <div class="list-group-item d-flex align-items-center px-0">
                <code class="flex-fill">{{ item.authority }}</code>
                <button
                  type="button"
                  class="btn btn-ghost-danger btn-sm"
                  (click)="onRemove(item)"
                  [attr.aria-label]="'移除权限 ' + item.authority"
                >
                  移除
                </button>
              </div>
            }
          </div>
        }
      </div>

      <form (ngSubmit)="onSubmit()" class="form-wrapper border-top pt-3">
        <div class="mb-3">
          <label class="form-label" for="authority">权限标识 *</label>
          <input
            class="form-control"
            type="text"
            id="authority"
            autocomplete="off"
            placeholder="例如 USER_VIEW"
            [formField]="authorityForm.authority"
          />
          <div class="form-hint">为用户直接分配的权限标识，与角色权限合并生效。</div>
        </div>
        <div class="d-flex">
          <button
            class="btn btn-primary ms-auto"
            type="submit"
            [disabled]="!authorityForm().valid() || isSubmitting()"
          >
            添加权限
          </button>
        </div>
      </form>
    </div>
  `,
  styles: [
    `
      :host {
        min-height: 100%;
        min-width: 100%;
      }
    `,
  ],
})
export class UserAuthorityForm {
  /** Code of the owning user. */
  userCode = input.required<string>();
  /** Display name of the owning user, used in messages. */
  userName = input('');

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);

  protected readonly authoritiesResource = httpResource<UserAuthority[]>(
    () => ({
      url: environment.secApiPath + '/users/authorities/search',
      params: { userCode: this.userCode(), size: 1000 },
    }),
    { defaultValue: [], debugName: 'user-authorities' },
  );

  protected readonly authorities = computed(() => this.authoritiesResource.value() ?? []);

  private readonly initialModel = { authority: '' };
  protected readonly model = signal({ ...this.initialModel });

  protected readonly authorityForm = form(this.model, (p) => {
    required(p.authority, { message: '权限标识 是必填项' });
  });

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.authorityForm, {
      action: async () => {
        const payload: UserAuthority = {
          userCode: this.userCode(),
          authority: this.model().authority.trim(),
        };
        this._http
          .post<UserAuthority>(environment.secApiPath + '/users/authorities/save', payload)
          .pipe(
            tap(() => this._message.success('权限已添加')),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => {
            this.model.set({ ...this.initialModel });
            this.authoritiesResource.reload();
          });
      },
    });
    this.isSubmitting.set(false);
  }

  protected onRemove(item: UserAuthority): void {
    if (item.id == null) {
      return;
    }
    if (!confirm(`确定移除权限「${item.authority ?? ''}」吗？`)) {
      return;
    }
    this._http
      .delete<void>(environment.secApiPath + '/users/authorities/delete', {
        body: { id: item.id, code: item.code },
      })
      .pipe(
        tap(() => this._message.success('权限已移除')),
        delay(600),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => this.authoritiesResource.reload());
  }
}
