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

import com.catenax.bpdm.bridge.cdq.config.SaasAdapterConfigProperties
import com.catenax.bpdm.bridge.cdq.exception.SaasRequestException
import com.fasterxml.jackson.databind.ObjectMapper
import org.eclipse.tractusx.bpdm.common.dto.saas.*
import org.eclipse.tractusx.bpdm.common.service.SaasMappings
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Instant
import java.time.format.DateTimeFormatter

private const val BUSINESS_PARTNER_PATH = "/businesspartners"
private const val RELATIONS_PATH = "/relations"
private const val DELETE_RELATIONS_PATH = "$RELATIONS_PATH/delete"
const val PARENT_RELATION_TYPE_KEY = "PARENT"

@Service
class SaasClient(
    private val webClient: WebClient,
    private val adapterProperties: SaasAdapterConfigProperties,
    private val objectMapper: ObjectMapper
) {


    fun readBusinessPartners(modifiedAfter: Instant, startAfter: String?): PagedResponseSaas<BusinessPartnerSaas> {
        return webClient
            .get()
            .uri { builder ->
                builder
                    .path(adapterProperties.readBusinessPartnerUrl)
                    .queryParam("modifiedAfter", toModifiedAfterFormat(modifiedAfter))
                    .queryParam("limit", adapterProperties.importLimit)
                    .queryParam("datasource", adapterProperties.datasource)
                    .queryParam("featuresOn", "USE_NEXT_START_AFTER", "FETCH_RELATIONS")
                if (startAfter != null) builder.queryParam("startAfter", startAfter)
                builder.build()
            }
            .retrieve()
            .bodyToMono<PagedResponseSaas<BusinessPartnerSaas>>()
            .block()!!
    }

    fun readBusinessPartnersByExternalIds(idValues: Collection<String>): PagedResponseSaas<BusinessPartnerSaas> {
        if (idValues.isEmpty()) return PagedResponseSaas(limit = 0, total = 0, values = emptyList())

        return webClient
            .get()
            .uri { builder ->
                builder
                    .path(adapterProperties.readBusinessPartnerUrl)
                    .queryParam("limit", idValues.size)
                    .queryParam("datasource", adapterProperties.datasource)
                    .queryParam("featuresOn", "USE_NEXT_START_AFTER", "FETCH_RELATIONS")
                    .queryParam("externalId", idValues.joinToString(","))
                builder.build()
            }
            .retrieve()
            .bodyToMono<PagedResponseSaas<BusinessPartnerSaas>>()
            .block()!!
    }

    fun upsertBpnm(bpnList: List<BusinessPartnerSaas>) {
        return upsertBusinessPartners(bpnList)
    }

    fun deleteParentRelations(businessPartners: Collection<BusinessPartnerSaas>) {
        val relationsToDelete = businessPartners
            .flatMap { businessPartner ->
                businessPartner.relations
                    .filter { it.type?.technicalKey == PARENT_RELATION_TYPE_KEY }
                    .filter { it.endNode == businessPartner.externalId }
            }
            .map { SaasMappings.toRelationToDelete(it) }
        if (relationsToDelete.isNotEmpty()) {
            deleteRelations(relationsToDelete)
        }
    }

    fun deleteRelations(relations: Collection<DeleteRelationsRequestSaas.RelationToDeleteSaas>) {
        try {
            webClient
                .post()
                .uri(adapterProperties.dataExchangeApiUrl + DELETE_RELATIONS_PATH)
                .bodyValue(objectMapper.writeValueAsString(DeleteRelationsRequestSaas(relations)))
                .retrieve()
                .bodyToMono<DeleteRelationsResponseSaas>()
                .block()!!
        } catch (e: Exception) {
            throw SaasRequestException("Delete relations request failed.", e)
        }
    }

    fun getAddresses(limit: Int? = null, startAfter: String? = null, externalIds: Collection<String>? = null) =
        getBusinessPartners(
            limit,
            startAfter,
            externalIds,
            adapterProperties.addressType,
            listOf("USE_NEXT_START_AFTER", "FETCH_RELATIONS")
        )


    fun getBusinessPartners(
        limit: Int? = null,
        startAfter: String? = null,
        externalIds: Collection<String>? = null,
        type: String? = null,
        featuresOn: Collection<String>? = null
    ): PagedResponseSaas<BusinessPartnerSaas> {
        val partnerCollection = try {
            webClient
                .get()
                .uri { builder ->
                    builder
                        .path(adapterProperties.dataExchangeApiUrl + BUSINESS_PARTNER_PATH)
                        .queryParam("dataSource", adapterProperties.datasource)
                    if (type != null) builder.queryParam("typeTechnicalKeys", type)
                    if (startAfter != null) builder.queryParam("startAfter", startAfter)
                    if (limit != null) builder.queryParam("limit", limit)
                    if (!featuresOn.isNullOrEmpty()) builder.queryParam("featuresOn", featuresOn.joinToString(","))
                    if (!externalIds.isNullOrEmpty()) builder.queryParam("externalId", externalIds.joinToString(","))
                    builder.build()
                }
                .retrieve()
                .bodyToMono<PagedResponseSaas<BusinessPartnerSaas>>()
                .block()!!
        } catch (e: Exception) {
            e.printStackTrace()
            throw SaasRequestException("Get business partners request failed.", e)
        }
        return partnerCollection
    }

    fun getSites(limit: Int? = null, startAfter: String? = null, externalIds: Collection<String>? = null) =
        getBusinessPartners(
            limit,
            startAfter,
            externalIds,
            adapterProperties.siteType,
            listOf("USE_NEXT_START_AFTER", "FETCH_RELATIONS")
        )

    fun upsertSiteRelations(relations: Collection<SiteLegalEntityRelation>) {
        val relationsSaas = relations.map {
            RelationSaas(
                startNode = it.legalEntityExternalId,
                startNodeDataSource = adapterProperties.datasource,
                endNode = it.siteExternalId,
                endNodeDataSource = adapterProperties.datasource,
                type = TypeKeyNameSaas(technicalKey = PARENT_RELATION_TYPE_KEY)
            )
        }.toList()
        upsertBusinessPartnerRelations(relationsSaas)
    }

    fun upsertAddressRelations(
        legalEntityRelations: Collection<AddressLegalEntityRelation>,
        siteRelations: Collection<AddressSiteRelation>
    ) {
        val legalEntityRelationsSaas = legalEntityRelations.map {
            RelationSaas(
                startNode = it.legalEntityExternalId,
                startNodeDataSource = adapterProperties.datasource,
                endNode = it.addressExternalId,
                endNodeDataSource = adapterProperties.datasource,
                type = TypeKeyNameSaas(technicalKey = PARENT_RELATION_TYPE_KEY)
            )
        }.toList()
        val siteRelationsSaas = siteRelations.map {
            RelationSaas(
                startNode = it.siteExternalId,
                startNodeDataSource = adapterProperties.datasource,
                endNode = it.addressExternalId,
                endNodeDataSource = adapterProperties.datasource,
                type = TypeKeyNameSaas(technicalKey = PARENT_RELATION_TYPE_KEY)
            )
        }.toList()
        upsertBusinessPartnerRelations(legalEntityRelationsSaas.plus(siteRelationsSaas))
    }

    private fun upsertBusinessPartnerRelations(relations: Collection<RelationSaas>) {
        val upsertRelationsRequest = UpsertRelationsRequestSaas(relations)
        val upsertResponse = try {
            webClient
                .put()
                .uri(adapterProperties.dataExchangeApiUrl + RELATIONS_PATH)
                .bodyValue(objectMapper.writeValueAsString(upsertRelationsRequest))
                .retrieve()
                .bodyToMono<UpsertRelationsResponseSaas>()
                .block()!!
        } catch (e: Exception) {
            throw SaasRequestException("Upsert business partner relations request failed.", e)
        }

        if (upsertResponse.failures.isNotEmpty() || upsertResponse.numberOfFailed > 0) {
            throw SaasRequestException("Upsert business partner relations request failed for some relations.")
        }
    }


    private fun upsertBusinessPartners(businessPartners: Collection<BusinessPartnerSaas>) {
        val upsertRequest =
            UpsertRequest(
                adapterProperties.datasource,
                businessPartners,
                listOf(
                    UpsertRequest.SaasFeatures.UPSERT_BY_EXTERNAL_ID,
                    UpsertRequest.SaasFeatures.API_ERROR_ON_FAILURES
                )
            )

        try {
            webClient
                .put()
                .uri(adapterProperties.dataExchangeApiUrl + BUSINESS_PARTNER_PATH)
                .bodyValue(objectMapper.writeValueAsString(upsertRequest))
                .retrieve()
                .bodyToMono<UpsertResponse>()
                .block()!!
        } catch (e: Exception) {
            throw SaasRequestException("Upsert business partners request failed.", e)
        }
    }

    private fun toModifiedAfterFormat(dateTime: Instant): String {
        return DateTimeFormatter.ISO_INSTANT.format(dateTime)
    }

    data class SiteLegalEntityRelation(
        val siteExternalId: String,
        val legalEntityExternalId: String
    )

    data class AddressLegalEntityRelation(
        val addressExternalId: String,
        val legalEntityExternalId: String
    )

    data class AddressSiteRelation(
        val addressExternalId: String,
        val siteExternalId: String
    )

}