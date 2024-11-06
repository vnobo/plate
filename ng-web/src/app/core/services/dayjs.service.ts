import { Injectable } from '@angular/core';
import dayjs from 'dayjs';
import isLeapYear from 'dayjs/plugin/isLeapYear';
import 'dayjs/locale/zh-cn';

@Injectable({
  providedIn: 'root',
})
export class DayjsService {
  dayjs = dayjs;

  constructor() {
    dayjs.extend(isLeapYear);
    dayjs.locale('zh-cn');
  }

  getCurrentDate() {
    return dayjs();
  }
}
