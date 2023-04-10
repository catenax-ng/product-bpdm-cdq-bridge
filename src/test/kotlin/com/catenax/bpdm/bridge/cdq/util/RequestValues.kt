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

package com.catenax.bpdm.bridge.cdq.util

import com.neovisionaries.i18n.LanguageCode
import org.eclipse.tractusx.bpdm.common.dto.response.type.TypeKeyNameDto
import org.eclipse.tractusx.bpdm.common.dto.response.type.TypeKeyNameUrlDto
import org.eclipse.tractusx.bpdm.pool.api.model.request.LegalFormRequest

object RequestValues {


    val identifierType1 = TypeKeyNameUrlDto(
        CommonValues.identifierTypeTechnicalKey1,
        CommonValues.identifierTypeName1,
        CommonValues.identifierTypeUrl1
    )
    val identifierType2 = TypeKeyNameUrlDto(
        CommonValues.identifierTypeTechnicalKey2,
        CommonValues.identifierTypeName2,
        CommonValues.identifierTypeUrl2
    )
    val identifierType3 = TypeKeyNameUrlDto(
        CommonValues.identifierTypeTechnicalKey3,
        CommonValues.identifierTypeName3,
        CommonValues.identifierTypeUrl3
    )

    val identifierStatus1 = TypeKeyNameDto(CommonValues.identifierStatusKey1, CommonValues.identifierStatusName1)
    val identifierStatus2 = TypeKeyNameDto(CommonValues.identifierStatusKey2, CommonValues.identifierStatusName2)
    val identifierStatus3 = TypeKeyNameDto(CommonValues.identifierStatusKey3, CommonValues.identifierStatusName3)

    val issuingBody1 = TypeKeyNameUrlDto(
        CommonValues.issuingBodyKey1,
        CommonValues.issuingBodyName1,
        CommonValues.issuingBodyUrl1
    )
    val issuingBody2 = TypeKeyNameUrlDto(
        CommonValues.issuingBodyKey2,
        CommonValues.issuingBodyName2,
        CommonValues.issuingBodyUrl2
    )
    val issuingBody3 = TypeKeyNameUrlDto(
        CommonValues.issuingBodyKey3,
        CommonValues.issuingBodyName3,
        CommonValues.issuingBodyUrl3
    )


    val legalForm1 = LegalFormRequest(
        CommonValues.legalFormTechnicalKey1,
        CommonValues.legalFormName1,
        CommonValues.legalFormUrl1,
        CommonValues.legalFormAbbreviation1,
        LanguageCode.en
    )
    val legalForm2 = LegalFormRequest(
        CommonValues.legalFormTechnicalKey2,
        CommonValues.legalFormName2,
        CommonValues.legalFormUrl2,
        CommonValues.legalFormAbbreviation2,
        LanguageCode.de
    )
    val legalForm3 = LegalFormRequest(
        CommonValues.legalFormTechnicalKey3,
        CommonValues.legalFormName3,
        CommonValues.legalFormUrl3,
        CommonValues.legalFormAbbreviation3,
        LanguageCode.zh
    )


}