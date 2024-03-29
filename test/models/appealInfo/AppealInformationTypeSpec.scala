/*
 * Copyright 2023 HM Revenue & Customs
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

package models.appealInfo

import base.SpecBase
import play.api.libs.json.Json

class AppealInformationTypeSpec extends SpecBase {

  val appealInfoAsJson = Json.parse(
    """
      |{
      |    "appealStatus": "99",
      |    "appealLevel": "01"
      |}
      |""".stripMargin)

  val appealInfoAsModel = AppealInformationType(
    appealStatus = Some(AppealStatusEnum.Unappealable),
    appealLevel = Some(AppealLevelEnum.HMRC)
  )

  "AppealInformationType" should {
    "be readable from JSON" in {
      val result = Json.fromJson(appealInfoAsJson)(AppealInformationType.format)
      result.isSuccess shouldBe true
      result.get shouldBe appealInfoAsModel
    }

    "be writable to JSON" in {
      val result = Json.toJson(appealInfoAsModel)(AppealInformationType.format)
      result shouldBe appealInfoAsJson
    }
  }

}
