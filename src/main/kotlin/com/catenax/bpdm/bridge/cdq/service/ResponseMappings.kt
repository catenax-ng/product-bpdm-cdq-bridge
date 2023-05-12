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
import org.eclipse.tractusx.bpdm.common.dto.response.PageResponse
import org.eclipse.tractusx.bpdm.pool.api.model.SyncType
import org.eclipse.tractusx.bpdm.pool.api.model.response.SyncResponse
import org.springframework.data.domain.Page


fun <S, T> Page<S>.toDto(dtoContent: Collection<T>): PageResponse<T> {
    return PageResponse(this.totalElements, this.totalPages, this.number, this.numberOfElements, dtoContent)
}


fun SyncRecord.toDto(): SyncResponse {
    return SyncResponse(SyncType.valueOf(type.toString()), status, count, progress, errorDetails, startedAt, finishedAt)
}
