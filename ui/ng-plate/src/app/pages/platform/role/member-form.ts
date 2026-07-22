import { Component, DestroyRef, computed, inject, input, signal } from '@angular/core';
import { HttpClient, httpResource } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { GroupMember } from './role.types';
import { User } from '../../dashboard/users/user.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-member-form',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <form (ngSubmit)="onSubmit()" class="form-wrapper">
        @if (usersResource.isLoading()) {
          <div class="text-center py-2 text-muted">加载用户列表中...</div>
        }
        <div class="mb-3">
          <label class="form-label" for="userCode">选择用户 *</label>
          <select class="form-select" id="userCode" [formField]="memberForm.userCode">
            <option value="">— 请选择 —</option>
            @for (u of users(); track u.code) {
              <option [value]="u.code">
                {{ u.name || u.username }}<ng-container> ({{ u.username }})</ng-container>
              </option>
            }
          </select>
        </div>
        <div class="d-flex">
          <button
            class="btn btn-primary ms-auto"
            type="submit"
            [disabled]="!memberForm().valid() || isSubmitting()"
          >
            添加成员
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
export class MemberForm {
  /** Code of the owning role */
  groupCode = input.required<string>();

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);

  /** Candidate users (for the dropdown) */
  protected readonly usersResource = httpResource<User[]>(
    () => ({
      url: environment.secApiPath + '/users/search',
      params: { size: 1000 },
    }),
    { defaultValue: [], debugName: 'member-users' },
  );
  protected readonly users = computed(() => this.usersResource.value() ?? []);

  private readonly initialModel = { userCode: '' };
  protected readonly model = signal({ ...this.initialModel });

  protected readonly memberForm = form(this.model, (p) => {
    required(p.userCode, { message: '请选择用户' });
  });

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.memberForm, {
      action: async () => {
        const payload: GroupMember = {
          groupCode: this.groupCode(),
          userCode: this.model().userCode,
        };
        this._http
          .post<GroupMember>(environment.secApiPath + '/groups/members/save', payload)
          .pipe(
            tap(() =>
              this._message.success(
                `已将用户 ${this.userName(this.model().userCode)} 加入该角色`,
              ),
            ),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => this.closeModal());
      },
    });
    this.isSubmitting.set(false);
  }

  private userName(code: string): string {
    return this.users().find((u) => u.code === code)?.name ?? code;
  }

  private closeModal() {
    const el = document.getElementById('exampleModal');
    if (el) {
      tabler?.Modal?.getOrCreateInstance(el)?.hide();
    }
  }
}
