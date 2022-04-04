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
import org.springframework.cloud.gateway.filter.headers.HttpHeadersFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

/**
 * Change gRPC response header back to gRPC-web
 *
 * @author allen.yuan@live.com
 */
@Slf4j
@Component
public class GrpcWebResponseHeadersFilter implements HttpHeadersFilter, Ordered {

    public static final String GRPC_WEB_TEXT_CONTENT_TYPE = "application/grpc-web-text";

    @Override
    public HttpHeaders filter(HttpHeaders headers, ServerWebExchange exchange) {
        log.debug("Enter GRPCWebResponseHeadersFilter");
        if (ServerWebExchangeUtils.isGrpcWeb(exchange)) {
            headers.remove(HttpHeaders.TRAILER);
            headers.set(HttpHeaders.CONTENT_TYPE, GRPC_WEB_TEXT_CONTENT_TYPE);
            // mark gateway server
            headers.add("server", "Spring Cloud Gateway");
            log.debug(headers.toString());
        }
        return headers;
    }

    @Override
    public boolean supports(Type type) {
        return Type.RESPONSE.equals(type);
    }

    @Override
    public int getOrder() {
        return -3;
    }

}
