import { Component, computed, effect, inject, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { Menu, MenuType } from './menu.types';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';
import { SHARED_IMPORTS } from '@app/shared/shared-imports';
import { CommonModule } from '@angular/common';
import { User } from '@app/pages/home/users/user.types';

@Component({
    selector: 'app-menu-form',
    imports: [
        CommonModule,
        NzModalModule,
        NzFormModule,
        NzSelectModule,
        NzButtonModule,
        NzInputModule,
        ...SHARED_IMPORTS,
    ],
    template: `
    <form nz-form [formGroup]="menuForm" (ngSubmit)="submitForm()">
      <div class="row my-2">
        <nz-form-item class="col-md mx-1mx-1">
          <nz-form-label nzRequired nzFor="code">编码</nz-form-label>
          <nz-form-control nzHasFeedback>
            <input nz-input type="text" id="code" formControlName="code" />
          </nz-form-control>
        </nz-form-item>
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired nzFor="pcode">父级</nz-form-label>
          <nz-form-control nzHasFeedback>
            <input nz-input type="text" id="pcode" formControlName="pcode" />
          </nz-form-control>
        </nz-form-item>
      </div>
      <div class="row my-2">
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired nzFor="code" for="tenantCode">租户</nz-form-label>
          <nz-form-control
            nzHasFeedback
            nzValidatingTip="验证中..."
            [nzErrorTip]="tenantCodeErrorTpl">
            <input nz-input type="text" id="tenantCode" formControlName="tenantCode" />
            <ng-template #tenantCodeErrorTpl let-control>
              <ng-container *ngIf="control.hasError('required')"
              >Please input your tenantCode!
              </ng-container>
              <ng-container *ngIf="control.hasError('duplicated')"
              >The tenantCode is redundant!
              </ng-container>
            </ng-template>
          </nz-form-control>
        </nz-form-item>
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="type">类型</nz-form-label>
          <nz-form-control
            nzHasFeedback
            nzValidatingTip="验证中..."
            [nzErrorTip]="typeErrorTpl">
            <nz-select id="type" formControlName="type">
              <nz-option nzLabel="文件夹" nzValue="FOLDER"></nz-option>
              <nz-option nzLabel="菜单" nzValue="MENU"></nz-option>
              <nz-option nzLabel="链接" nzValue="LINK"></nz-option>
              <nz-option nzLabel="API" nzValue="API"></nz-option>
            </nz-select>
            <ng-template #typeErrorTpl let-control>
              <ng-container *ngIf="control.hasError('required')"
              >Please input your type!
              </ng-container>
              <ng-container *ngIf="control.hasError('duplicated')"
              >The type is redundant!
              </ng-container>
            </ng-template>
          </nz-form-control>
        </nz-form-item>
      </div>
      <div class="row my-2">
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="authority">权限</nz-form-label>
          <nz-form-control nzHasFeedback nzValidatingTip="验证中...">
            <nz-input-group nzAddOnBefore="ROLE_">
              <input nz-input type="text" id="authority" formControlName="authority" />
            </nz-input-group>
          </nz-form-control>
        </nz-form-item>
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="name">名称</nz-form-label>
          <nz-form-control nzHasFeedback nzValidatingTip="验证中...">
            <input nz-input type="text" id="name" formControlName="name" />
          </nz-form-control>
        </nz-form-item>
      </div>
      <div class="row my-2">
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="sortNo">排序</nz-form-label>
          <nz-form-control nzHasFeedback nzValidatingTip="验证中...">
            <input nz-input type="text" id="sortNo" formControlName="sortNo" />
          </nz-form-control>
        </nz-form-item>
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="icons">图标</nz-form-label>
          <nz-form-control nzHasFeedback nzValidatingTip="验证中...">
            <input nz-input type="text" id="icons" formControlName="icons" />
          </nz-form-control>
        </nz-form-item>
      </div>
      <div class="row my-2">
        <nz-form-item class="col-md mx-1">
          <nz-form-label nzRequired for="path">路径</nz-form-label>
          <nz-form-control nzHasFeedback nzValidatingTip="验证中...">
            <input nz-input type="text" id="path" formControlName="path" />
          </nz-form-control>
        </nz-form-item>
      </div>
      <nz-form-item>
        <nz-form-control [nzOffset]="6" [nzSpan]="5">
          <button nz-button type="reset">重置</button>
        </nz-form-control>
        <nz-form-control [nzOffset]="3" [nzSpan]="5">
          <button [disabled]="menuForm.invalid" nz-button nzType="primary" type="submit">保存</button>
        </nz-form-control>
      </nz-form-item>
    </form>
  `
})
export class MenuFormComponent implements OnInit {
  menuData = signal<Menu>({} as Menu);
  created = computed(() => this.menuData().id == undefined);
  formSubmit = output<Menu>();
  private readonly _formBuilder = inject(FormBuilder);
  menuForm: FormGroup = this._formBuilder.group({
    id: [null],
    code: [{ value: '0', disabled: true }, [Validators.required]],
    pcode: [{ value: '0', disabled: true }, [Validators.required]],
    tenantCode: [{ value: '0', disabled: true }, Validators.required],
    type: [MenuType.MENU, Validators.required],
    authority: [null, [Validators.required, Validators.minLength(6), Validators.maxLength(64)]],
    name: [null, Validators.required],
    path: ['', [Validators.required, Validators.pattern(/^(\/|(\/[a-zA-Z0-9_-]+)+)$/)]],
    icons: [null, Validators.required],
    sortNo: [null, Validators.required],
    extend: [null],
    permissions: [null],
  });

  constructor() {
    effect(() => {
      if (this.created()) {
        this.menuForm.patchValue({} as User);
      } else {
        this.menuForm.patchValue(this.menuData());
      }
    });
  }

  ngOnInit(): void {
  }

  submitForm() {
    if (this.menuForm.valid) {
      this.formSubmit.emit(this.menuForm.getRawValue());
    }
  }
}
