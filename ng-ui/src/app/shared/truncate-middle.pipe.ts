import { Pipe, type PipeTransform } from '@angular/core';

@Pipe({
  name: 'appTruncateMiddle',
})
export class TruncateMiddlePipe implements PipeTransform {
  transform(value: string, length: number = 8): string {
    if (!value || value.length <= length) {
      return value;
    }

    const start = value.substring(0, 3);
    const end = value.substring(value.length - 4);
    const middle = '..';

    return start + middle + end;
  }
}
