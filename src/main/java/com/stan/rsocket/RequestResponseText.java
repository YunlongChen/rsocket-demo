package com.stan.rsocket;

import io.rsocket.AbstractRSocket;
import io.rsocket.Payload;
import io.rsocket.RSocket;
import io.rsocket.RSocketFactory;
import io.rsocket.frame.decoder.PayloadDecoder;
import io.rsocket.transport.netty.client.TcpClientTransport;
import io.rsocket.transport.netty.server.TcpServerTransport;
import io.rsocket.util.DefaultPayload;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

public class RequestResponseText {


    public static void main(String[] args) {

        Disposable localhost = RSocketFactory.receive()
                .frameDecoder(PayloadDecoder.ZERO_COPY)
                .acceptor(((setup, sendingSocket) ->
                        Mono.just(
                                new AbstractRSocket() {
                                    @Override
                                    public Mono<Payload> requestResponse(Payload payload) {
                                        return Mono.just(DefaultPayload.create("ECHO >> " + payload.getDataUtf8()));
                                    }
                                })
                ))
                .transport(TcpServerTransport.create("localhost", 7000))
                .start()
                .subscribe();


        RSocket clientSocket = RSocketFactory.connect()
                .transport(TcpClientTransport.create("localhost", 7000))
                .start()
                .block();

        assert clientSocket != null;
        clientSocket.requestResponse(DefaultPayload.create("Hello"))
                .map(Payload::getDataUtf8)
                .doOnNext(System.out::print)
                .doOnEach((signal) -> System.out.print("what i have created!" + signal))
                .block();
        ///销毁链接
        localhost.dispose();
    }
}
