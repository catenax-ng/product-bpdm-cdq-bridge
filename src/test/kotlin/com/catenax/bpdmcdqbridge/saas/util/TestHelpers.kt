package com.catenax.bpdmcdqbridge.saas.util

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.assertj.core.api.Assertions
import org.assertj.core.api.RecursiveComparisonAssert
import org.eclipse.tractusx.bpdm.pool.api.client.PoolClientImpl
import org.eclipse.tractusx.bpdm.pool.api.model.SyncStatus
import org.eclipse.tractusx.bpdm.pool.api.model.response.SyncResponse
import org.springframework.stereotype.Component
import org.springframework.test.web.reactive.server.WebTestClient
import java.time.Instant

private const val ASYNC_TIMEOUT_IN_MS: Long = 5 * 1000 //5 seconds
private const val ASYNC_CHECK_INTERVAL_IN_MS: Long = 200
private const val BPDM_DB_SCHEMA_NAME: String = "bpdmcdq"
private const val BPDM_DB_SCHEMA_POOL: String = "bpdm"

@Component
class TestHelpers(
    entityManagerFactory: EntityManagerFactory,
    private val poolClient: PoolClientImpl
) {

    val em: EntityManager = entityManagerFactory.createEntityManager()

    fun truncateDbTables() {
        em.transaction.begin()

        em.createNativeQuery(
            """
            DO $$ DECLARE table_names RECORD;
            BEGIN
                FOR table_names IN SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema='$BPDM_DB_SCHEMA_NAME'
                    AND table_name NOT IN ('flyway_schema_history') 
                LOOP 
                    EXECUTE format('TRUNCATE TABLE $BPDM_DB_SCHEMA_NAME.%I CONTINUE IDENTITY CASCADE;', table_names.table_name);
                END LOOP;
            END $$;
        """.trimIndent()
        ).executeUpdate()

        em.transaction.commit()

        em.transaction.begin()

        em.createNativeQuery(
            """
            DO $$ DECLARE table_names RECORD;
            BEGIN
                FOR table_names IN SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema='$BPDM_DB_SCHEMA_POOL'
                    AND table_name NOT IN ('flyway_schema_history') 
                LOOP 
                    EXECUTE format('TRUNCATE TABLE $BPDM_DB_SCHEMA_POOL.%I CONTINUE IDENTITY CASCADE;', table_names.table_name);
                END LOOP;
            END $$;
        """.trimIndent()
        ).executeUpdate()

        em.transaction.commit()
    }

    fun createTestMetadata() {

        poolClient.metadata().createLegalForm(RequestValues.legalForm1)
        poolClient.metadata()
        poolClient.metadata().createLegalForm(RequestValues.legalForm2)
        poolClient.metadata().createLegalForm(RequestValues.legalForm3)

        poolClient.metadata().createIdentifierType( RequestValues.identifierType1)
        poolClient.metadata().createIdentifierType( RequestValues.identifierType2)
        poolClient.metadata().createIdentifierType( RequestValues.identifierType3)

        poolClient.metadata().createIssuingBody(RequestValues.issuingBody1)
        poolClient.metadata().createIssuingBody(RequestValues.issuingBody2)
        poolClient.metadata().createIssuingBody(RequestValues.issuingBody3)

        poolClient.metadata().createIdentifierStatus(RequestValues.identifierStatus1)
        poolClient.metadata().createIdentifierStatus(RequestValues.identifierStatus2)
        poolClient.metadata().createIdentifierStatus(RequestValues.identifierStatus3)


    }

    fun startSyncAndAwaitSuccess(client: WebTestClient, syncPath: String): SyncResponse {
        return startSyncAndAwaitResult(client, syncPath, SyncStatus.SUCCESS)
    }

    fun startSyncAndAwaitError(client: WebTestClient, syncPath: String): SyncResponse {
        return startSyncAndAwaitResult(client, syncPath, SyncStatus.ERROR)
    }

    private fun startSyncAndAwaitResult(client: WebTestClient, syncPath: String, status: SyncStatus): SyncResponse {

        client.invokePostEndpointWithoutResponse(syncPath)
        //check for async import to finish several times
        val timeOutAt = Instant.now().plusMillis(ASYNC_TIMEOUT_IN_MS)
        var syncResponse: SyncResponse
        do {
            Thread.sleep(ASYNC_CHECK_INTERVAL_IN_MS)

            syncResponse = client.invokeGetEndpoint(syncPath)

            if (syncResponse.status == status)
                break

        } while (Instant.now().isBefore(timeOutAt))

        Assertions.assertThat(syncResponse.status).isEqualTo(status)

        return syncResponse
    }

    fun <T> assertRecursively(actual: T): RecursiveComparisonAssert<*> {
        return Assertions.assertThat(actual)
            .usingRecursiveComparison()
            .ignoringCollectionOrder()
            .ignoringAllOverriddenEquals()
    }

}