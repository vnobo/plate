import { Component, computed, DestroyRef, inject, input, signal } from '@angular/core';
import { HttpClient, httpResource } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService, ModalsService } from '@app/plugins';
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
              <span class="visually-hidden">Loading...</span>
            </div>
            <span class="ms-2 text-muted">Loading authorities...</span>
          </div>
        } @else if (authorities().length === 0) {
          <div class="text-muted text-center py-3">This user has no directly assigned authorities yet</div>
        } @else {
          <div class="list-group list-group-flush">
            @for (item of authorities(); track item.code) {
              <div class="list-group-item d-flex align-items-center px-0">
                <code class="flex-fill">{{ item.authority }}</code>
                <button
                  type="button"
                  class="btn btn-ghost-danger btn-sm"
                  (click)="onRemove(item)"
                  [attr.aria-label]="'Remove authority ' + item.authority"
                >
                  Remove
                </button>
              </div>
            }
          </div>
        }
      </div>

      <form (ngSubmit)="onSubmit()" class="form-wrapper border-top pt-3">
        <div class="mb-3">
          <label class="form-label" for="authority">Authority Code *</label>
          <input
            class="form-control"
            type="text"
            id="authority"
            autocomplete="off"
            placeholder="e.g. USER_VIEW"
            [formField]="authorityForm.authority"
          />
          <div class="form-hint">Authority code directly assigned to the user, merged with role authorities.</div>
        </div>
        <div class="d-flex">
          <button
            class="btn btn-primary ms-auto"
            type="submit"
            [disabled]="!authorityForm().valid() || isSubmitting()"
          >
            Add Authority
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
  private readonly _modal = inject(ModalsService);
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
    required(p.authority, { message: 'Authority code is required' });
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
            tap(() => this._message.success('Authority added')),
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
    this._modal
      .confirm({
        title: 'Remove Authority',
        message: `Are you sure you want to remove authority "${item.authority ?? ''}"?`,
        confirmText: 'Remove',
        danger: true,
      })
      .then((ok) => {
        if (!ok) {
          return;
        }
        this._http
          .delete<void>(environment.secApiPath + '/users/authorities/delete', {
            body: { id: item.id, code: item.code },
          })
          .pipe(
            tap(() => this._message.success('Authority removed')),
            delay(600),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => this.authoritiesResource.reload());
      });
  }
}
