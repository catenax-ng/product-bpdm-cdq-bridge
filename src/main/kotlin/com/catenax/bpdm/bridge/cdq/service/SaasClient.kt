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
import org.eclipse.tractusx.bpdm.common.dto.saas.BusinessPartnerSaas
import org.eclipse.tractusx.bpdm.common.dto.saas.PagedResponseSaas
import org.eclipse.tractusx.bpdm.common.dto.saas.UpsertRequest
import org.eclipse.tractusx.bpdm.common.dto.saas.UpsertResponse
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import java.time.Instant
import java.time.format.DateTimeFormatter

private const val BUSINESS_PARTNER_PATH = "/businesspartners"
private const val FETCH_BUSINESS_PARTNER_PATH = "$BUSINESS_PARTNER_PATH/fetch"

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

}