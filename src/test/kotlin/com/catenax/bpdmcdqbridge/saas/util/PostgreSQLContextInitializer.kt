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

import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.Network
import org.testcontainers.containers.PostgreSQLContainer

/**
 * When used on a spring boot test, starts a singleton postgres db container that is shared between all integration tests.
 */


class PostgreSQLContextInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    companion object {

        const val postgresAlias = "bpdm-postgres"
        val postgreSQLContainer = PostgreSQLContainer("postgres:13.2")
            .withEnv("cluster.name", "cdqbridge")
            .withEnv("node.name", "bpdm-postgres")
            .withAccessToHost(true)
            .withNetwork(Network.SHARED)
            .withNetworkAliases( postgresAlias)



    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {


        postgreSQLContainer.start()
        TestPropertyValues.of(
            "bpdm.datasource.alias=${postgresAlias}",
            "spring.datasource.url=${postgreSQLContainer.jdbcUrl}",
            "spring.datasource.username=${postgreSQLContainer.username}",
            "spring.datasource.password=${postgreSQLContainer.password}",
        ).applyTo(applicationContext.environment)
    }
}