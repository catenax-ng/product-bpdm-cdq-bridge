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

package com.catenax.bpdmcdqbridge.saas.util

import com.catenax.bpdmcdqbridge.saas.util.OpenSearchContextInitializer.Companion.openSearchContainer
import com.catenax.bpdmcdqbridge.saas.util.PostgreSQLContextInitializer.Companion.postgreSQLContainer
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.GenericContainer
import org.testcontainers.lifecycle.Startable

/**
 * When used on a spring boot test, starts a singleton postgres db container that is shared between all integration tests.
 */


class BpdmPoolContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {

    companion object {
        const val BPDM_PORT = 8080

        private val bpdmPoolContainer: GenericContainer<*> =
            GenericContainer("ghcr.io/catenax-ng/tx-bpdm/pool:4.0.0-alpha.1")
                .dependsOn(listOf<Startable>(postgreSQLContainer, openSearchContainer))
                .withNetwork(postgreSQLContainer.getNetwork())
                .withExposedPorts(BPDM_PORT)


    }


    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        val postgresNetworkAlias = applicationContext.environment.getProperty("bpdm.datasource.alias")
        val openSearchNetworkAlias = applicationContext.environment.getProperty("bpdm.opensearch.alias")
        val dataBase = postgreSQLContainer.getDatabaseName()
        bpdmPoolContainer.withEnv(
            "spring.datasource.url", "jdbc:postgresql://${postgresNetworkAlias}:5432/${dataBase}?loggerLevel=OFF"
        )
            .withEnv("bpdm.opensearch.host", openSearchNetworkAlias)
            .withEnv(
                "pdm.opensearch.port",
                OpenSearchContextInitializer.OPENSEARCH_PORT.toString()
            )
            .withEnv("bpdm.opensearch.scheme", "http")
            .withEnv(
                "spring.datasource.username", postgreSQLContainer.username
            )
            .withEnv(
                "spring.datasource.password", postgreSQLContainer.password
            ).start()

        println()
        TestPropertyValues.of(
            "bpdm.poolClient.port=${bpdmPoolContainer.getMappedPort(8080)}",
        ).applyTo(applicationContext.environment)

    }
}