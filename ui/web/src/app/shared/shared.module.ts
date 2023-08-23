import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {RouterModule} from "@angular/router";

@NgModule({
  imports: [
    CommonModule,
    RouterModule
  ],
  declarations: [
    PageNotFoundComponent
  ],
  exports: [
    PageNotFoundComponent,
    CommonModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule,
    MatSnackBarModule,
    MatProgressBarModule
  ]
})
export class SharedModule {
}
