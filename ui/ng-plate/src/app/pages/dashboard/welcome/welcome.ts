import { Component } from '@angular/core';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'app-welcome',
  imports: [ReactiveFormsModule],
  templateUrl: './welcome.html',
  styleUrl: './welcome.scss',
})
export class Welcome {
  perfileForm = new FormGroup({
    name: new FormControl(''),
    email: new FormControl(''),
    password: new FormControl(''),
    confirmPassword: new FormControl(''),
  });
}
