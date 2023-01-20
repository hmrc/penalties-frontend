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

package views.components

import base.{BaseSelectors, SpecBase}
import models.User
import models.appealInfo.{AppealInformationType, AppealLevelEnum, AppealStatusEnum}
import models.lpp.{LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import org.jsoup.nodes.Document
import viewmodels.LatePaymentPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLPP

import java.time.LocalDate

class LatePaymentPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser
  val summaryCardHtml: summaryCardLPP = injector.instanceOf[summaryCardLPP]

  val summaryCardModel: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 2, 1),
      penaltyAmountPaid = Some(400),
      penaltyAmountOutstanding = Some(0))))
  ).get.head

  val summaryCardModelWithUnappealableStatus: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 2, 1),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = Some(AppealLevelEnum.HMRC)
        )
      )))))
  ).get.head

  val summaryCardModelWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.4),
      penaltyAmountOutstanding = Some(00.0),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))))
  ).get.head

  val summaryCardModelVATPaymentDate: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = Some(00.0),
      principalChargeBillingFrom = LocalDate.parse("2020-01-01"),
      principalChargeBillingTo = LocalDate.parse("2020-01-31"),
      principalChargeDueDate = LocalDate.parse("2020-03-07"))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaid: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = Some(0.00),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))
    ))
  ).get.head

  val summaryCardModelForAdditionalPenaltyUnappealable: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = Some(0.00),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7),
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )))
    )))
  ).get.head


  val summaryCardModelForAdditionalPenaltyPaidWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(00.00),
      penaltyAmountOutstanding = Some(123.40))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(0.00),
      penaltyAmountOutstanding = Some(23.45),
      penaltyStatus = LPPPenaltyStatusEnum.Posted)))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDuePartiallyPaid: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(100.00),
      penaltyAmountOutstanding = Some(60.22),
      penaltyStatus = LPPPenaltyStatusEnum.Posted)))
  ).get.head

  val summaryCardModelDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(200), penaltyAmountPaid = Some(10))))
  ).get.head

  val summaryCardModelDueNoPaymentsMade: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1Paid.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(400), penaltyAmountPaid = Some(0))))
  ).get.head

  val summaryCardModelWithAppealedPenaltyAccepted: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1AppealPaid(AppealStatusEnum.Upheld, AppealLevelEnum.HMRC)))
  ).get.head

  val summaryCardModelWithAppealedPenaltyRejected: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1AppealPaid(AppealStatusEnum.Rejected, AppealLevelEnum.HMRC)))
  ).get.head
  val summaryCardModelWithAppealedPenalty: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleLPP1AppealPaid(AppealStatusEnum.Under_Appeal, AppealLevelEnum.HMRC)))
  ).get.head

  "summaryCard" when {
    "given a LPP1" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModel))

      "display the penalty amount" in {
        doc.select("h4").text() shouldBe "£400 penalty"
      }

      "display the penalty amount (with padded zero if whole tenths)" in {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithTenths))
        doc.select("h4").text() shouldBe "£123.40 penalty"
      }

      "display the View calculation link" in {
        doc.select("footer > div a").get(0).text() shouldBe "View calculation"
        doc.select("a").get(0).attr("href") shouldBe "/penalties/calculation?principalChargeReference=12345678901234&penaltyCategory=LPP1"
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
        doc.select("dd").get(1).text() shouldBe "VAT for period 1 January 2020 to 1 February 2020"
      }

      "display principalChargeDueDate in Charge due" in {
        doc.select("dt").get(2).text() shouldBe "Charge due"
        doc.select("dd").get(2).text() shouldBe "1 February 2020"
      }

      "display the date in Charge due" in {
        val docVATPaymentDate: Document = asDocument(summaryCardHtml.apply(summaryCardModelVATPaymentDate))
        docVATPaymentDate.select("dt").get(2).text() shouldBe "Charge due"
        docVATPaymentDate.select("dd").get(2).text() shouldBe "7 March 2020"
      }

      "display the appeal link and have the correct aria-label (LPP1)" in {
        doc.select(".app-summary-card__footer a").get(1).text shouldBe "Appeal this penalty"
        doc.select(".app-summary-card__footer a").get(1).attr("aria-label") shouldBe "Appeal first penalty for late payment of charge due on 1 February 2020"
      }

      "display the check if you can appeal link if the penalty is unappealable" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithUnappealableStatus))
        doc.select(".app-summary-card__footer a").get(1).text shouldBe "Check if you can appeal"
        doc.select(".app-summary-card__footer a").get(1).attr("href").contains(summaryCardModelWithUnappealableStatus.principalChargeReference)
        doc.select("dt").eq(4).isEmpty shouldBe true
      }
    }

    "given a LPP2" should {
      implicit val docWithAdditionalPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaid))
      implicit val docWithAdditionalPenaltyTenthsOfPence: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaidWithTenths))

      "display the penalty amount" in {
        docWithAdditionalPenalty.select("h4").text() shouldBe "£123.45 penalty"
      }

      "display the penalty amount (with padded zero for whole tenths)" in {
        docWithAdditionalPenaltyTenthsOfPence.select("h4").text() shouldBe "£123.40 penalty"
      }

      "display the View calculation link" in {
        docWithAdditionalPenalty.select("footer > div a").get(0).text() shouldBe "View calculation"
        docWithAdditionalPenalty.select("a").get(0).attr("href") shouldBe "/penalties/calculation?principalChargeReference=12345678901234&penaltyCategory=LPP2"
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

      "display the appeal link and have the correct aria-label (LPP2)" in {
        docWithAdditionalPenalty.select(".app-summary-card__footer a").get(1).text shouldBe "Appeal this penalty"
        docWithAdditionalPenalty.select(".app-summary-card__footer a").get(1).attr("aria-label") shouldBe "Appeal second penalty for late payment of charge due on 7 March 2020"
      }

      "display the check if you can appeal link if the penalty is unappealable" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyUnappealable))
        doc.select(".app-summary-card__footer a").get(1).text shouldBe "Check if you can appeal"
        doc.select(".app-summary-card__footer a").get(1).attr("href").contains(summaryCardModelForAdditionalPenaltyUnappealable.principalChargeReference)
        doc.select("dt").eq(4).isEmpty shouldBe true
      }
    }

    "given an appealed penalty" should {
      val docWithAppealedPenaltyAccepted: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAccepted))
      val docWithAppealedPenaltyRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejected))
      val docWithAppealedPenalty: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenalty))

      "have the appeal status for ACCEPTED" in {
        docWithAppealedPenaltyAccepted.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyAccepted.select("dd").get(4).text() shouldBe "Appeal accepted"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPenaltyRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenaltyRejected.select("dd").get(4).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPenalty.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPenalty.select("dd").get(4).text() shouldBe "Under review by HMRC"
      }
    }
  }
}
