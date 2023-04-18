/*******************************************************************************
 * Copyright (c) 2021,2023 Contributors to the Eclipse Foundation
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/

package com.catenax.bpdm.bridge.cdq.config


import org.eclipse.tractusx.bpdm.pool.api.client.PoolClientImpl
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class ClientsConfig {

    @Bean
    @ConditionalOnProperty(
        value = ["bpdm.pool-client.pool-security-enabled"],
        havingValue = "true"
    )
    fun poolClient(
        poolSecurityConfigProperties: PoolSecurityConfigProperties,
        webServerAppCtxt: ServletWebServerApplicationContext,
        env: Environment,
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService
    ): PoolClientImpl {
        val url =
            env.getProperty("bpdm.pool-client.url") ?: throw IllegalStateException("bpdm.pool-client.url not found")
        val webClient =
            createWebClient(poolSecurityConfigProperties, clientRegistrationRepository, authorizedClientService, url)
        return PoolClientImpl { webClient }
    }


    @Bean
    @ConditionalOnProperty(
        value = ["bpdm.pool-client.pool-security-enabled"],
        havingValue = "false", matchIfMissing = true
    )
    fun poolClientNoAuth(env: Environment, poolSecurityConfigProperties: PoolSecurityConfigProperties): PoolClientImpl {
        val url =
            env.getProperty("bpdm.pool-client.url") ?: throw IllegalStateException("bpdm.pool-client.url not found")
        return PoolClientImpl { WebClient.create(url) }
    }


    private fun createWebClient(
        poolSecurityConfigProperties: PoolSecurityConfigProperties,
        clientRegistrationRepository: ClientRegistrationRepository,
        authorizedClientService: OAuth2AuthorizedClientService,
        baseUrl: String
    ): WebClient {
        val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
        val authorizedClientManager =
            AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService)
        authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)

        val oauth = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
        oauth.setDefaultClientRegistrationId(poolSecurityConfigProperties.oauth2ClientRegistration)
        return WebClient.builder()
            .apply(oauth.oauth2Configuration())
            .baseUrl(baseUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build()
    }


}