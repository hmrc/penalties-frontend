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
import models.compliance.MissingReturn
import org.jsoup.Jsoup

import java.time.LocalDateTime

class CompliancePageHelperSpec extends SpecBase {
  val sampleDate1 = LocalDateTime.of(2022, 1, 1, 1, 1, 1)
  val sampleDate2 = LocalDateTime.of(2022, 1, 31, 0, 0, 0)

  val singleMissingReturn = Seq(
    MissingReturn(
      startDate = sampleDate1,
      endDate = sampleDate2
    )
  )
  val multipleMissingReturns = Seq(
    MissingReturn(
      startDate = sampleDate1.plusMonths(1),
      endDate = sampleDate2.plusMonths(1)
    ),
    MissingReturn(
      startDate = sampleDate1,
      endDate = sampleDate2
    )
  )

  val pageHelper: CompliancePageHelper = injector.instanceOf[CompliancePageHelper]

  "getUnsubmittedReturnContentFromSequence" should {
    "return an empty html element when there is no missing returns" in {
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(Seq.empty)
      result.body.isEmpty shouldBe true
    }

    "return bullets for one missing period" in {
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(singleMissingReturn)
      val parsedResult = Jsoup.parse(result.body)
      parsedResult.select("li").text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
    }

    "return bullets for multiple missing periods" in {
      val result = pageHelper.getUnsubmittedReturnContentFromSequence(multipleMissingReturns)
      val parsedResult = Jsoup.parse(result.body)
      parsedResult.select("li:nth-child(1)").text() shouldBe "VAT period 1 February 2022 to 28 February 2022"
      parsedResult.select("li:nth-child(2)").text() shouldBe "VAT period 1 January 2022 to 31 January 2022"
    }
  }
}
