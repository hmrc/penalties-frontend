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

package utils

import play.api.i18n.Messages
import java.time.{LocalDate, LocalDateTime}

import play.twirl.api.Html

trait ImplicitDateFormatter {

  implicit def dateToString(date: LocalDate)(implicit messages: Messages): String =
    s"${date.getDayOfMonth}" + Html("&nbsp;") +  s"${messages(s"month.${date.getMonthValue}")}" + Html("&nbsp;") + s"${date.getYear}"

  implicit def dateTimeToString(date: LocalDateTime)(implicit messages: Messages): String =
    s"${date.getDayOfMonth} ${messages(s"month.${date.getMonthValue}")} ${date.getYear}"

  implicit def dateTimeToMonthYearString(date: LocalDateTime)(implicit messages: Messages): String =
    s"${messages(s"month.${date.getMonthValue}")} ${date.getYear}"

  implicit def dateToMonthYearString(date: LocalDate)(implicit messages: Messages): String =
    s"${messages(s"month.${date.getMonthValue}")}" + Html("&nbsp;") + s"${date.getYear}"
}

object ImplicitDateFormatter extends ImplicitDateFormatter
