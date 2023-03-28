package com.catenax.bpdm.bridge.cdq

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class BpdmCdqBridgeApplication

fun main(args: Array<String>) {
	runApplication<com.catenax.bpdm.bridge.cdq.BpdmCdqBridgeApplication>(*args)
}
