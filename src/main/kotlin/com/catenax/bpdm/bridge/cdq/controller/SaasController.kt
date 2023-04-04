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


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.common.dto.response.PageResponse
import org.eclipse.tractusx.bpdm.pool.api.client.PoolClientImpl
import org.eclipse.tractusx.bpdm.pool.api.model.request.AddressPropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.LegalEntityPropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.request.SitePropertiesSearchRequest
import org.eclipse.tractusx.bpdm.pool.api.model.response.LegalEntityMatchResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saas")
class SaasController(val poolClient: PoolClientImpl) {

    @Operation(summary = "Throw a not implemented error")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "500", description = "Not implemented error")
        ]
    )
    @GetMapping("/endpoint")
    fun throwNotImplementedError(): PageResponse<LegalEntityMatchResponse> {
        return poolClient.legalEntities().getLegalEntities(
            LegalEntityPropertiesSearchRequest.EmptySearchRequest,
            AddressPropertiesSearchRequest.EmptySearchRequest, SitePropertiesSearchRequest.EmptySearchRequest,
            PaginationRequest()
        )

    }

}