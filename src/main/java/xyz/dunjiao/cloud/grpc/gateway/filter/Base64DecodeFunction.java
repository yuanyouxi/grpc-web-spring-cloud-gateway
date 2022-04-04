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

import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.factory.rewrite.RewriteFunction;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Base64;

/**
 * Base64 decode byte[]
 *
 * @author allen.yuan@live.com
 */
@Slf4j
@Component
public class Base64DecodeFunction implements RewriteFunction<byte[], byte[]> {
    @Override
    public Publisher<byte[]> apply(ServerWebExchange exchange, byte[] bytes) {
        if (!ServerWebExchangeUtils.isAlreadyRouted(exchange)) {
            log.debug("decode before route");
            Base64.Decoder decoder = Base64.getDecoder();
            return Mono.just(decoder.decode(bytes));
        } else {
            log.debug("decode after route");
        }
        return Mono.just(bytes);
    }
}
