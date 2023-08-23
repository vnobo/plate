import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormControl, Validators} from "@angular/forms";
import {Credentials, LoginService} from "./login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {LayoutService} from "../../shared/layout.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {

  loginForm = this.formBuilder.group({
    username: new FormControl('', [
      Validators.required,
      Validators.minLength(5),
      Validators.maxLength(32),
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(6),
      Validators.maxLength(32)
    ]),
    rememberMe: new FormControl(false)
  });

  constructor(private formBuilder: FormBuilder,
              private loginService: LoginService,
              private layoutService: LayoutService,
              private router: Router,
              private route: ActivatedRoute) {
  }

  onSubmit(): void {
    const credentials: Credentials = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password
    };

    if (this.loginForm.value.rememberMe) {
      this.loginService.rememberMe(credentials);
    } else {
      this.loginService.clearRememberMe();
    }
    this.loginService.login(credentials).subscribe((res) => {
      this.router.navigate(['/dashboard/index'], {relativeTo: this.route}).then();
    });
  }

  ngOnInit(): void {
    if (this.loginService.getRememberMe()) {
      const credentials = JSON.parse(localStorage.getItem('credentials') || '{}');
      this.loginForm.patchValue({
        username: credentials.username,
        password: credentials.password,
        rememberMe: true
      });
    }
    this.layoutService.isHide(true);
  }

  ngOnDestroy(): void {
    this.layoutService.isHide(false);
  }

}
