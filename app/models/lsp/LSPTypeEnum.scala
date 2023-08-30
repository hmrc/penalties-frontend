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

package models.lsp

import play.api.libs.json._

object LSPTypeEnum extends Enumeration {

  val AddedFAP: LSPTypeEnum.Value = Value("AF")
  val RemovedFAP: LSPTypeEnum.Value = Value("RF")
  val AppealedPoint: LSPTypeEnum.Value = Value("AP")
  val RemovedPoint: LSPTypeEnum.Value = Value("RP")
  val Point: LSPTypeEnum.Value = Value("P")
  val Financial: LSPTypeEnum.Value = Value("F")

  implicit val format: Format[LSPTypeEnum.Value] = new Format[LSPTypeEnum.Value] {

    override def writes(o: LSPTypeEnum.Value): JsValue = JsString(o.toString.toUpperCase)

    override def reads(json: JsValue): JsResult[LSPTypeEnum.Value] = json.as[String].toUpperCase match {
      case "AF" => JsSuccess(AddedFAP)
      case "RF" => JsSuccess(RemovedFAP)
      case "AP" => JsSuccess(AppealedPoint)
      case "RP" => JsSuccess(RemovedPoint)
      case "P" => JsSuccess(Point)
      case "F" => JsSuccess(Financial)
    }
  }

}
