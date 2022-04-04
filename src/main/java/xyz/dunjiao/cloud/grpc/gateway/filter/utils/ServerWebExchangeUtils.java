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

package xyz.dunjiao.cloud.grpc.gateway.filter.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.web.server.ServerWebExchange;

import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR;

/**
 * Exchange Utils
 *
 * check route contains grpc-web metadata
 *
 * @author allen.yuan@live.com
 */
@Slf4j
public final class ServerWebExchangeUtils {
    public static String GRPC_WEB = "grpcWeb";

    public static boolean isGrpcWeb(ServerWebExchange exchange) {
        boolean res = false;
        Route route = exchange.getAttribute(GATEWAY_ROUTE_ATTR);
        if (route != null) {
            // get the route metadata do filter if marked grpc-web
            Map<String, Object> metadata = route.getMetadata();
            if (!metadata.isEmpty() && metadata.containsKey(GRPC_WEB)) {
                try {
                    res = (boolean) route.getMetadata().get(GRPC_WEB);
                } catch (Exception e) {
                    if (e.getClass() == ClassCastException.class) {
                        log.error("route:" + route.getId() + " metadata grpcWeb is not boolean");
                    } else {
                        log.error(e.getMessage());
                    }
                }
            }
        }
        return res;
    }
}
