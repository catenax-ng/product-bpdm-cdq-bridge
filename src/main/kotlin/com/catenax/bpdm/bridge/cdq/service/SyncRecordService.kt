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
import com.catenax.bpdm.bridge.cdq.repository.SyncRecordRepository
import mu.KotlinLogging
import org.eclipse.tractusx.bpdm.pool.api.model.SyncStatus
import org.eclipse.tractusx.bpdm.pool.api.model.SyncType
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Uses transaction isolation level "serializable" in order to make sure that in case of parallel execution on different spring boot instances,
 * only one instance can get the sync record and make changes like setting it to "running" state at the same time.
 */
@Service
class SyncRecordService(
    private val syncRecordRepository: SyncRecordRepository
) {
    companion object {
        val syncStartTime = LocalDateTime.of(2000, 1, 1, 0, 0).toInstant(ZoneOffset.UTC)
    }

    private val logger = KotlinLogging.logger { }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    fun getOrCreateRecord(type: SyncType): SyncRecord {
        return syncRecordRepository.findByType(type) ?: run {
            logger.info { "Create new sync record entry for type $type" }
            val newEntry = SyncRecord(
                type,
                SyncStatus.NOT_SYNCED,
                syncStartTime
            )
            syncRecordRepository.save(newEntry)
        }
    }


}