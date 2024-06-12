import {Component, OnDestroy, OnInit} from '@angular/core';
import {FormBuilder, FormControl, FormGroup, Validators} from "@angular/forms";
import {Credentials, LoginService} from "./login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {Subject, takeUntil} from "rxjs";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit, OnDestroy {

  loginForm: FormGroup<{
    username: FormControl<string | null>,
    password: FormControl<string | null>,
    rememberMe: FormControl<boolean | null>
  }>;

  private _subject: Subject<void> = new Subject<void>();

  constructor(private router: Router,
              private route: ActivatedRoute,
              private formBuilder: FormBuilder,
              private loginService: LoginService) {
    this.loginForm = this.formBuilder.group({
      username: new FormControl('', [
        Validators.required,
        Validators.minLength(5),
        Validators.maxLength(64)
      ]),
      password: new FormControl('', [
        Validators.required,
        Validators.minLength(6),
        Validators.maxLength(64)
      ]),
      rememberMe: new FormControl(false)
    });
  }

  onSubmit(): void {
    const credentials: Credentials = {
      username: this.loginForm.value.username,
      password: this.loginForm.value.password
    };

    if (this.loginForm.value.rememberMe) {
      this.loginService.rememberMe(credentials);
    }
    const login = this.loginService.login(credentials);
    const result = login.pipe(takeUntil(this._subject));
    result.subscribe(() => {
      this.router.navigate(['/home'], {relativeTo: this.route}).then();
    });
  }

  ngOnInit(): void {
  }

  ngOnDestroy(): void {
    this._subject.next();
    this._subject.complete();
  }
}
