package com.stan.rsocket;

import io.rsocket.*;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import io.rsocket.util.EmptyPayload;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class FireAndForgetExample {

    public static void main(String[] args) {

        Disposable disposable = RSocketFactory.receive()
                .acceptor(((setup, sendingSocket) ->
                        Mono.just(
                                new AbstractRSocket() {
                                    @Override
                                    public Mono<Payload> requestResponse(Payload payload) {
                                        return Mono.just(DefaultPayload.create("ECHO >> " + payload.getDataUtf8()));
                                    }

                                    @Override
                                    public Mono<Void> fireAndForget(Payload payload) {
                                        System.out.printf("收到了请求" + payload.getDataUtf8().toUpperCase());
                                        return Mono.empty();
                                    }
                                })))
                .transport(TcpServerTransport.create("localhost", 7000))
                .start()
                .subscribe();

        RSocket socket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        assert socket != null;
        socket.fireAndForget(DefaultPayload.create("this is message".getBytes())).block();

        socket.fireAndForget(DefaultPayload.create("hwllo world".getBytes())).block();


        disposable.dispose();

    }
}
