package com.catenax.bpdm.bridge.cdq.entity

import jakarta.persistence.*
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.time.temporal.ChronoUnit

@Entity
@Table(name = "changelog_import_status")
data class ChangelogImportStatus(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "last_sync_date")
    @UpdateTimestamp
    val lastSyncDate: Instant = Instant.now().truncatedTo(ChronoUnit.MICROS)
)
