/// <reference types="@angular/localize" />
import { registerLocaleData } from '@angular/common';
import zh from '@angular/common/locales/zh';

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from '@app/app.config';
import { AppComponent } from '@app/app.component';

registerLocaleData(zh);

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
