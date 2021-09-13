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

package models.reason

import play.api.libs.json.{Format, JsError, JsResult, JsString, JsSuccess, JsValue}

object PaymentPenaltyReasonEnum extends Enumeration {
  val VAT_NOT_PAID_WITHIN_15_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val VAT_NOT_PAID_WITHIN_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val VAT_NOT_PAID_AFTER_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val CENTRAL_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val CENTRAL_ASSESSMENT_NOT_PAID_AFTER_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_15_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val ERROR_CORRECTION_NOTICE_NOT_PAID_WITHIN_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val ERROR_CORRECTION_NOTICE_NOT_PAID_AFTER_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_15_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val OFFICERS_ASSESSMENT_NOT_PAID_WITHIN_30_DAYS: PaymentPenaltyReasonEnum.Value = Value
  val OFFICERS_ASSESSMENT_NOT_PAID_AFTER_30_DAYS: PaymentPenaltyReasonEnum.Value = Value

  implicit val format: Format[PaymentPenaltyReasonEnum.Value] = new Format[PaymentPenaltyReasonEnum.Value] {
    override def writes(o: PaymentPenaltyReasonEnum.Value): JsValue = {
      JsString(o.toString.toUpperCase)
    }

    private def getEnumFromString(s: String): Option[Value] = values.find(_.toString == s)

    override def reads(json: JsValue): JsResult[PaymentPenaltyReasonEnum.Value] = {
      getEnumFromString(json.as[String].toUpperCase) match {
        case Some(v) => JsSuccess(v)
        case e => JsError(s"$e Penalty Reason not recognised")
      }
    }
  }
}

