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

package com.catenax.bpdm.bridge.cdq.service

import mu.KotlinLogging
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.gate.api.client.GateClientImpl
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

/**
 * Starts the partner import either blocking or non-blocking
 */
@Service
class ChangelogService(
    val gateClient: GateClientImpl
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Periodically check business partner changelog
     */
    @Scheduled(cron = "\${bpdm.change-log.import-scheduler-cron-expr:-}", zone = "UTC")
    fun import() {
        val changelogList = gateClient.changelog().getChangelogEntriesLsaType(
            paginationRequest = PaginationRequest(),
            fromTime = Instant.now().minus(Duration.ofDays(10)),
            lsaType = null
        ).content
        println(changelogList.toString())
    }


}