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

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;
import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.containsEncodedParts;

/**
 * Discover gRPC service port from service metadata
 * Build final request URI before route
 *
 * @author allen.yuan@live.com
 */
@Component
@Slf4j
public class GrpcLoadBalanceGatewayFilterFactory extends AbstractGatewayFilterFactory<GrpcLoadBalanceGatewayFilterFactory.Config> {
    public static final int GRPC_LOAD_BALANCER_CLIENT_FILTER_ORDER = 10151;

    public GrpcLoadBalanceGatewayFilterFactory() {
        super(GrpcLoadBalanceGatewayFilterFactory.Config.class);
        log.info("Loaded GrpcGatewayFilterFactory [Grpc]");
    }

    @Override
    public GatewayFilter apply(Config config) {
        return new OrderedGatewayFilter(((exchange, chain) -> {
            URI uri = exchange.getRequiredAttribute(GATEWAY_REQUEST_URL_ATTR);
            Response<ServiceInstance> response = exchange.getRequiredAttribute(GATEWAY_LOADBALANCER_RESPONSE_ATTR);
            ServiceInstance serviceInstance = response.getServer();
            Map<String, String> metadata = serviceInstance.getMetadata();
            if (metadata != null && !metadata.isEmpty()) {
                String portString = metadata.get("gRPC_port");
                if (portString == null) {
                    portString = metadata.get("gRPC.port");
                    if (portString == null) {
                        // return 404
                        throw NotFoundException.create(true, "Unable to find grpc instance for service " + serviceInstance.getServiceId());
                    }
                    log.warn("Found legacy grpc port metadata.");
                }
                // change port and continue
                int port = Integer.parseInt(portString);
                URI requestUrl = reconstructUri(port, uri);
                exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, requestUrl);
                return chain.filter(exchange);
            } else {
                // 404
                throw NotFoundException.create(true, "Unable to find grpc instance for service " + serviceInstance.getServiceId());
            }
        }), GRPC_LOAD_BALANCER_CLIENT_FILTER_ORDER);
    }

    @Data
    public static class Config {
        private boolean enabled;
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("enabled");
    }

    private URI reconstructUri(int port, URI original) {
        if (port == original.getPort()) {
            return original;
        }
        boolean encoded = containsEncodedParts(original);
        return UriComponentsBuilder.fromUri(original).port(port).build(encoded).toUri();
    }

}
