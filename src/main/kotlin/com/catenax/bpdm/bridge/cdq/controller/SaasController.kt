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

package com.catenax.bpdm.bridge.cdq.controller


import com.catenax.bpdm.bridge.cdq.config.SaasAdapterConfigProperties
import com.catenax.bpdm.bridge.cdq.exception.BpdmRequestSizeException
import com.catenax.bpdm.bridge.cdq.service.ImportStarterService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.common.dto.response.PageResponse
import org.eclipse.tractusx.bpdm.pool.api.client.PoolClientImpl
import org.eclipse.tractusx.bpdm.pool.api.model.ImportIdEntry
import org.eclipse.tractusx.bpdm.pool.api.model.request.AddressPropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.ImportIdFilterRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.LegalEntityPropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.SitePropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.response.ImportIdMappingResponse
import org.eclipse.tractusx.bpdm.pool.api.model.response.LegalEntityMatchResponse
import org.eclipse.tractusx.bpdm.pool.api.model.response.SyncResponse
import org.springdoc.core.annotations.ParameterObject
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saas")
class SaasController(
    private val poolClient: PoolClientImpl,
    private val partnerImportService: ImportStarterService,
    private val adapterConfigProperties: SaasAdapterConfigProperties
) {


    @Operation(
        summary = "Paginate Identifier Mappings by CX-Pool Identifiers",
        description = "Paginate through all CX-Pool Identifier and Business Partner Number mappings."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The found import identifier mappings"),
            ApiResponse(
                responseCode = "400",
                description = "On malformed requests or exceeding the request size of \${bpdm.saas.request-size-limit}"
            ),
        ]
    )
    @GetMapping("/identifier-mappings")
    fun getImportEntries(@ParameterObject paginationRequest: PaginationRequest): PageResponse<ImportIdEntry> {
        if (paginationRequest.size > adapterConfigProperties.requestSizeLimit)
            throw BpdmRequestSizeException(paginationRequest.size, adapterConfigProperties.requestSizeLimit)
        return partnerImportService.getImportIdEntries(paginationRequest)
    }

    @Operation(
        summary = "Filter Identifier Mappings by CX-Pool Identifiers",
        description = "Specify a range of CX-Pool Identifiers to get the corresponding mapping to their Business Partner Numbers"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "The found import identifier mappings"),
            ApiResponse(
                responseCode = "400",
                description = "On malformed requests or exceeding the request size of \${bpdm.saas.request-size-limit}"
            ),
        ]
    )
    @PostMapping("/identifier-mappings/filter")
    fun getImportEntries(@RequestBody importIdFilterRequest: ImportIdFilterRequest): ImportIdMappingResponse {
        if (importIdFilterRequest.importIdentifiers.size > adapterConfigProperties.requestSizeLimit)
            throw BpdmRequestSizeException(
                importIdFilterRequest.importIdentifiers.size,
                adapterConfigProperties.requestSizeLimit
            )
        return partnerImportService.getImportIdEntries(importIdFilterRequest.importIdentifiers)
    }

    @Operation(
        summary = "Fetch information about the SaaS synchronization",
        description = "Fetch information about the latest import (either ongoing or already finished)"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Import information found"),
            ApiResponse(
                responseCode = "500",
                description = "Fetching failed (no connection to database)",
                content = [Content()]
            )
        ]
    )
    @GetMapping("/business-partner/sync")
    fun getSyncStatus(): SyncResponse {
        return partnerImportService.getImportStatus()
    }



    @Operation(
        summary = "Import new business partner records from SaaS",
        description = "Triggers an asynchronous import of new business partner records from SaaS. " +
                "A SaaS record counts as new when it does not have a BPN and the BPDM service does not already have a record with the same SaaS ID. " +
                "This import only regards records with a modifiedAfter timestamp since the last import."
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "Import successfully started"),
            ApiResponse(responseCode = "409", description = "Import already running"),
            ApiResponse(
                responseCode = "500",
                description = "Import couldn't start to unexpected error",
                content = [Content()]
            )
        ]
    )
    @PostMapping("/business-partner/sync")
    fun importBusinessPartners(): SyncResponse {
        return partnerImportService.importAsync()
    }

}