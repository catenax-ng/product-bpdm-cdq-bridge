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

package com.catenax.bpdmcdqbridge.saas.config



import org.eclipse.tractusx.bpdm.pool.api.client.PoolClientImpl
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.web.reactive.function.client.WebClient


@Configuration
class PoolClientConfig {




    @Bean
    fun poolClient(webServerAppCtxt: ServletWebServerApplicationContext,env:Environment): PoolClientImpl {
        val url = env.getProperty("bpdm.poolClient.url") ?: throw IllegalStateException("poolClient.url not found")
        val port = env.getProperty("bpdm.poolClient.port")?: throw IllegalStateException("poolClient.port not found")
        return PoolClientImpl { WebClient.create("$url:${port.toInt()}") }
    }
}