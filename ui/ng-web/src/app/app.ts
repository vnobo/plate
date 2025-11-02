import { afterNextRender, Component, OnInit, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { MessageService } from '@app/plugins';

@Component({
  selector: 'app-root',
  imports: [RouterOutlet],
  templateUrl: './app.html',
  styleUrl: './app.scss',
})
export class App implements OnInit {
  constructor(private readonly messageService: MessageService) {
    afterNextRender(async () => {
      this.messageService.success('Method not implemented.');
      this.messageService.info('Method not implemented.');
      this.messageService.warning('Method not implemented.');
      this.messageService.error('Method not implemented.');
    });
  }

  ngOnInit(): void {}
}
