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

package views.components.v2

import base.{BaseSelectors, SpecBase}
import models.User
import models.v3.lpp.{LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import org.jsoup.nodes.Document
import viewmodels.v2.LatePaymentPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.v2.{summaryCardLPP => summaryCardLPPv2}

import java.time.LocalDate

class LatePaymentPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser
  val summaryCardHtml: summaryCardLPPv2 = injector.instanceOf[summaryCardLPPv2]

  val summaryCardModel: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaid.copy(principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 2, 1))))
  ).get.head

  val summaryCardModelWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.4),
      penaltyAmountOutstanding = Some(00.0),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = LocalDate.of(2020, 2, 1),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))))
  ).get.head

  val summaryCardModelVATPaymentDate: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = Some(00.0))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaid: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = Some(0.00),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = LocalDate.of(2020, 2, 1),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))
    ))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaidWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(00.00),
      penaltyAmountOutstanding = Some(123.40))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDue: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaymentDue.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(00.00),
      penaltyAmountOutstanding = Some(23.45),
      penaltyStatus = LPPPenaltyStatusEnum.Posted)))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDuePartiallyPaid: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaymentDue.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(100.00),
      penaltyAmountOutstanding = Some(60.22),
      penaltyStatus = LPPPenaltyStatusEnum.Posted)))
  ).get.head

  val summaryCardModelDue: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaymentDue))
  ).get.head

  val summaryCardModelDueNoPaymentsMade: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPPDetailsVATPaymentDue.copy(penaltyAmountPaid = Some(0.00),
      penaltyAmountOutstanding = Some(1001.45))))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAccepted: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAcceptedv2))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAcceptedAgent: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedAcceptedv2))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenaltyRejected: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejectedv2))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejectedAgent: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedRejectedv2))
  )(implicitly, agentUser).get.head

  val summaryCardModelWithAppealedPenalty: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyUnderAppealv2))
  ).get.head


  // TODO: Update for Reinstated
  val summaryCardModelWithAppealedPenaltyReinstated: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedReinstatedv2))
  ).get.head


  val summaryCardModelWithAppealedPenaltyReinstatedAgent: LatePaymentPenaltySummaryCard = summaryCardHelperv2.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLatePaymentPenaltyAppealedReinstatedv2))
  )(implicitly, agentUser).get.head

  "summaryCard" when {
    "given a penalty" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModel))

      "display the penalty amount" in {
        doc.select("h3").text() shouldBe "£400 penalty"
      }

      "display the penalty amount (with padded zero if whole tenths)" in {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithTenths))
        doc.select("h3").text() shouldBe "£123.40 penalty"
      }

      "display the View calculation link" in {
        doc.select("footer > div a").get(0).text() shouldBe "View calculation"
        doc.select("a").get(0).attr("href") shouldBe "/penalties/calculation?penaltyId=12345678901234&isAdditional=false"
      }

      "display the 'PAID' status" in {
        doc.select("strong").text() shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMade))
        doc.select("strong").text() shouldBe "due"
      }

      "display the '£200 DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDue))
        doc.select("strong").text() shouldBe "£200 due"
      }

      "display the Penalty type" in {
        doc.select("dt").get(0).text() shouldBe "Penalty type"
        doc.select("dd").get(0).text() shouldBe "First penalty for late payment"
      }

      "display the 'Overdue charge' row" in {
        doc.select("dt").get(1).text() shouldBe "Overdue charge"
        //TODO: Get the Reasons
        doc.select("dd").get(1).text() shouldBe "LPPPenaltyReasonKey"
      }

      "display Date(principalChargeDueDate) in Charge due" in {
        doc.select("dt").get(2).text() shouldBe "Charge due"
        doc.select("dd").get(2).text() shouldBe "1 February 2020"
      }
//      TODO: implement Reason
      "display the date in VAT Payment date" ignore {
        val docVATPaymentDate: Document = asDocument(summaryCardHtml.apply(summaryCardModelVATPaymentDate))
        docVATPaymentDate.select("dt").get(2).text() shouldBe "VAT payment date"
        docVATPaymentDate.select("dd").get(2).text() shouldBe "1 March 2020"
      }

      "display the penalty reason" ignore {
        doc.select("dt").get(3).text() shouldBe "Penalty reason"
        doc.select("dd").get(3).text() shouldBe "VAT not paid within 15 days"
      }

      "display the appeal link" in {
        doc.select(".app-summary-card__footer a").get(1).text shouldBe "Appeal this penalty"
      }
    }

    "given an additional penalty" should {
      implicit val docWithAdditionalPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaid))
      implicit val docWithAdditionalPenaltyTenthsOfPence: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaidWithTenths))

      "display the penalty amount" in {
        docWithAdditionalPenalty.select("h3").text() shouldBe "£123.45 additional penalty"
      }

      "display the penalty amount (with padded zero for whole tenths)" in {
        docWithAdditionalPenaltyTenthsOfPence.select("h3").text() shouldBe "£123.40 additional penalty"
      }

      "display the View calculation link" in {
        docWithAdditionalPenalty.select("footer > div a").get(0).text() shouldBe "View calculation"
        docWithAdditionalPenalty.select("a").get(0).attr("href") shouldBe "/penalties/calculation?penaltyId=12345678901234&isAdditional=true"
      }

      "display the 'PAID' status" in {
        docWithAdditionalPenalty.select("strong").text() shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDue))
        doc.select("strong").text() shouldBe "due"
      }

      "display the '£60.22 DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDuePartiallyPaid))
        doc.select("strong").text() shouldBe "£60.22 due"
      }

      "display the Penalty type" in {
        docWithAdditionalPenalty.select("dt").get(0).text() shouldBe "Penalty type"
        docWithAdditionalPenalty.select("dd").get(0).text() shouldBe "Second penalty for late payment"
      }
// TODO: implement reason
      "display the penalty reason" ignore {
        docWithAdditionalPenalty.select("dt").get(1).text() shouldBe "Penalty reason"
        docWithAdditionalPenalty.select("dd").get(1).text() shouldBe "VAT more than 30 days late"
      }

      "display the appeal link" in {
        docWithAdditionalPenalty.select(".app-summary-card__footer a").get(1).text shouldBe "Appeal this penalty"
      }
    }

    "given an appealed penalty" should {
       val docWithAppealedPenaltyAccepted: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAccepted))
      val docWithAppealedPenaltyAcceptedAgent: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAcceptedAgent))

      val docWithAppealedPenaltyRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejected))
      val docWithAppealedPenaltyRejectedAgent: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejectedAgent))

      val docWithAppealedPenalty: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenalty))

      "have the appeal status for ACCEPTED" in {
        docWithAppealedPenaltyAccepted.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAccepted.select("dd").get(4).text() shouldBe "Appeal accepted Read outcome message"
      }

      "have the appeal status for ACCEPTED - no outcome message for agents" in {
        docWithAppealedPenaltyAcceptedAgent.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAcceptedAgent.select("dd").get(4).text() shouldBe "Appeal accepted"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPenaltyRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejected.select("dd").get(4).text() shouldBe "Appeal rejected Read outcome message"
      }

      "have the appeal status for REJECTED - no outcome message for agents" in {
        docWithAppealedPenaltyRejectedAgent.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejectedAgent.select("dd").get(4).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPenalty.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenalty.select("dd").get(4).text() shouldBe "Under review by HMRC"
      }
    }
  }
}
