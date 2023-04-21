package com.catenax.bpdm.bridge.cdq

import com.catenax.bpdm.bridge.cdq.util.PostgreSQLContextInitializer
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(initializers = [PostgreSQLContextInitializer::class])
class BpdmCdqBridgeApplicationTests {

	@Test
	fun contextLoads() {
	}

}
