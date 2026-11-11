import { HttpClient } from '@angular/common/http';
import { Component, DestroyRef, computed, effect, inject, input, linkedSignal, signal } from '@angular/core';
import { FormField, disabled, form, required, submit } from '@angular/forms/signals';
import { MessageService } from '@app/plugins';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { delay, tap } from 'rxjs';

import { environment } from '@envs/env';
import { MenuItem, MenuType, MENU_TYPES, ROOT_PCODE } from './menus.types';

@Component({
  selector: 'app-menu-form',
  imports: [FormField],
  template: `
    <div class="container-fluid">
      <!-- 用原生 submit 并阻止默认提交：本组件不导入 FormsModule，(ngSubmit) 不会生效，
           一旦放行原生提交，表单字段会被拼进 URL -->
      <form novalidate (submit)="$event.preventDefault(); onSubmit()" class="form-wrapper">
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="name">Menu Name *</label>
            <input class="form-control" type="text" id="name" [formField]="menuForm.name" />
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="authority">Authority Code *</label>
            <input
              class="form-control"
              type="text"
              id="authority"
              autocomplete="off"
              [formField]="menuForm.authority"
            />
            <div class="form-hint">
              Use only letters and underscores; the ROLE_ prefix is auto-completed server-side, e.g. menuSystemX → ROLE_MENU_SYSTEM_X
            </div>
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="pcode">Parent Menu</label>
            <select class="form-select" id="pcode" [formField]="menuForm.pcode">
              <option [value]="ROOT_PCODE">Top-level menu (no parent)</option>
              @for (p of parentOptions(); track p.code) {
                <option [value]="p.code">{{ p.name }}</option>
              }
            </select>
          </div>
          <div class="col-lg-6">
            <label class="form-label" for="type">Menu Type</label>
            <select class="form-select" id="type" [formField]="menuForm.type">
              @for (t of menuTypes; track t) {
                <option [value]="t">{{ t }}</option>
              }
            </select>
            <div class="form-hint">FOLDER is only a sidebar group title with no landing page</div>
          </div>
        </div>
        <div class="row mb-3">
          <div class="col-lg-6">
            <label class="form-label" for="path">Module Path</label>
            <input class="form-control" type="text" id="path" [formField]="menuForm.path" />
            <div class="form-hint">Relative path, mounted under /platform on the frontend</div>
          </div>
          <div class="col-lg-3">
            <label class="form-label" for="sortNo">Sort Order</label>
            <input class="form-control" type="number" id="sortNo" [formField]="menuForm.sortNo" />
          </div>
          <div class="col-lg-3">
            <label class="form-label" for="icons">Icon Name</label>
            <input class="form-control" type="text" id="icons" [formField]="menuForm.icons" />
          </div>
        </div>
        <div class="d-flex">
          <button class="btn btn-primary ms-auto" type="submit" [disabled]="!menuForm().valid() || isSubmitting()">
            Save Menu
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
export class MenuForm {
  inputData = input<MenuItem | null>(null);
  allMenus = input<MenuItem[]>([]);

  private readonly menuData = linkedSignal(this.inputData);
  readonly created = computed(() => this.menuData()?.code == undefined);

  private readonly _http = inject(HttpClient);
  private readonly _message = inject(MessageService);
  private readonly _destroyRef = inject(DestroyRef);

  protected readonly ROOT_PCODE = ROOT_PCODE;
  protected readonly menuTypes = MENU_TYPES;

  isSubmitting = signal(false);

  private readonly initialModel = {
    id: undefined as number | undefined,
    code: '',
    pcode: ROOT_PCODE,
    authority: '',
    type: 'MENU' as MenuType,
    name: '',
    path: '',
    sortNo: 0,
    icons: '',
  };

  protected readonly menuModel = signal({ ...this.initialModel });

  protected readonly menuForm = form(this.menuModel, (p) => {
    required(p.name, { message: 'Menu name is required' });
    required(p.authority, { message: 'Authority code is required' });
    // 后端 modify() 会用库里的旧值覆盖 authority，编辑时改了也不生效，直接禁用
    disabled(p.authority, () => !this.created());
  });

  /** 只有 FOLDER 能当父级，且不能选自己 */
  parentOptions = computed(() => {
    const selfCode = this.menuData()?.code;
    return this.allMenus().filter((m) => m.type === 'FOLDER' && m.code && m.code !== selfCode);
  });

  constructor() {
    effect(() => {
      const data = this.menuData();
      if (this.created()) {
        this.menuModel.set({
          ...this.initialModel,
          pcode: data?.pcode ?? ROOT_PCODE,
        });
      } else if (data) {
        this.menuModel.set({
          ...this.initialModel,
          id: data.id,
          code: data.code ?? '',
          pcode: data.pcode ?? ROOT_PCODE,
          authority: data.authority ?? '',
          type: data.type ?? 'MENU',
          name: data.name ?? '',
          path: data.path ?? '',
          sortNo: data.sortNo ?? 0,
          icons: data.icons ?? '',
        });
      }
    });
  }

  async onSubmit() {
    this.isSubmitting.set(true);
    await submit(this.menuForm, {
      action: async () => {
        const payload: Record<string, unknown> = { ...this.menuModel() };
        if (this.created()) {
          // 新增时后端不会补 tenantCode，空值会撞上 tenant_code 的 NOT NULL，这里给默认值
          delete payload['code'];
          payload['tenantCode'] = ROOT_PCODE;
        }
        this._http
          .post<MenuItem>(environment.relaApiPath + '/menus/save', payload)
          .pipe(
            tap(() => this._message.success(this.created() ? 'Menu created' : 'Menu updated')),
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
      const instance = tabler?.Modal?.getOrCreateInstance(el);
      instance?.hide();
    }
  }
}
