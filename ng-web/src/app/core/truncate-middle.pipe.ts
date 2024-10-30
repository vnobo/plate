import { Pipe, type PipeTransform } from '@angular/core';

@Pipe({
  name: 'appTruncateMiddle',
  standalone: true,
})
export class TruncateMiddlePipe implements PipeTransform {
  transform(value: string, length: number = 6): string {
    if (!value || value.length <= length) {
      return value;
    }

    const start = value.substring(0, 4);
    const end = value.substring(value.length - 5);
    const middle = '..';

    return start + middle + end;
  }
}
