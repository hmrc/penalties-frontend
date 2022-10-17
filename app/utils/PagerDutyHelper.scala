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

package utils

import utils.Logger.logger

object PagerDutyHelper {

  object PagerDutyKeys extends Enumeration{
    final val INVALID_JSON_RECEIVED_FROM_PENALTIES_BACKEND = Value
    final val RECEIVED_4XX_FROM_PENALTIES_BACKEND = Value
    final val RECEIVED_5XX_FROM_PENALTIES_BACKEND = Value
    final val EMPTY_PENALTY_BODY = Value
    final val INVALID_DATA_RETURNED_FOR_CALCULATION_ROW = Value
    final val NO_DATA_RETURNED_FROM_COMPLIANCE = Value
    final val POC_ACHIEVEMENT_DATE_NOT_FOUND = Value
    final val UNEXPECTED_ERROR_FROM_PENALTIES_BACKEND = Value
  }

  def log(fileAndMethodName: String, pagerDutyKey: PagerDutyKeys.Value): Unit = {
    logger.warn(s"$pagerDutyKey - $fileAndMethodName")
  }

  def logStatusCode(fileAndMethodName: String, code: Int)(keyOn4xx: PagerDutyKeys.Value, keyOn5xx: PagerDutyKeys.Value): Unit = {
    code match {
      case code if code >= 400 && code <= 499 => log(fileAndMethodName, keyOn4xx)
      case code if code >= 500 => log(fileAndMethodName, keyOn5xx)
      case _ =>
    }
  }
}
