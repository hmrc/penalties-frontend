/*
 * Copyright 2021 HM Revenue & Customs
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

package models.penalty

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class PaymentPeriodSpec extends AnyWordSpec with Matchers {
  val paymentPeriodModelAsJson: JsValue = Json.parse(
    """
      |{
      | "startDate": "2020-01-01T13:00:00.123",
      | "endDate": "2020-01-01T13:00:00.123",
      | "dueDate": "2020-01-01T13:00:00.123",
      | "paymentStatus": "PAID"
      |}
      |""".stripMargin)

  val paymentPeriodModel: PaymentPeriod = PaymentPeriod(
    startDate = LocalDateTime.of(2020,1,1,13,0,0).plus(123, ChronoUnit.MILLIS),
    endDate = LocalDateTime.of(2020,1,1,13,0,0).plus(123, ChronoUnit.MILLIS),
    dueDate = LocalDateTime.of(2020,1,1,13,0,0).plus(123, ChronoUnit.MILLIS),
    paymentStatus = PaymentStatusEnum.Paid
  )

  "PaymentPeriod" should {
    "be writeable to JSON" in {
      val result = Json.toJson(paymentPeriodModel)
      result shouldBe paymentPeriodModelAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(paymentPeriodModelAsJson)(PaymentPeriod.format)
      result.isSuccess shouldBe true
      result.get shouldBe paymentPeriodModel
    }
  }
}
