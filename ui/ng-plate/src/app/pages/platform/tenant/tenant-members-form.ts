import { Component, computed, DestroyRef, inject, input, signal } from '@angular/core';
import { HttpClient, httpResource } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { TenantMember } from './tenant.types';
import { User } from '../users/user.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-tenant-members',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <div class="mb-3">
        @if (membersResource.isLoading()) {
          <div class="text-center py-3">
            <div class="spinner-border spinner-border-sm text-muted" role="status">
              <span class="visually-hidden">加载中...</span>
            </div>
            <span class="ms-2 text-muted">加载成员列表中...</span>
          </div>
        } @else if (memberRows().length === 0) {
          <div class="text-muted text-center py-3">该租户尚未添加成员</div>
        } @else {
          <div class="list-group list-group-flush">
            @for (row of memberRows(); track row.member.code) {
              <div class="list-group-item d-flex align-items-center px-0">
                <span class="avatar bg-primary-lt me-2">
                  {{ (row.displayName || '?').slice(0, 1) }}
                </span>
                <div class="flex-fill min-w-0">
                  <div class="fw-5 text-truncate">{{ row.displayName }}</div>
                  <div class="text-secondary small text-truncate">
                    @if (row.user) {
                      {{ row.user.username }}
                    } @else {
                      用户编码：{{ row.member.userCode }}
                    }
                  </div>
                </div>
                @if (row.member.enabled === false) {
                  <span class="badge bg-secondary-lt me-2">已停用</span>
                }
                <button
                  type="button"
                  class="btn btn-ghost-danger btn-sm"
                  (click)="onRemove(row.member)"
                  [attr.aria-label]="'移除成员 ' + row.displayName"
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
          <label class="form-label" for="userCode">选择用户 *</label>
          @if (usersResource.isLoading()) {
            <div class="text-muted small">加载用户列表中...</div>
          } @else if (candidates().length === 0) {
            <div class="text-muted small">所有用户均已是该租户成员</div>
          }
          <select class="form-select" id="userCode" [formField]="memberForm.userCode">
            <option value="">— 请选择 —</option>
            @for (u of candidates(); track u.code) {
              <option [value]="u.code">{{ u.name || u.username }} ({{ u.username }})</option>
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
export class TenantMembersForm {
  /** Code of the owning tenant. */
  tenantCode = input.required<string>();
  /** Display name of the owning tenant, used in messages. */
  tenantName = input('');

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);

  protected readonly membersResource = httpResource<TenantMember[]>(
    () => ({
      url: environment.secApiPath + '/tenants/members/search',
      params: { tenantCode: this.tenantCode(), size: 1000 },
    }),
    { defaultValue: [], debugName: 'tenant-members' },
  );

  /** Candidate users, also used to resolve member display names. */
  protected readonly usersResource = httpResource<User[]>(
    () => ({
      url: environment.secApiPath + '/users/search',
      params: { size: 1000 },
    }),
    { defaultValue: [], debugName: 'tenant-member-users' },
  );

  protected readonly members = computed(() => this.membersResource.value() ?? []);
  protected readonly users = computed(() => this.usersResource.value() ?? []);

  protected readonly userMap = computed(() => {
    const map = new Map<string, User>();
    for (const u of this.users()) {
      if (u.code) {
        map.set(u.code, u);
      }
    }
    return map;
  });

  /** Members enriched with a display name resolved from the user list. */
  protected readonly memberRows = computed(() =>
    this.members().map((member) => {
      const user = member.userCode ? this.userMap().get(member.userCode) : undefined;
      return {
        member,
        user,
        displayName: user?.name || user?.username || member.userCode || '?',
      };
    }),
  );

  /** Users that are not yet members of this tenant. */
  protected readonly candidates = computed(() => {
    const memberCodes = new Set(
      this.members()
        .map((m) => m.userCode)
        .filter((c): c is string => !!c),
    );
    return this.users().filter((u) => u.code && !memberCodes.has(u.code));
  });

  private readonly initialModel = { userCode: '' };
  protected readonly model = signal({ ...this.initialModel });

  protected readonly memberForm = form(this.model, (p) => {
    required(p.userCode, { message: '请选择用户' });
  });

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.memberForm, {
      action: async () => {
        const payload: TenantMember = {
          tenantCode: this.tenantCode(),
          userCode: this.model().userCode,
          enabled: true,
        };
        this._http
          .post<TenantMember>(environment.secApiPath + '/tenants/members/save', payload)
          .pipe(
            tap(() =>
              this._message.success(`已将用户 ${this.userName(this.model().userCode)} 加入租户`),
            ),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => {
            this.model.set({ ...this.initialModel });
            this.membersResource.reload();
          });
      },
    });
    this.isSubmitting.set(false);
  }

  protected onRemove(member: TenantMember): void {
    if (member.id == null) {
      return;
    }
    const name = member.userCode
      ? this.userMap().get(member.userCode)?.name ?? member.userCode
      : '该成员';
    if (!confirm(`确定将「${name}」移出租户吗？`)) {
      return;
    }
    this._http
      .delete<void>(environment.secApiPath + '/tenants/members/delete', {
        body: { id: member.id, code: member.code },
      })
      .pipe(
        tap(() => this._message.success('成员已移除')),
        delay(600),
        takeUntilDestroyed(this._destroyRef),
      )
      .subscribe(() => this.membersResource.reload());
  }

  private userName(code: string): string {
    const user = this.userMap().get(code);
    return user?.name || user?.username || code;
  }
}
