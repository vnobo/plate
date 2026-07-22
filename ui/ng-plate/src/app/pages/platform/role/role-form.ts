import {
  Component,
  computed,
  DestroyRef,
  effect,
  inject,
  input,
  linkedSignal,
  signal,
} from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { FormField, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';
import { Group, ROOT_PCODE } from './role.types';
import { environment } from '@envs/env';

@Component({
  selector: 'app-role-form',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <form (ngSubmit)="onSubmit()" class="form-wrapper">
        <div class="mb-3">
          <label class="form-label" for="name">角色名称 *</label>
          <input class="form-control" type="text" id="name" [formField]="roleForm.name" />
        </div>
        <div class="mb-3">
          <label class="form-label" for="pcode">父级角色</label>
          <select class="form-select" id="pcode" [formField]="roleForm.pcode">
            <option [value]="ROOT_PCODE">根角色（顶级）</option>
            @for (p of parentOptions(); track p.code) {
              <option [value]="p.code">{{ p.name }}</option>
            }
          </select>
        </div>
        <div class="mb-3">
          <label class="form-label" for="description">描述</label>
          <textarea class="form-control" type="text" id="description" rows="3" [formField]="roleForm.description"></textarea>
        </div>
        <div class="d-flex">
          <button class="btn btn-primary ms-auto" type="submit" [disabled]="!roleForm().valid() || isSubmitting()">
            保存角色
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
export class RoleForm {
  inputData = input<Group | null>(null);
  allGroups = input<Group[]>([]);

  private readonly userData = linkedSignal(this.inputData);
  readonly created = computed(() => this.userData()?.code == undefined);

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly ROOT_PCODE = ROOT_PCODE;

  isSubmitting = signal(false);

  private readonly initialModel = {
    id: undefined as number | undefined,
    code: '',
    pcode: ROOT_PCODE,
    name: '',
    description: '',
  };

  protected readonly roleModel = signal({ ...this.initialModel });

  protected readonly roleForm = form(this.roleModel, (p) => {
    required(p.name, { message: '角色名称 是必填项' });
    required(p.pcode, { message: '父级角色 是必填项' });
  });

  /** 可选父级 = 除自身及其后代之外的所有角色（避免循环） */
  parentOptions = computed(() => {
    const data = this.userData();
    const selfCode = data?.code;
    const excluded = selfCode ? this.descendantsOf(selfCode) : new Set<string>();
    return this.allGroups().filter((g) => g.code && !excluded.has(g.code));
  });

  constructor() {
    effect(() => {
      const data = this.userData();
      if (this.created()) {
        this.roleModel.set({
          ...this.initialModel,
          pcode: data?.pcode ?? ROOT_PCODE,
        });
      } else if (data) {
        this.roleModel.set({
          ...this.initialModel,
          id: data.id,
          code: data.code ?? '',
          pcode: data.pcode ?? ROOT_PCODE,
          name: data.name ?? '',
          description: data.description ?? '',
        });
      }
    });
  }

  private descendantsOf(code: string): Set<string> {
    const all = this.allGroups();
    const result = new Set<string>([code]);
    let changed = true;
    while (changed) {
      changed = false;
      for (const g of all) {
        const pc = g.pcode;
        if (pc && result.has(pc) && g.code && !result.has(g.code)) {
          result.add(g.code);
          changed = true;
        }
      }
    }
    return result;
  }

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.roleForm, {
      action: async () => {
        const model = this.roleModel();
        const result: Group = {
          name: model.name,
          description: model.description,
          pcode: model.pcode,
        };
        if (!this.created()) {
          result.id = model.id;
          result.code = model.code;
        }
        this._http
          .post<Group>(environment.secApiPath + '/groups/save', result)
          .pipe(
            tap(() => this._message.success(this.created() ? '角色创建成功' : '角色更新成功')),
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
