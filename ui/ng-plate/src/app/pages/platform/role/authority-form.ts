import { Component, DestroyRef, computed, effect, inject, input, linkedSignal, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { outputToObservable, takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { GroupAuthority } from './role.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-authority-form',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <form (ngSubmit)="onSubmit()" class="form-wrapper">
        <div class="mb-3">
          <label class="form-label" for="authority">权限标识 *</label>
          <input class="form-control" type="text" id="authority" [formField]="authorityForm.authority" />
          <div class="form-hint">
            例如 <code>USER_VIEW</code>、<code>ROLE_EDIT</code>，用于标识该角色拥有的操作权限。
          </div>
        </div>
        <div class="d-flex">
          <button
            class="btn btn-primary ms-auto"
            type="submit"
            [disabled]="!authorityForm().valid() || isSubmitting()"
          >
            保存权限
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
export class AuthorityForm {
  /** 当前所属角色编码 */
  groupCode = input.required<string>();
  /** 编辑时传入已有权限，新建时为 null */
  inputData = input<GroupAuthority | null>(null);

  private readonly data = linkedSignal(this.inputData);
  readonly created = computed(() => this.data()?.code == undefined);

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  isSubmitting = signal(false);

  private readonly initialModel = {
    id: undefined as number | undefined,
    code: '',
    authority: '',
  };

  protected readonly model = signal({ ...this.initialModel });

  protected readonly authorityForm = form(this.model, (p) => {
    required(p.authority, { message: '权限标识 是必填项' });
  });

  constructor() {
    effect(() => {
      const d = this.data();
      if (this.created()) {
        this.model.set({ ...this.initialModel });
      } else if (d) {
        this.model.set({
          ...this.initialModel,
          id: d.id,
          code: d.code ?? '',
          authority: d.authority ?? '',
        });
      }
    });
  }

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.authorityForm, {
      action: async () => {
        const m = this.model();
        const payload: GroupAuthority = {
          authority: m.authority,
          groupCode: this.groupCode(),
        };
        if (!this.created()) {
          payload.id = m.id;
          payload.code = m.code;
        }
        this._http
          .post<GroupAuthority>(environment.secApiPath + '/groups/authorities/save', payload)
          .pipe(
            tap(() => this._message.success(this.created() ? '权限已添加' : '权限已更新')),
            delay(800),
            takeUntilDestroyed(this._destroyRef),
          )
          .subscribe(() => this.closeModal());
      },
    });
    this.isSubmitting.set(false);
  }

  private closeModal() {
    const el = document.getElementById('exampleModal');
    if (el) {
      tabler?.Modal?.getOrCreateInstance(el)?.hide();
    }
  }
}
