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

package viewmodels

import base.SpecBase
import models.compliance.{Return, ReturnStatusEnum}
import org.jsoup.Jsoup
import utils.ImplicitDateFormatter

import java.time.LocalDateTime

class TimelineHelperSpec extends SpecBase with ImplicitDateFormatter {

  val helper = injector.instanceOf[TimelineHelper]

  val sampleExpiryDate = LocalDateTime.of(2023,3,1,1,1,1)

  val complianceReturns = Seq(
    Return(sampleDate,sampleDate.plusDays(7), sampleDate.plusMonths(1), Some(ReturnStatusEnum.Submitted)),
    Return(sampleDate,sampleDate.plusDays(7), sampleDate.plusMonths(1), None),
  )

  "TimelineHelper" when {
    "getTimelineContent is called and given compliance returns to take action on" should {
      "return the timeline component wrapped in html" in {
        val result = helper.getTimelineContent(complianceReturns, sampleExpiryDate)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("ol").attr("class") shouldBe "hmrc-timeline"
        parsedHtmlResult.select("h2").get(0).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        parsedHtmlResult.select("span").get(0).text() shouldBe "Submit VAT Return by 23 May 2021"
        parsedHtmlResult.select("strong").text shouldBe "Submitted on time"

        parsedHtmlResult.select("h2").get(1).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        parsedHtmlResult.select("span").get(1).text() shouldBe "Submit VAT Return by 23 May 2021"

        parsedHtmlResult.select("#point-expiry-date").text shouldBe "If you complete these actions we will remove your points in March 2023."
      }
    }
  }
}
