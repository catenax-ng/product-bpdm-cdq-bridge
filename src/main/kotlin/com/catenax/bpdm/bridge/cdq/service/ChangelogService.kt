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

import com.catenax.bpdm.bridge.cdq.dto.BpnResponse
import com.catenax.bpdm.bridge.cdq.entity.SyncRecord
import mu.KotlinLogging
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.gate.api.client.GateClientImpl
import org.eclipse.tractusx.bpdm.gate.api.model.AddressGateInputResponse
import org.eclipse.tractusx.bpdm.gate.api.model.LegalEntityGateInputResponse
import org.eclipse.tractusx.bpdm.gate.api.model.SiteGateInputResponse
import org.eclipse.tractusx.bpdm.gate.api.model.response.LsaType
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
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
        val record = syncRecordService.setSynchronizationStart(SyncRecord.BridgeSyncType.GATE_IMPORT)
        try {
            importPaginated(record)
        } catch (e: Exception) {
            logger.error(e) { "Exception encountered on SaaS import" }
            syncRecordService.setSynchronizationError(
                SyncRecord.BridgeSyncType.GATE_IMPORT,
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

        val groupedChangelogList = changelogList.groupBy { it.businessPartnerType }

        val bpnCollection = ArrayList<BpnResponse>()
        groupedChangelogList.forEach { (businessPartnerType, changelogEntries) ->
            val externalIds = changelogEntries.map { it.externalId }
            val bpns = fetchBpnBasedOnChangeLogEntries(externalIds, businessPartnerType)
            bpnCollection.addAll(bpns)
        }

        upsertBpnOnSaas(bpnCollection)

        syncRecordService.setSynchronizationSuccess(SyncRecord.BridgeSyncType.CHANGELOG_IMPORT)
    }

    private fun fetchBpnBasedOnChangeLogEntries(
        externalIds: List<String>,
        businessPartnerType: LsaType
    ): List<BpnResponse> {
        val resultList = mutableListOf<BpnResponse>()
        var currentPage = 0
        var totalPages: Int
        do {
            val pageResponse: Page<*> = PageImpl(emptyList<Any>())
//            = when (businessPartnerType) {
//                //TODO methods on gateClient need to be created
//                LsaType.Address -> gateClient.addresses().getAddressesByExternalIds(PaginationRequest(page = currentPage), externalIds)
//                LsaType.LegalEntity -> gateClient.legalEntities().getLegalEntitiesByExternalIds(PaginationRequest(page = currentPage), externalIds)
//                else -> gateClient.sites().getSitesByExternalIds(PaginationRequest(page = currentPage), externalIds)
//            }

            totalPages = pageResponse.totalPages

            pageResponse.content.forEach { item ->
                val bpnResponse = when (businessPartnerType) {
                    LsaType.Address -> BpnResponse.AddressResponse(item as AddressGateInputResponse)
                    LsaType.LegalEntity -> BpnResponse.LegalEntityResponse(item as LegalEntityGateInputResponse)
                    else -> BpnResponse.SiteResponse(item as SiteGateInputResponse)
                }
                resultList.add(bpnResponse)
            }

            currentPage++
        } while (currentPage < totalPages)

        return resultList
    }

    private fun upsertBpnOnSaas(bpn: ArrayList<BpnResponse>) {
        // Not implemented yet
    }

}