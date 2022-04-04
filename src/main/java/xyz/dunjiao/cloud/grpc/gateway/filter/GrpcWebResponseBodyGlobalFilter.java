/*
 * Copyright (c) 2022. Yuan Kai<allen.yuan@live.com>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package xyz.dunjiao.cloud.grpc.gateway.filter;

import org.reactivestreams.Publisher;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import reactor.core.publisher.Flux;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.NettyWriteResponseFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * Transfer response chunks from gRPC to gRPC-web
 *
 * @author allen.yuan@live.com
 */
@Slf4j
@Component
public class GrpcWebResponseBodyGlobalFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return chain.filter(exchange.mutate().response(new Base64EncodedServerHttpResponse(exchange)).build());
    }

    protected class Base64EncodedServerHttpResponse extends ServerHttpResponseDecorator {
        private final DataBufferFactory bufferFactory;

        public Base64EncodedServerHttpResponse(ServerWebExchange exchange) {
            super(exchange.getResponse());
            ServerHttpResponse originalResponse = exchange.getResponse();
            this.bufferFactory = originalResponse.bufferFactory();
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            return this.getDelegate().writeWith(Flux.from(body).map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                Base64.Encoder encoder = Base64.getEncoder();
                return bufferFactory.wrap(encoder.encode(bytes));
            }));
        }
    }

    @Override
    public int getOrder() {
        return NettyWriteResponseFilter.WRITE_RESPONSE_FILTER_ORDER - 1;
    }

}
