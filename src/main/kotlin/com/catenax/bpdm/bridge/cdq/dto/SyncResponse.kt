package com.catenax.bpdm.bridge.cdq.dto

import com.catenax.bpdm.bridge.cdq.entity.SyncRecord
import org.eclipse.tractusx.bpdm.pool.api.model.SyncStatus
import java.time.Instant

data class SyncResponse(
    val type: SyncRecord.BridgeSyncType,
    val status: SyncStatus,
    val count: Int = 0,
    val progress: Float = 0f,
    val errorDetails: String? = null,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null
)
