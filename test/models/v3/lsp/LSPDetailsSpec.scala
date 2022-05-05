/*
 * Copyright 2022 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package models.v3.lsp

import base.SpecBase
import models.v3.appealInfo.{AppealInformationType, AppealStatusEnum}
import play.api.libs.json.Json

import java.time.LocalDate

class LSPDetailsSpec extends SpecBase {

  val LSPDetailsAsJson = Json.parse(
    """
      |    {
      |       "penaltyNumber": "12345678901234",
      |       "penaltyOrder": "01",
      |       "penaltyCategory": "P",
      |       "penaltyStatus": "ACTIVE",
      |       "FAPIndicator": "X",
      |       "penaltyCreationDate": "2069-10-30",
      |       "penaltyExpiryDate": "2069-10-30",
      |       "expiryReason": "FAP",
      |       "communicationsDate": "2069-10-30",
      |       "lateSubmissions": [
      |         {
      |           "taxPeriodStartDate": "2069-10-30",
      |           "taxPeriodEndDate": "2069-10-30",
      |           "taxPeriodDueDate": "2069-10-30",
      |           "returnReceiptDate": "2069-10-30",
      |           "taxReturnStatus": "Fulfilled"
      |         }
      |       ],
      |       "appealInformation": [
      |         {
      |           "appealStatus": "99",
      |           "appealLevel": "01"
      |         }
      |       ],
      |       "chargeDueDate": "2069-10-30",
      |       "chargeOutstandingAmount": 200,
      |       "chargeAmount": 200
      |    }
      |""".stripMargin)

  val LSPDetailsAsModel = LSPDetails(
    penaltyNumber = "12345678901234",
    penaltyOrder = "01",
    penaltyCategory = LSPPenaltyCategoryEnum.Point,
    penaltyStatus = LSPPenaltyStatusEnum.Active,
    FAPIndicator = "X",
    penaltyCreationDate = LocalDate.parse("2069-10-30"),
    penaltyExpiryDate = LocalDate.parse("2069-10-30"),
    expiryReason = Some("FAP"),
    communicationsDate = LocalDate.parse("2069-10-30"),
    lateSubmissions = Some(Seq(
      LateSubmission(
        taxPeriodStartDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodEndDate = Some(LocalDate.parse("2069-10-30")),
        taxPeriodDueDate = Some(LocalDate.parse("2069-10-30")),
        returnReceiptDate = Some(LocalDate.parse("2069-10-30")),
        taxReturnStatus = TaxReturnStatusEnum.Fulfilled
      )
    )),
    appealInformation = Some(Seq(
      AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some("01")
      )
    )),
    chargeAmount = Some(200),
    chargeOutstandingAmount = Some(200),
    chargeDueDate = Some(LocalDate.parse("2069-10-30"))
  )

  "LSPDetails" should {
    "be readable from JSON" in {
      val result = Json.fromJson(LSPDetailsAsJson)(LSPDetails.format)
      result.isSuccess shouldBe true
      result.get shouldBe LSPDetailsAsModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(LSPDetailsAsModel)(LSPDetails.format)
      result shouldBe LSPDetailsAsJson
    }
  }

}
