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

package views.components

import java.time.LocalDateTime

import base.{BaseSelectors, SpecBase}
import models.penalty.{PaymentPeriod, PaymentStatusEnum}
import viewmodels.LatePaymentPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLPP
import org.jsoup.nodes.Document

class LatePaymentPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  val summaryCardHtml: summaryCardLPP = injector.instanceOf[summaryCardLPP]

  val summaryCardModel: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyPaid.copy(
      period = PaymentPeriod(
        LocalDateTime.of(2020,1,1,1,1,1),
        LocalDateTime.of(2020,2,1,1,1,1),
        PaymentStatusEnum.Paid
      ))))
  ).get.head

  val summaryCardModelDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyDue))
  ).get.head

  val summaryCardModelWithAppealedPenalty: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedUnderReview))
  ).get.head

  val summaryCardModelWithAppealedPenaltyUnderTribunalReview: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedUnderTribunalReview))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAccepted: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAccepted))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAcceptedByTribunal: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAcceptedTribunal))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejected: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejected))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejectedTribunal: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejectedTribunal))
  ).get.head

  "summaryCard" when {
    "given a penalty" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModel))

      "display the penalty amount" in {
        doc.select("h3").text() shouldBe "£400 penalty"
      }

      "display the 'PAID' status" in {
        doc.select("strong").text() shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDue))
        doc.select("strong").text() shouldBe "due"
      }

      "display the VAT period" in {
        doc.select("dt").get(0).text() shouldBe "VAT Period"
        doc.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the penalty reason" in {
        doc.select("dt").get(1).text() shouldBe "Penalty reason"
        doc.select("dd").get(1).text() shouldBe "VAT not paid within 15 days"
      }

      "display the appeal link" in {
        doc.select(".app-summary-card__footer a").get(0).text shouldBe "Appeal this penalty"
      }
    }

    "given an appealed penalty" should {
      val docWithAppealedPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenalty))
      val docWithAppealedPenaltyUnderTribunalReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyUnderTribunalReview))
      val docWithAppealedPenaltyAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAccepted))
      val docWithAppealedPenaltyAcceptedByTribunal: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAcceptedByTribunal))
      val docWithAppealedPenaltyRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejected))
      val docWithAppealedPenaltyUnderTribunalRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejectedTribunal))

      "not show the appeal link" in {
        docWithAppealedPenalty.select(".app-summary-card__footer a").isEmpty shouldBe true
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPenalty.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenalty.select("dd").get(2).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in {
        docWithAppealedPenaltyUnderTribunalReview.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyUnderTribunalReview.select("dd").get(2).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for ACCEPTED" in {
        docWithAppealedPenaltyAccepted.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAccepted.select("dd").get(2).text() shouldBe "Appeal accepted Read outcome message"
      }

      "have the appeal status ACCEPTED_BY_TRIBUNAL" in {
        docWithAppealedPenaltyAcceptedByTribunal.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAcceptedByTribunal.select("dd").get(2).text() shouldBe "Appeal accepted by tax tribunal Read outcome message"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPenaltyRejected.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejected.select("dd").get(2).text() shouldBe "Appeal rejected Read outcome message"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithAppealedPenaltyUnderTribunalRejected.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyUnderTribunalRejected.select("dd").get(2).text() shouldBe "Appeal rejected by tax tribunal Read outcome message"
      }
    }
  }
}
