import { Injectable } from '@angular/core';
import {
  APPLICATION_JSON,
  BufferEncoders,
  encodeCompositeMetadata,
  encodeRoute,
  encodeSimpleAuthMetadata,
  MESSAGE_RSOCKET_AUTHENTICATION,
  MESSAGE_RSOCKET_COMPOSITE_METADATA,
  MESSAGE_RSOCKET_ROUTING,
  RSocketClient,
} from 'rsocket-core';
import { Single } from 'rsocket-flowable';
import { ReactiveSocket } from 'rsocket-types';
import RSocketWebSocketClient from 'rsocket-websocket-client';
import { Observable } from 'rxjs';

export interface MessageOut {
  type: string;
  content: string;
  data: any;
  from: string;
  to: string;
  status: number;
}

@Injectable({ providedIn: 'root' })
export class RSocketCLientService {
  private readonly transport = new RSocketWebSocketClient(
    {
      debug: true,
      url: 'ws://localhost:4200/rsocket',
      wsCreator: url => new WebSocket(url),
    },
    BufferEncoders
  );

  private socketClient: Single<ReactiveSocket<Buffer, Buffer>> | null = null;
  private token: string | null = null;

  connect(token: string) {
    const socketClient = new RSocketClient({
      setup: {
        // ms btw sending keepalive to server
        keepAlive: 6000,
        // ms timeout if no keepalive response
        lifetime: 180000,
        dataMimeType: APPLICATION_JSON.string,
        metadataMimeType: MESSAGE_RSOCKET_COMPOSITE_METADATA.string,
        payload: {
          data: Buffer.from(token),
          metadata: encodeCompositeMetadata([
            [MESSAGE_RSOCKET_ROUTING, encodeRoute('connect.setup')],
            [MESSAGE_RSOCKET_AUTHENTICATION, encodeSimpleAuthMetadata('admin', '123456')],
          ]),
        },
      },
      transport: this.transport,
    });
    this.token = token;
    this.socketClient = socketClient.connect();
  }
  connectionStatus() {
    if (this.socketClient === null) {
      throw new Error('socketClient is null');
    }
    this.socketClient.then(socket => {
      socket.connectionStatus().subscribe(event => console.log(`connectionStatus :${event.kind}`));
    });
  }

  requestStream(route?: string): Observable<MessageOut> {
    route = route || 'request.stream';
    const observable = new Observable<MessageOut>(subscriber => {
      if (this.socketClient === null) {
        subscriber.error(new Error('socketClient 不能为空!,请先调用 connect 方法设置!'));
        return;
      }
      if (this.token === null) {
        subscriber.error(new Error('token 不能为空!,请先调用 connect 方法设置!'));
        return;
      }
      const token = this.token;
      this.socketClient.subscribe({
        onComplete: socket =>
          socket
            .requestStream({
              data: Buffer.from(token),
              metadata: encodeCompositeMetadata([[MESSAGE_RSOCKET_ROUTING, encodeRoute(route)]]),
            })
            .subscribe({
              onComplete: () => {
                console.log('complete');
                subscriber.complete();
              },
              onError: error => {
                console.log('Connection has been closed due to:: ' + error);
                subscriber.error(error);
              },
              onNext: value => {
                const jsonStr = value.data?.toString();
                if (jsonStr !== undefined && jsonStr !== null) {
                  console.log('onNext: %s,%s', value.data, value.metadata);
                  subscriber.next(JSON.parse(jsonStr));
                }
              },
              onSubscribe: subscription => {
                console.log('onNext: %s,%s', subscription);
                subscription.request(2147483647);
              },
            }),
        onError: error => {
          console.log('error:', error);
          subscriber.error(error);
        },
      });
    });
    return observable;
  }
}
