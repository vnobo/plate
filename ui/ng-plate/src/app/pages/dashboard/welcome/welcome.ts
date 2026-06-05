
import {Component, OnInit, ChangeDetectionStrategy} from '@angular/core';
import {FormControl, FormGroup, ReactiveFormsModule} from '@angular/forms';

@Component({
  selector: 'app-welcome',
  imports: [ReactiveFormsModule],
  templateUrl: './welcome.html',
  changeDetection: ChangeDetectionStrategy.Eager,
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
