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


import com.catenax.bpdm.bridge.cdq.repository.ImportEntryRepository
import org.eclipse.tractusx.bpdm.common.dto.request.PaginationRequest
import org.eclipse.tractusx.bpdm.common.dto.response.PageResponse
import org.eclipse.tractusx.bpdm.pool.api.model.ImportIdEntry
import org.eclipse.tractusx.bpdm.pool.api.model.SyncType
import org.eclipse.tractusx.bpdm.pool.api.model.response.ImportIdMappingResponse
import org.eclipse.tractusx.bpdm.pool.api.model.response.SyncResponse
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

/**
 * Starts the partner import either blocking or non-blocking
 */
@Service
class ImportStarterService(
    private val syncRecordService: SyncRecordService,
    private val importEntryRepository: ImportEntryRepository
) {


    /**
     * Paginate over import entries by [paginationRequest]
     */
    fun getImportIdEntries(paginationRequest: PaginationRequest): PageResponse<ImportIdEntry> {
        val entriesPage = importEntryRepository.findAll(PageRequest.of(paginationRequest.page, paginationRequest.size))
        return entriesPage.toDto(entriesPage.content.map { ImportIdEntry(it.importIdentifier, it.bpn) })
    }


    fun getImportIdEntries(importIdentifiers: Collection<String>): ImportIdMappingResponse {
        val foundEntries = importEntryRepository.findByImportIdentifierIn(importIdentifiers)
            .map { ImportIdEntry(it.importIdentifier, it.bpn) }
        val missingEntries = importIdentifiers.minus(foundEntries.map { it.importId }.toSet())

        return ImportIdMappingResponse(foundEntries, missingEntries)
    }

    fun getImportStatus(): SyncResponse {
        return syncRecordService.getOrCreateRecord(SyncType.SAAS_IMPORT).toDto()
    }


}