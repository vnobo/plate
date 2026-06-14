import { Component, signal } from '@angular/core';
import { form } from '@angular/forms/signals';

@Component({
  selector: 'app-welcome',
  imports: [],
  templateUrl: './welcome.html',
  styleUrl: './welcome.scss',
})
export class Welcome {
  protected readonly profileModel = signal({
    name: '',
    email: '',
    password: '',
    confirmPassword: '',
  });

  protected readonly profileForm = form(this.profileModel);
}
