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


import com.catenax.bpdm.bridge.cdq.entity.SyncRecord
import mu.KotlinLogging
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.gate.api.client.GateClientImpl
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Starts the partner import either blocking or non-blocking
 */
@Service
class ChangelogService(
    val gateClient: GateClientImpl,
    val saasClient: SaasClient,
    val syncRecordService: SyncRecordService,
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Periodically check business partner changelog
     */
    @Scheduled(cron = "\${bpdm.change-log.import-scheduler-cron-expr:-}", zone = "UTC")
    fun import() {
        val record = syncRecordService.setSynchronizationStart(SyncRecord.BridgeSyncType.CHANGELOG_IMPORT)
        try {
            importPaginated(record)
        } catch (e: Exception) {
            logger.error(e) { "Exception encountered on SaaS import" }
            syncRecordService.setSynchronizationError(
                SyncRecord.BridgeSyncType.CHANGELOG_IMPORT,
                e.message ?: "No Message",
                null // Replace with startAfter value if applicable
            )
        }
    }

    private fun importPaginated(record: SyncRecord) {

        val changelogList = gateClient.changelog().getChangelogEntriesLsaType(
            paginationRequest = PaginationRequest(),
            fromTime = record.fromTime,
            lsaType = null
        ).content

        changelogList.forEach { entry ->
            processChangelogEntry(entry.externalId)
        }


    }

    private fun processChangelogEntry(externalId: String) {
        val response = ""// get bpn based on externalId
    }


}