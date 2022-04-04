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

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;

/**
 * Change header from gRPC to gRPC-web in pre-filter
 * Change header to a none gRPC value in post-filter to skip the SCG GRPCResponseHeadersFilter
 *
 * @author allen.yuan@live.com
 */
@Slf4j
@Component
public class GrpcWebRequestHeaderGlobalFilter implements GlobalFilter, Ordered {
    private final String grpcContentType = "application/grpc";
    private final String notGrpcContentType = "application/not-grpc";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (xyz.dunjiao.cloud.grpc.gateway.filter.utils.ServerWebExchangeUtils.isGrpcWeb(exchange)) {
            ServerHttpRequest request = exchange.getRequest();
            return chain.filter(exchange.mutate().request(new ServerHttpRequestDecorator(request) {
                @Override
                @NonNull
                public HttpHeaders getHeaders() {
                    // refer to NettyWriteResponseFilter
                    // return different request headers for pre-filters and post-filters
                    Connection connection = exchange.getAttribute(ServerWebExchangeUtils.CLIENT_RESPONSE_CONN_ATTR);
                    HttpHeaders headers = new HttpHeaders();
                    headers.putAll(request.getHeaders());
                    if (connection == null) {
                        headers.set(HttpHeaders.CONTENT_TYPE, grpcContentType);

                    } else {
                        headers.set(HttpHeaders.CONTENT_TYPE, notGrpcContentType);
                    }
                    headers.set("te","trailers");
                    headers.remove(HttpHeaders.CONTENT_LENGTH);
                    return headers;
                }
            }).build());
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return GrpcWebRequestBodyGlobalFilter.MODIFY_REQUEST_FILTER_ORDER + 1;
    }
}
