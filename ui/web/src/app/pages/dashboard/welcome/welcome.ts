import {CommonModule} from '@angular/common';
import {Component, OnInit} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-welcome',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './welcome.html',
  styleUrls: ['./welcome.scss'],
})
export class Welcome implements OnInit {
  perfileForm = new FormGroup({
    name: new FormControl(''),
    email: new FormControl(''),
    password: new FormControl(''),
    confirmPassword: new FormControl(''),
  });
  ngOnInit() {}
}
