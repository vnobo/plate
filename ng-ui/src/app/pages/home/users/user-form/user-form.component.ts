import { Component, computed, effect, inject, OnInit, output, signal } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { SHARED_IMPORTS } from '@app/shared/shared-imports';
import { User } from '@app/pages/home/users/user.types';
import { NzModalModule } from 'ng-zorro-antd/modal';

@Component({
  selector: 'app-user-form',
  standalone: true,
  imports: [NzModalModule, ...SHARED_IMPORTS],
  templateUrl: './user-form.component.html',
  styleUrl: './user-form.component.scss',
})
export class UserFormComponent implements OnInit {
  userData = signal<User>({} as User);
  created = computed(() => this.userData().id == undefined);
  formSubmit = output<User>();
  private readonly fb = inject(FormBuilder);
  userForm: FormGroup = this.fb.group({
    id: [null],
    code: [''],
    tenantCode: [''],
    username: ['', [Validators.required]],
    password: [''],
    confirmPassword: [''],
    disabled: [false],
    accountExpired: [false],
    accountLocked: [false],
    credentialsExpired: [false],
    email: ['', [Validators.email]],
    phone: [''],
    name: ['', Validators.required],
    avatar: [''],
    bio: [''],
  });

  constructor() {
    effect(() => {
      if (this.created()) {
        this.userForm.controls['password'].addValidators([Validators.required, Validators.minLength(6)]);
        this.userForm.controls['confirmPassword'].addValidators([Validators.required, Validators.minLength(6)]);
        this.userForm.patchValue({} as User);
      } else {
        this.userForm.controls['password'].clearValidators();
        this.userForm.controls['confirmPassword'].clearValidators();
        this.userForm.controls['username'].disable({ onlySelf: true });
        this.userForm.patchValue(this.userData());
      }
    });
  }

  ngOnInit(): void {
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      if (this.userForm.controls['password'].value != this.userForm.controls['confirmPassword'].value) {
        this.userForm.controls['confirmPassword'].setErrors({ confirm: true });
        return;
      }
      this.formSubmit.emit(this.userForm.getRawValue());
    }
  }
}
