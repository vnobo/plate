import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Output, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { NzFormModule } from 'ng-zorro-antd/form';
import { NzModalModule } from 'ng-zorro-antd/modal';
import { Menu, MenuType } from './menu.types';
import { NzSelectModule } from 'ng-zorro-antd/select';
import { NzInputModule } from 'ng-zorro-antd/input';
import { NzButtonModule } from 'ng-zorro-antd/button';

@Component({
  selector: 'app-menu-form',
  standalone: true,
  imports: [
    ReactiveFormsModule,
    CommonModule,
    NzModalModule,
    NzFormModule,
    NzSelectModule,
    NzButtonModule,
    NzInputModule,
  ],
  template: `
    <nz-modal [(nzVisible)]="isVisible" nzTitle="菜单编辑" (nzOnCancel)="handleCancel()">
      <form nz-form [formGroup]="menuForm" (ngSubmit)="submitForm()">
        <div *nzModalContent class="my-1">
          <div class="row g-3 my-1">
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired nzFor="code">编码</nz-form-label>
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="codeErrorTpl">
                <input nz-input type="text" id="code" formControlName="code" />
                <ng-template #codeErrorTpl let-control>
                  <ng-container *ngIf="control.hasError('required')"
                    >Please input your username!
                  </ng-container>
                  <ng-container *ngIf="control.hasError('duplicated')"
                    >The username is redundant!
                  </ng-container>
                </ng-template>
              </nz-form-control>
            </nz-form-item>
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired nzFor="pcode">父级</nz-form-label>
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="pcodeErrorTpl">
                <input nz-input type="text" id="pcode" formControlName="pcode" />
                <ng-template #pcodeErrorTpl let-control>
                  <ng-container *ngIf="control.hasError('required')"
                    >Please input your username!
                  </ng-container>
                  <ng-container *ngIf="control.hasError('duplicated')"
                    >The username is redundant!
                  </ng-container>
                </ng-template>
              </nz-form-control>
            </nz-form-item>
          </div>
          <div class="row g-3 my-1">
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired nzFor="code" for="tenantCode"
                >租户</nz-form-label
              >
              <nz-form-control
                [nzSpan]="18"
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
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired for="type">类型</nz-form-label>
              <nz-form-control
                [nzSpan]="18"
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
          <div class="row g-3 my-1">
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired for="authority">权限</nz-form-label>
              <nz-form-control [nzSpan]="18" nzHasFeedback nzValidatingTip="验证中...">
                <nz-input-group nzAddOnBefore="ROLE_">
                  <input nz-input type="text" id="authority" formControlName="authority" />
                </nz-input-group>
              </nz-form-control>
            </nz-form-item>
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired for="name">名称</nz-form-label>
              <nz-form-control [nzSpan]="18" nzHasFeedback nzValidatingTip="验证中...">
                <input nz-input type="text" id="name" formControlName="name" />
              </nz-form-control>
            </nz-form-item>
          </div>
          <div class="row g-3 my-1">
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired for="sortNo">排序</nz-form-label>
              <nz-form-control [nzSpan]="18" nzHasFeedback nzValidatingTip="验证中...">
                <input nz-input type="text" id="sortNo" formControlName="sortNo" />
              </nz-form-control>
            </nz-form-item>
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired for="icons">图标</nz-form-label>
              <nz-form-control [nzSpan]="18" nzHasFeedback nzValidatingTip="验证中...">
                <input nz-input type="text" id="icons" formControlName="icons" />
              </nz-form-control>
            </nz-form-item>
          </div>
          <div class="row g-3 my-1">
            <nz-form-item class="col-md-6">
              <nz-form-label [nzSpan]="6" nzRequired for="path">路径</nz-form-label>
              <nz-form-control [nzSpan]="18" nzHasFeedback nzValidatingTip="验证中...">
                <input nz-input type="text" id="path" formControlName="path" />
              </nz-form-control>
            </nz-form-item>
          </div>
        </div>
        <div *nzModalFooter>
          <button nzType="default" nz-button (click)="restForm()">重置</button>
          <button nzType="primary" nz-button (click)="submitForm()">确认</button>
        </div>
      </form>
      <div class="clearfix">...</div>
    </nz-modal>
  `,
})
export class MenuFormComponent {
  isVisible = signal(false);

  @Output() event = new EventEmitter<{
    btn: string;
    status: number;
    data: null;
  }>();

  menuForm: FormGroup = new FormGroup({});

  constructor(private formBuilder: FormBuilder) {
    this.menuForm = this.formBuilder.group({
      id: [null],
      code: [{ value: null, disabled: true }, Validators.required],
      pcode: [{ value: '0', disabled: true }, Validators.required],
      tenantCode: [{ value: '0', disabled: true }, Validators.required],
      type: [MenuType.MENU, Validators.required],
      authority: [null, Validators.required, Validators.minLength(6), Validators.maxLength(64)],
      name: [null, Validators.required],
      path: [null, Validators.required, Validators.pattern(/^(\/|(\/[a-zA-Z0-9_-]+)+)$/)],
      icons: [null, Validators.required],
      sortNo: [null, Validators.required],
      extend: [null],
      permissions: [null],
    });
  }

  submitForm() {
    const menu = this.menuForm.value as Menu;
    console.log(menu);
  }

  restForm(): void {
    this.menuForm.reset();
  }

  showModal(): void {
    this.event.next({ btn: 'show', status: 0, data: null });
    this.isVisible.set(true);
  }

  handleCancel(): void {
    this.event.next({ btn: 'cancel', status: -1, data: null });
    this.isVisible.set(false);
  }
}
