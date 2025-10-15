import { afterNextRender, Component, inject, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MessageService } from '@app/plugins';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  private readonly _message = inject(MessageService);

  constructor() {
    afterNextRender(async () => {
      this._message.success('Method not implemented.');
      this._message.info('Method not implemented.');
      this._message.warning('Method not implemented.');
      this._message.error('Method not implemented.');
    });
  }

  ngOnInit(): void {}
}
