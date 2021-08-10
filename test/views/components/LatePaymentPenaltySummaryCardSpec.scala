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

import base.{BaseSelectors, SpecBase}
import models.User
import models.payment.PaymentFinancial
import models.penalty.{PaymentPeriod, PaymentStatusEnum}
import models.point.PenaltyTypeEnum
import org.jsoup.nodes.Document
import viewmodels.LatePaymentPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLPP

import java.time.LocalDateTime

class LatePaymentPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser
  val summaryCardHtml: summaryCardLPP = injector.instanceOf[summaryCardLPP]

  val summaryCardModel: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyPaid.copy(
      period = PaymentPeriod(
        LocalDateTime.of(2020,1,1,1,1,1),
        LocalDateTime.of(2020,2,1,1,1,1),
        LocalDateTime.of(2020,2,1,1,1,1),
        PaymentStatusEnum.Paid
      ))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaid: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyPaid.copy(
      `type` = PenaltyTypeEnum.Additional,
      reason = "VAT_NOT_PAID_ON_TIME",
      period = PaymentPeriod(
        startDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        endDate = LocalDateTime.of(2020, 2, 1, 1, 1, 1),
        dueDate = LocalDateTime.of(2020, 3, 7, 1, 1, 1),
        paymentStatus = PaymentStatusEnum.Paid
      ),
      financial = PaymentFinancial(
        amountDue = 123.45, outstandingAmountDue = 0.00, dueDate = LocalDateTime.of(2020,2,1,1,1,1)
      ))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyDue.copy(
      `type` = PenaltyTypeEnum.Additional,
      period = PaymentPeriod(
        LocalDateTime.of(2020,1,1,1,1,1),
        LocalDateTime.of(2020,2,1,1,1,1),
        LocalDateTime.of(2020,2,1,1,1,1),
        PaymentStatusEnum.Paid
      ),
      financial = PaymentFinancial(
        amountDue = 123.45, outstandingAmountDue = 0.00, dueDate = LocalDateTime.of(2020,2,1,1,1,1)
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

  val summaryCardModelWithAppealedPenaltyAcceptedAgent: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAccepted))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenaltyAcceptedByTribunal: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAcceptedTribunal))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAcceptedByTribunalAgent: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAcceptedTribunal))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenaltyRejected: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejected))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejectedAgent: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejected))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenaltyRejectedTribunal: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejectedTribunal))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejectedTribunalAgent: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejectedTribunal))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenaltyReinstated: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedReinstated))
  ).get.head

  val summaryCardModelWithAppealedPenaltyReinstatedAgent: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedReinstated))
  )(implicitly, agentUser).get.head

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

    "given an additional penalty" should {
      implicit val docWithAdditionalPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaid))

      "display the penalty amount" in {
        docWithAdditionalPenalty.select("h3").text() shouldBe "£123.45 additional penalty"
      }

      "display the 'PAID' status" in {
        docWithAdditionalPenalty.select("strong").text() shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDue))
        doc.select("strong").text() shouldBe "due"
      }

      "display the VAT period" in {
        docWithAdditionalPenalty.select("dt").get(0).text() shouldBe "VAT Period"
        docWithAdditionalPenalty.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the penalty reason" in {
        docWithAdditionalPenalty.select("dt").get(1).text() shouldBe "Penalty reason"
        docWithAdditionalPenalty.select("dd").get(1).text() shouldBe "VAT more than 30 days late"
      }

      "display the charged daily from - 31 days after the due date" in {
        docWithAdditionalPenalty.select("dt").get(2).text() shouldBe "Charged daily from"
        docWithAdditionalPenalty.select("dd").get(2).text() shouldBe "7 April 2020"
      }

      "display the appeal link" in {
        docWithAdditionalPenalty.select(".app-summary-card__footer a").get(0).text shouldBe "Appeal this penalty"
      }
    }

    "given an appealed penalty" should {
      val docWithAppealedPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenalty))
      val docWithAppealedPenaltyUnderTribunalReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyUnderTribunalReview))
      val docWithAppealedPenaltyAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAccepted))
      val docWithAppealedPenaltyAcceptedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAcceptedAgent))
      val docWithAppealedPenaltyAcceptedByTribunal: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAcceptedByTribunal))
      val docWithAppealedPenaltyAcceptedByTribunalAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAcceptedByTribunalAgent))
      val docWithAppealedPenaltyRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejected))
      val docWithAppealedPenaltyRejectedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejectedAgent))
      val docWithAppealedPenaltyTribunalRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejectedTribunal))
      val docWithAppealedPenaltyTribunalRejectedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejectedTribunalAgent))
      val docWithAppealedPenaltyTribunalReinstated: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyReinstated))
      val docWithAppealedPenaltyTribunalReinstatedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyReinstatedAgent))

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

      "have the appeal status for ACCEPTED - no outcome message for agents" in {
        docWithAppealedPenaltyAcceptedAgent.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAcceptedAgent.select("dd").get(2).text() shouldBe "Appeal accepted"
      }

      "have the appeal status ACCEPTED_BY_TRIBUNAL" in {
        docWithAppealedPenaltyAcceptedByTribunal.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAcceptedByTribunal.select("dd").get(2).text() shouldBe "Appeal accepted by tax tribunal Read outcome message"
      }

      "have the appeal status ACCEPTED_BY_TRIBUNAL - no outcome message for agents" in {
        docWithAppealedPenaltyAcceptedByTribunalAgent.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAcceptedByTribunalAgent.select("dd").get(2).text() shouldBe "Appeal accepted by tax tribunal"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPenaltyRejected.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejected.select("dd").get(2).text() shouldBe "Appeal rejected Read outcome message"
      }

      "have the appeal status for REJECTED - no outcome message for agents" in {
        docWithAppealedPenaltyRejectedAgent.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejectedAgent.select("dd").get(2).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithAppealedPenaltyTribunalRejected.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyTribunalRejected.select("dd").get(2).text() shouldBe "Appeal rejected by tax tribunal Read outcome message"
      }

      "have the appeal status for TRIBUNAL REJECTED - no outcome message for agents" in {
        docWithAppealedPenaltyTribunalRejectedAgent.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyTribunalRejectedAgent.select("dd").get(2).text() shouldBe "Appeal rejected by tax tribunal"
      }

      "have the appeal status for REINSTATED" in {
        docWithAppealedPenaltyTribunalReinstated.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyTribunalReinstated.select("dd").get(2).text() shouldBe "Appeal outcome changed Read message"
      }

      "have the appeal status for REINSTATED - no outcome message for agents" in {
        docWithAppealedPenaltyTribunalReinstatedAgent.select("dt").get(2).text() shouldBe "Appeal status"
        docWithAppealedPenaltyTribunalReinstatedAgent.select("dd").get(2).text() shouldBe "Appeal outcome changed"
      }
    }
  }
}
