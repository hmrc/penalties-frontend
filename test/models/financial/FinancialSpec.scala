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

package models.financial

import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import play.api.libs.json.{JsValue, Json}

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

class FinancialSpec extends AnyWordSpec with Matchers {
  val financialModelNoCrystalizedNoEstimatedInterestAsJson: JsValue = Json.parse(
    """
      |{
      | "amountDue": 400.12,
      | "outstandingAmountDue": 400.12,
      | "dueDate": "2019-01-31T23:59:59.999"
      |}
      |
      |""".stripMargin)
  val financialModelNoCrystalizedInterestAsJson: JsValue = Json.parse(
    """
      |{
      | "amountDue": 400.12,
      | "outstandingAmountDue": 400.12,
      | "dueDate": "2019-01-31T23:59:59.999",
      | "estimatedInterest": 10.00
      |}
      |
      |""".stripMargin)
  val financialModelNoEstimatedInterestAsJson: JsValue = Json.parse(
    """
      |{
      | "amountDue": 400.12,
      | "outstandingAmountDue": 400.12,
      | "dueDate": "2019-01-31T23:59:59.999",
      | "crystalizedInterest": 10.00
      |}
      |
      |""".stripMargin)
  val financialModelAsJson: JsValue = Json.parse(
    """
      |{
      | "amountDue": 400.12,
      | "outstandingAmountDue": 400.12,
      | "dueDate": "2019-01-31T23:59:59.999",
      | "estimatedInterest": 10.00,
      | "crystalizedInterest": 10.00
      |}
      |
      |""".stripMargin)

  val financialNoCrystalizedNoEstimatedInterestModel: Financial = Financial(
    amountDue = 400.12,
    outstandingAmountDue = 400.12,
    dueDate = LocalDateTime.of(2019, 1, 31, 23, 59, 59).plus(999, ChronoUnit.MILLIS)
  )
  val financialNoEstimatedInterestModel: Financial = Financial(
    amountDue = 400.12,
    outstandingAmountDue = 400.12,
    crystalizedInterest = Some(10.00),
    dueDate = LocalDateTime.of(2019, 1, 31, 23, 59, 59).plus(999, ChronoUnit.MILLIS)
  )
  val financialNoCrystalizedInterestModel: Financial = Financial(
    amountDue = 400.12,
    outstandingAmountDue = 400.12,
    estimatedInterest = Some(10.00),
    dueDate = LocalDateTime.of(2019, 1, 31, 23, 59, 59).plus(999, ChronoUnit.MILLIS)
  )
  val financialModel: Financial = Financial(
    amountDue = 400.12,
    outstandingAmountDue = 400.12,
    estimatedInterest = Some(10.00),
    crystalizedInterest = Some(10.00),
    dueDate = LocalDateTime.of(2019, 1, 31, 23, 59, 59).plus(999, ChronoUnit.MILLIS)
  )

  "Financial" should {
    "be writeable to JSON" in {
      val result = Json.toJson(financialModel)
      result shouldBe financialModelAsJson
    }

    "be readable from JSON" in {
      val result = Json.fromJson(financialModelAsJson)(Financial.format)
      result.isSuccess shouldBe true
      result.get shouldBe financialModel
    }

    "be writeable to JSON without the estimatedInterest" in {
      val result = Json.toJson(financialNoEstimatedInterestModel)
      result shouldBe financialModelNoEstimatedInterestAsJson
    }

    "be readable from JSON without the estimatedInterest" in {
      val result = Json.fromJson(financialModelNoEstimatedInterestAsJson)(Financial.format)
      result.isSuccess shouldBe true
      result.get shouldBe financialNoEstimatedInterestModel
    }
    "be writeable to JSON without the crystalizedInterest" in {
      val result = Json.toJson(financialNoCrystalizedInterestModel)
      result shouldBe financialModelNoCrystalizedInterestAsJson
    }

    "be readable from JSON without the crystalizedInterest" in {
      val result = Json.fromJson(financialModelNoCrystalizedInterestAsJson)(Financial.format)
      result.isSuccess shouldBe true
      result.get shouldBe financialNoCrystalizedInterestModel
    }
    "be writeable to JSON without the estimatedInterest and crystalizedInterest" in {
      val result = Json.toJson(financialNoCrystalizedNoEstimatedInterestModel)
      result shouldBe financialModelNoCrystalizedNoEstimatedInterestAsJson
    }

    "be readable from JSON without the estimatedInterest and crystalizedInterest" in {
      val result = Json.fromJson(financialModelNoCrystalizedNoEstimatedInterestAsJson)(Financial.format)
      result.isSuccess shouldBe true
      result.get shouldBe financialNoCrystalizedNoEstimatedInterestModel
    }
  }
}
