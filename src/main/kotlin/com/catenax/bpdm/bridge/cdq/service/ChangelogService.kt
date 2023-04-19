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
import org.eclipse.tractusx.bpdm.gate.api.model.request.PaginationStartAfterRequest
import org.eclipse.tractusx.bpdm.gate.api.model.response.LsaType
import org.eclipse.tractusx.bpdm.gate.api.model.response.PageStartAfterResponse
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

/**
 * Starts the partner import either blocking or non-blocking
 */
@Service
class ChangelogService(
    val gateClient: GateClientImpl,
    val syncRecordService: SyncRecordService,
    val saasClient: SaasClient,
    val saasRequestMappingService: SaasRequestMappingService
) {

    private val logger = KotlinLogging.logger { }

    /**
     * Periodically check business partner changelog
     */
    @Scheduled(cron = "\${bpdm.change-log.import-scheduler-cron-expr-address:-}", zone = "UTC")
    fun importAddress() {
        importByType(SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_ADDRESS, LsaType.Address)
    }

    @Scheduled(cron = "\${bpdm.change-log.import-scheduler-cron-expr-site:-}", zone = "UTC")
    fun importSite() {
        importByType(SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_SITE, LsaType.Site)
    }

    @Scheduled(cron = "\${bpdm.change-log.import-scheduler-cron-expr-legal-entity:-}", zone = "UTC")
    fun importLegalEntity() {
        importByType(SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_LEGAL_ENTITY, LsaType.LegalEntity)
    }

    private fun importByType(syncType: SyncRecord.BridgeSyncType, lsaType: LsaType) {
        val record = syncRecordService.setSynchronizationStart(syncType)
        try {
            importPaginated(record, lsaType)
            syncRecordService.setSynchronizationSuccess(syncType)
        } catch (e: Exception) {
            logger.error(e) { "Exception encountered on SaaS import" }
            syncRecordService.setSynchronizationError(
                syncType,
                e.message ?: "No Message",
                null // Replace with startAfter value if applicable
            )
        }
    }

    private fun importPaginated(record: SyncRecord, lsaType: LsaType) {
        val changelogList = gateClient.changelog().getChangelogEntriesLsaType(
            paginationRequest = PaginationRequest(),
            fromTime = record.fromTime,
            lsaType = lsaType
        ).content

        val externalIds = changelogList.map { it.externalId }

        val syncType = when (lsaType) {
            LsaType.Address -> SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_ADDRESS
            LsaType.LegalEntity -> SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_LEGAL_ENTITY
            else -> SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_SITE
        }

        val bpnCollection = when (lsaType) {
            LsaType.Address -> {
                fetchBpnBasedOnChangeLogEntries(
                    externalIds, syncType,
                    gateClient.addresses()::getAddressesByExternalIds, BpnResponse::AddressResponse
                )
            }

            LsaType.LegalEntity -> {
                fetchBpnBasedOnChangeLogEntries(
                    externalIds, syncType,
                    gateClient.legalEntities()::getLegalEntitiesByExternalIds, BpnResponse::LegalEntityResponse
                )
            }

            else -> {
                fetchBpnBasedOnChangeLogEntries(
                    externalIds, syncType,
                    gateClient.sites()::getSitesByExternalIds, BpnResponse::SiteResponse
                )
            }
        }

        upsertBpnOnSaas(bpnCollection, syncType)

    }


    private fun <T> fetchBpnBasedOnChangeLogEntries(
        externalIds: List<String>,
        syncType: SyncRecord.BridgeSyncType,
        fetchFunction: (PaginationStartAfterRequest, List<String>) -> PageStartAfterResponse<T>,
        responseMapper: (T) -> BpnResponse
    ): ArrayList<BpnResponse> {
        val resultList = mutableListOf<BpnResponse>()
        var startAfter: String? = null
        var importedCount = 0

        do {
            val pageResponse = fetchFunction(PaginationStartAfterRequest(startAfter = startAfter), externalIds)
            startAfter = pageResponse.nextStartAfter
            val progress = importedCount / pageResponse.total.toFloat()
            syncRecordService.setProgress(syncType, importedCount, progress)

            pageResponse.content.forEach { item ->
                resultList.add(responseMapper(item))
            }

            importedCount += pageResponse.content.size
        } while (startAfter != null)

        return ArrayList(resultList)
    }


    private fun upsertBpnOnSaas(bpn: ArrayList<BpnResponse>, syncType: SyncRecord.BridgeSyncType) {

        val updatedBpnCollection = bpn.map { element ->
            when (element) {
                is BpnResponse.AddressResponse -> saasRequestMappingService.toSaasModel(element.address)
                is BpnResponse.LegalEntityResponse -> saasRequestMappingService.toSaasModel(element.legalEntity)
                is BpnResponse.SiteResponse -> saasRequestMappingService.toSaasModel(element.site)
            }
        }


        saasClient.upsertBpnm(updatedBpnCollection)

        if (syncType == SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_ADDRESS) {
            deleteAddressRelationsAndCreateNewOnes(bpn)
        } else if (syncType == SyncRecord.BridgeSyncType.CHANGELOG_IMPORT_SITE) {
            deleteSiteRelationsAndCreateNewOnes(bpn)
        }


    }

    private fun deleteSiteRelationsAndCreateNewOnes(bpn: ArrayList<BpnResponse>) {
        val sites = bpn.filterIsInstance<BpnResponse.SiteResponse>().map { it.site }
        val siteResponseList = sites.map { saasRequestMappingService.toSaasModel(it) }
        val sitePage = saasClient.getSites(externalIds = siteResponseList.mapNotNull { it.externalId })
        saasClient.deleteParentRelations(sitePage.values)
        val relations = sites.map {
            SaasClient.SiteLegalEntityRelation(
                siteExternalId = it.externalId,
                legalEntityExternalId = it.legalEntityExternalId
            )
        }.toList()
        saasClient.upsertSiteRelations(relations)
    }

    private fun deleteAddressRelationsAndCreateNewOnes(bpn: ArrayList<BpnResponse>) {
        val addresses = bpn.filterIsInstance<BpnResponse.AddressResponse>().map { it.address }
        val addressResponseList = addresses.map { saasRequestMappingService.toSaasModel(it) }
        val addressesPage = saasClient.getAddresses(externalIds = addressResponseList.mapNotNull { it.externalId })
        saasClient.deleteParentRelations(addressesPage.values)

        val legalEntityRelations = addresses.filter {
            it.legalEntityExternalId != null
        }.map {
            SaasClient.AddressLegalEntityRelation(
                addressExternalId = it.externalId,
                legalEntityExternalId = it.legalEntityExternalId!!
            )
        }.toList()
        val siteRelations = addresses.filter {
            it.siteExternalId != null
        }.map {
            SaasClient.AddressSiteRelation(
                addressExternalId = it.externalId,
                siteExternalId = it.siteExternalId!!
            )
        }.toList()
        saasClient.upsertAddressRelations(legalEntityRelations, siteRelations)
    }
}