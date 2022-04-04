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

import xyz.dunjiao.cloud.grpc.gateway.filter.utils.ServerWebExchangeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Decode gRPC-web body byte[]
 *
 * @author allen.yuan@live.com
 */
@Slf4j
@Component
public class GrpcWebRequestBodyGlobalFilter implements GlobalFilter, Ordered {

    private final GatewayFilter delegate;
    public static int MODIFY_REQUEST_FILTER_ORDER = 10150;


    public GrpcWebRequestBodyGlobalFilter(ModifyRequestBodyGatewayFilterFactory modifyRequestBodyGatewayFilterFactory, Base64DecodeFunction base64DecodeFunction) {
        delegate = modifyRequestBodyGatewayFilterFactory
                .apply(new ModifyRequestBodyGatewayFilterFactory
                        .Config()
                        .setRewriteFunction(base64DecodeFunction)
                        .setInClass(byte[].class)
                        .setOutClass(byte[].class));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        if (ServerWebExchangeUtils.isGrpcWeb(exchange)) {
            log.debug("do request body decode");
            return delegate.filter(exchange, chain);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return MODIFY_REQUEST_FILTER_ORDER;
    }

}
