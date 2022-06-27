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

package models.v3.lpp

import play.api.libs.json._

object MainTypeEnum extends Enumeration {
  val VATReturnCharge: MainTypeEnum.Value = Value("4700")
  val VATReturnFirstLPP: MainTypeEnum.Value = Value("4703")
  val VATReturnSecondLPP: MainTypeEnum.Value = Value("4704")
  val CentralAssessment: MainTypeEnum.Value = Value("4720")
  val CentralAssessmentFirstLPP: MainTypeEnum.Value = Value("4723")
  val CentralAssessmentSecondLPP: MainTypeEnum.Value = Value("4724")
  val OfficersAssessment: MainTypeEnum.Value = Value("4730")
  val OfficersAssessmentFirstLPP: MainTypeEnum.Value = Value("4741")
  val OfficersAssessmentSecondLPP: MainTypeEnum.Value = Value("4742")
  val ErrorCorrection: MainTypeEnum.Value = Value("4731")
  val ErrorCorrectionFirstLPP: MainTypeEnum.Value = Value("4743")
  val ErrorCorrectionSecondLPP: MainTypeEnum.Value = Value("4744")
  val AdditionalAssessment: MainTypeEnum.Value = Value("4732")
  val AdditionalAssessmentFirstLPP: MainTypeEnum.Value = Value("4758")
  val AdditionalAssessmentSecondLPP: MainTypeEnum.Value = Value("4759")
  val ProtectiveAssessment: MainTypeEnum.Value = Value("4733")
  val ProtectiveAssessmentFirstLPP: MainTypeEnum.Value = Value("4761")
  val ProtectiveAssessmentSecondLPP: MainTypeEnum.Value = Value("4762")
  val POAReturnCharge: MainTypeEnum.Value = Value("4701")
  val POAReturnChargeFirstLPP: MainTypeEnum.Value = Value("4716")
  val POAReturnChargeSecondLPP: MainTypeEnum.Value = Value("4717")
  val AAReturnCharge: MainTypeEnum.Value = Value("4702")
  val AAReturnChargeFirstLPP: MainTypeEnum.Value = Value("4718")
  val AAReturnChargeSecondLPP: MainTypeEnum.Value = Value("4719")

  implicit val format: Format[MainTypeEnum.Value] = new Format[MainTypeEnum.Value] {
    override def writes(o: MainTypeEnum.Value): JsValue = {
      JsString(o.toString)
    }

    override def reads(json: JsValue): JsResult[MainTypeEnum.Value] = {
      json.as[String] match {
        case "4700" => JsSuccess(VATReturnCharge)
        case "4703" => JsSuccess(VATReturnFirstLPP)
        case "4704" => JsSuccess(VATReturnSecondLPP)
        case "4720" => JsSuccess(CentralAssessment)
        case "4723" => JsSuccess(CentralAssessmentFirstLPP)
        case "4724" => JsSuccess(CentralAssessmentSecondLPP)
        case "4730" => JsSuccess(OfficersAssessment)
        case "4741" => JsSuccess(OfficersAssessmentFirstLPP)
        case "4742" => JsSuccess(OfficersAssessmentSecondLPP)
        case "4731" => JsSuccess(ErrorCorrection)
        case "4743" => JsSuccess(ErrorCorrectionFirstLPP)
        case "4744" => JsSuccess(ErrorCorrectionSecondLPP)
        case "4732" => JsSuccess(AdditionalAssessment)
        case "4758" => JsSuccess(AdditionalAssessmentFirstLPP)
        case "4759" => JsSuccess(AdditionalAssessmentSecondLPP)
        case "4733" => JsSuccess(ProtectiveAssessment)
        case "4761" => JsSuccess(ProtectiveAssessmentFirstLPP)
        case "4762" => JsSuccess(ProtectiveAssessmentSecondLPP)
        case "4701" => JsSuccess(POAReturnCharge)
        case "4716" => JsSuccess(POAReturnChargeFirstLPP)
        case "4717" => JsSuccess(POAReturnChargeSecondLPP)
        case "4702" => JsSuccess(AAReturnCharge)
        case "4718" => JsSuccess(AAReturnChargeFirstLPP)
        case "4719" => JsSuccess(AAReturnChargeSecondLPP)
        case e => JsError(s"$e not recognised")
      }
    }
  }
}