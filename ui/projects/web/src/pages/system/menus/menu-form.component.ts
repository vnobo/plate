import {Component, EventEmitter, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Component({
  selector: 'app-menu-form',
  template: `
    <nz-modal
      [(nzVisible)]="isVisible"
      nzTitle="菜单编辑"
      (nzOnCancel)="handleCancel()"
      (nzOnOk)="handleOk()">
      <form nz-form [formGroup]="menuForm" (ngSubmit)="onSubmit()">
        <ng-container *nzModalContent class="p-3">
          <div class="row g-3 ">
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired nzFor="code"
              >编码
              </nz-form-label
              >
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="codeErrorTpl">
                <input
                  nz-input
                  type="text"
                  id="code"
                  formControlName="code"
                  [disabled]="true"/>
                <ng-template #codeErrorTpl let-control>
                  <ng-container
                    *ngIf="control.hasError('required')"
                  >Please input your
                    username!
                  </ng-container
                  >
                  <ng-container
                    *ngIf="control.hasError('duplicated')"
                  >The username is
                    redundant!
                  </ng-container
                  >
                </ng-template>
              </nz-form-control>
            </nz-form-item>
            <nz-form-item class="col-md">
              <nz-form-label [nzSpan]="6" nzRequired nzFor="pcode"
              >父级
              </nz-form-label
              >
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="pcodeErrorTpl">
                <input
                  [disabled]="true"
                  nz-input
                  type="text"
                  id="pcode"
                  formControlName="pcode"/>
                <ng-template #pcodeErrorTpl let-control>
                  <ng-container
                    *ngIf="control.hasError('required')"
                  >Please input your
                    username!
                  </ng-container
                  >
                  <ng-container
                    *ngIf="control.hasError('duplicated')"
                  >The username is
                    redundant!
                  </ng-container
                  >
                </ng-template>
              </nz-form-control>
            </nz-form-item>
          </div>
          <div class="row g-3">
            <nz-form-item class="col-md">
              <nz-form-label
                [nzSpan]="6"
                nzRequired
                nzFor="code"
                for="tenantCode"
              >租户
              </nz-form-label
              >
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="tenantCodeErrorTpl">
                <input
                  type="text"
                  class="form-control"
                  id="tenantCode"
                  formControlName="tenantCode"/>
                <ng-template #tenantCodeErrorTpl let-control>
                  <ng-container
                    *ngIf="control.hasError('required')"
                  >Please input your
                    username!
                  </ng-container
                  >
                  <ng-container
                    *ngIf="control.hasError('duplicated')"
                  >The username is
                    redundant!
                  </ng-container
                  >
                </ng-template>
              </nz-form-control>
            </nz-form-item>
            <nz-form-item class="col-md">
              <nz-form-label
                [nzSpan]="6"
                nzRequired
                nzFor="code"
                for="type"
              >类型
              </nz-form-label
              >
              <nz-form-control
                [nzSpan]="18"
                nzHasFeedback
                nzValidatingTip="验证中..."
                [nzErrorTip]="typeErrorTpl">
                <input
                  type="text"
                  class="form-control"
                  id="type"
                  formControlName="type"/>
                <ng-template #typeErrorTpl let-control>
                  <ng-container
                    *ngIf="control.hasError('required')"
                  >Please input your
                    username!
                  </ng-container
                  >
                  <ng-container
                    *ngIf="control.hasError('duplicated')"
                  >The username is
                    redundant!
                  </ng-container
                  >
                </ng-template>
              </nz-form-control>
            </nz-form-item>
          </div>
          <div class="form-group">
            <label for="authority"
            >权&nbsp;&nbsp;&nbsp;&nbsp;限</label
            >
            <input
              type="text"
              class="form-control"
              id="authority"
              formControlName="authority"/>
          </div>

          <div class="form-group">
            <label for="name">Name</label>
            <input
              type="text"
              class="form-control"
              id="name"
              formControlName="name"/>
          </div>

          <div class="form-group">
            <label for="path">Path</label>
            <input
              type="text"
              class="form-control"
              id="path"
              formControlName="path"/>
          </div>

          <div class="form-group">
            <label for="sort">Sort</label>
            <input
              type="text"
              class="form-control"
              id="sort"
              formControlName="sort"/>
          </div>

          <div class="form-group">
            <label for="extend">Extend</label>
            <input
              type="text"
              class="form-control"
              id="extend"
              formControlName="extend"/>
          </div>

          <div class="form-group">
            <label for="permissions">Permissions</label>
            <input
              type="text"
              class="form-control"
              id="permissions"
              formControlName="permissions"/>
          </div>

          <div class="form-group">
            <label for="icons">Icons</label>
            <input
              type="text"
              class="form-control"
              id="icons"
              formControlName="icons"/>
          </div>
        </ng-container>
        <ng-container *nzModalFooter>
          <button class="btn btn-outline-dark me-3" type="reset">
            重置
          </button>
          <button class="btn btn-primary" type="submit">确认</button>
        </ng-container>
      </form>
      <div class="clearfix">...</div>
    </nz-modal>
  `,
  styles: [],
})
export class MenuFormComponent {
  isVisible = false;

  @Output() event = new EventEmitter<{
    btn: string;
    status: number;
    data: any;
  }>();

  menuForm: FormGroup;

  constructor(private formBuilder: FormBuilder) {
    this.menuForm = this.formBuilder.group({
      id: [null],
      code: [null],
      pcode: [null],
      tenantCode: [null],
      type: [null, Validators.required],
      authority: [
        null,
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(64),
      ],
      name: [null, Validators.required],
      path: [
        null,
        Validators.required,
        Validators.pattern(/^(\/|(\/[a-zA-Z0-9_-]+)+)$/),
      ],
      sort: [null, Validators.required],
      extend: [null],
      permissions: [null],
      icons: [null],
    });
  }

  //表单提交
  onSubmit() {
  }

  showModal(): void {
    this.event.next({btn: 'show', status: 0, data: null});
    this.isVisible = true;
  }

  handleOk(): void {
    this.event.next({btn: 'ok', status: 200, data: null});
    this.isVisible = false;
  }

  handleCancel(): void {
    this.event.next({btn: 'cancel', status: -1, data: null});
    this.isVisible = false;
  }
}
