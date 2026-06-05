import { Component, signal } from '@angular/core';
import { DatePipe, DecimalPipe, CurrencyPipe } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import dayjs from './core/dayjs.locale';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet, DatePipe, DecimalPipe, CurrencyPipe],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App {
  protected readonly title = signal('ng-web');
  protected readonly btnClicked = signal(false);

  protected toggleBtn(): void {
    this.btnClicked.update(v => !v);
  }

  // i18n test data
  protected readonly currentDate = new Date();
  protected readonly dayjsDate = dayjs();
  protected readonly sampleNumber = 1234567.89;
  protected readonly samplePrice = 99.99;
}
