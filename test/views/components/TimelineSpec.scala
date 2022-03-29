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

package views.components

import base.{BaseSelectors, SpecBase}
import org.jsoup.nodes.Document
import utils.ImplicitDateFormatter
import viewmodels.TimelineEvent
import views.behaviours.ViewBehaviours
import views.html.components.timeline

class TimelineSpec extends SpecBase with ViewBehaviours with ImplicitDateFormatter {

  object Selectors extends BaseSelectors

  val timelineHtml: timeline = injector.instanceOf[timeline]

  val timelineEventsNoStatus: Seq[TimelineEvent] = Seq(
    TimelineEvent(
      s"VAT period ${dateTimeToString(sampleDate)} to ${dateTimeToString(sampleDate.plusDays(7))}",
      s"Submit VAT Return by ${dateTimeToString(sampleDate.plusMonths(1))}",
      None
    ),
    TimelineEvent(
      s"VAT period ${dateTimeToString(sampleDate)} to ${dateTimeToString(sampleDate.plusDays(7))}",
      s"Submit VAT Return by ${dateTimeToString(sampleDate.plusMonths(1))}",
      None
    ),
  )

  val timelineEventsWithStatus: Seq[TimelineEvent] = Seq(
    TimelineEvent(
      s"VAT period ${dateTimeToString(sampleDate)} to ${dateTimeToString(sampleDate.plusDays(7))}",
      s"Submit VAT Return by ${dateTimeToString(sampleDate.plusMonths(1))}",
      Some("Submitted on time")
    ),
    TimelineEvent(
      s"VAT period ${dateTimeToString(sampleDate)} to ${dateTimeToString(sampleDate.plusDays(7))}",
      s"Submit VAT Return by ${dateTimeToString(sampleDate.plusMonths(1))}",
      None
    ),
  )

  "timeline" when {
    "given timeline events with no actions completed" should {
      implicit val doc: Document = asDocument(timelineHtml.apply(timelineEventsNoStatus))

      "display a timeline with no tag status' " in {
        doc.select("ol").attr("class") shouldBe "hmrc-timeline"
        doc.select("h3").get(0).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        doc.select("span").get(0).text() shouldBe "Submit VAT Return by 23 May 2021"

        doc.select("h3").get(1).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        doc.select("span").get(1).text() shouldBe "Submit VAT Return by 23 May 2021"
      }
    }

    "given timeline events with some actions completed" should {
      implicit val doc: Document = asDocument(timelineHtml.apply(timelineEventsWithStatus))

      "display a timeline with no tag status' " in {
        doc.select("ol").attr("class") shouldBe "hmrc-timeline"
        doc.select("h3").get(0).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        doc.select("span").get(0).text() shouldBe "Submit VAT Return by 23 May 2021"
        doc.select("strong").text shouldBe "Submitted on time"

        doc.select("h3").get(1).text() shouldBe "VAT period 23 April 2021 to 30 April 2021"
        doc.select("span").get(1).text() shouldBe "Submit VAT Return by 23 May 2021"
      }
    }
  }

}
