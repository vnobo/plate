import {Component} from '@angular/core';
import {CommonModule} from '@angular/common';

@Component({
  selector: 'lib-commons',
  standalone: true,
  imports: [CommonModule],
  template: `
    <p>
      commons works!
    </p>
  `,
  styles: ``
})
export class CommonsComponent {

}
