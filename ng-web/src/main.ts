/// <reference types="@angular/localize" />

import { bootstrapApplication } from '@angular/platform-browser';
import { appConfig } from './app/app.config';
import { AppComponent } from './app/app.component';

import { registerLocaleData } from '@angular/common';
import zh from '@angular/common/locales/zh';

import dayjs from 'dayjs';
import isLeapYear from 'dayjs/plugin/isLeapYear';
import 'dayjs/locale/zh-cn';

registerLocaleData(zh);

dayjs.extend(isLeapYear);
dayjs.locale('zh-cn');

bootstrapApplication(AppComponent, appConfig).catch(err => console.error(err));
