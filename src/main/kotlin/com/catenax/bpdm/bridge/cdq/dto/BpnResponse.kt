package com.catenax.bpdm.bridge.cdq.dto

import org.eclipse.tractusx.bpdm.gate.api.model.AddressGateInputResponse
import org.eclipse.tractusx.bpdm.gate.api.model.LegalEntityGateInputResponse
import org.eclipse.tractusx.bpdm.gate.api.model.SiteGateInputResponse

sealed class BpnResponse {
    data class AddressResponse(val address: AddressGateInputResponse) : BpnResponse()
    data class LegalEntityResponse(val legalEntity: LegalEntityGateInputResponse) : BpnResponse()
    data class SiteResponse(val site: SiteGateInputResponse) : BpnResponse()
}
