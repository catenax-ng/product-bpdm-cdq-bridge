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
import org.eclipse.tractusx.bpdm.pool.api.model.ImportIdEntry
import org.springdoc.core.annotations.ParameterObject
import org.springframework.core.env.Environment
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/saas")
class SaasController(
    val environment: Environment
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
                description = "On malformed requests or exceeding the request size of "
            ),
        ]
    )
    @GetMapping("/identifier-mappings")
    fun getImportEntries(@ParameterObject paginationRequest: PaginationRequest): PageResponse<ImportIdEntry> {
        return PageResponse(0, 1, 1, 1, emptyList())
    }

    @Operation(summary = "Throw a not implemented error")
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "500", description = "Not implemented error")
        ]
    )
    @GetMapping("/endpoint")
    fun throwNotImplementedError(): String {
        throw NotImplementedError("This endpoint is not implemented yet.")
    }

}