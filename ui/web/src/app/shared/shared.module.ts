import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {PageNotFoundComponent} from "./page-not-found/page-not-found.component";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatProgressBarModule} from "@angular/material/progress-bar";
import {RouterModule} from "@angular/router";
import {AsideNavbarComponent} from './aside-navbar/aside-navbar.component';
import {HeaderNavbarComponent} from './header-navbar/header-navbar.component';


@NgModule({
  imports: [
    CommonModule,
    RouterModule
  ],
  declarations: [
    PageNotFoundComponent,
    AsideNavbarComponent,
    HeaderNavbarComponent
  ],
  exports: [
    PageNotFoundComponent,
    AsideNavbarComponent,
    HeaderNavbarComponent,
    MatSnackBarModule,
    MatProgressBarModule,
    CommonModule,
    FormsModule,
    HttpClientModule,
    ReactiveFormsModule
  ]
})
export class SharedModule {
}
