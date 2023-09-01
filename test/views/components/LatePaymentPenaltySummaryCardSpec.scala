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
import models.lpp.{LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum}
import org.jsoup.nodes.Document
import viewmodels.LatePaymentPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLPP
import java.time.LocalDate

import models.lpp.MainTransactionEnum.{CentralAssessmentFirstLPP, CentralAssessmentSecondLPP}

class LatePaymentPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser
  val summaryCardHtml: summaryCardLPP = injector.instanceOf[summaryCardLPP]

  val summaryCardModel: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 2, 1),
      penaltyAmountPosted = 400,
      penaltyAmountPaid = Some(400),
      penaltyAmountOutstanding = None,
      penaltyChargeReference = Some("CHRG1234"))))
  ).get.head

  val summaryCardModelWithUnappealableStatusAndEmptyAppealLevel: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 2, 1),
      appealInformation = Some(Seq(
        AppealInformationType(
          appealStatus = Some(AppealStatusEnum.Unappealable),
          appealLevel = None
        )
      )))))
  ).get.head

  val summaryCardModelWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.4),
      penaltyAmountPosted = 123.4,
      penaltyAmountOutstanding = None,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))))
  ).get.head

  val summaryCardModelVATPaymentDate: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = None,
      principalChargeBillingFrom = LocalDate.parse("2020-01-01"),
      principalChargeBillingTo = LocalDate.parse("2020-01-31"),
      principalChargeDueDate = LocalDate.parse("2020-03-07"))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaid: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPosted = 123.45,
      penaltyAmountPaid = Some(123.45),
      penaltyAmountOutstanding = None,
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7))
    ))
  ).get.head

  val summaryCardModelForUnappealableLPP2: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyChargeReference = None,
      penaltyAmountPaid = None,
      penaltyAmountOutstanding = None,
      penaltyAmountAccruing = 123.45,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7),
      principalChargeLatestClearing = None,
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      )))
    )))
  ).get.head

  val summaryCardModelDueNoPaymentsMadeLPP2CentralAssessment: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyChargeReference = None,
      penaltyAmountPaid = None,
      penaltyAmountAccruing = 123.45,
      penaltyAmountOutstanding = None,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing,
      penaltyChargeDueDate = Some(LocalDate.of(2020, 2, 1)),
      principalChargeBillingFrom = LocalDate.of(2020, 1, 1),
      principalChargeBillingTo = LocalDate.of(2020, 2, 1),
      principalChargeDueDate = LocalDate.of(2020, 3, 7),
      principalChargeLatestClearing = None,
      appealInformation = Some(Seq(AppealInformationType(
        appealStatus = Some(AppealStatusEnum.Unappealable),
        appealLevel = Some(AppealLevelEnum.HMRC)
      ))),
      LPPDetailsMetadata = LPPDetailsMetadata(
        mainTransaction = Some(CentralAssessmentSecondLPP),
        None,
        None
      )
    )))
  ).get.head

  val summaryCardModelForAdditionalPenaltyPaidWithTenths: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = None,
      penaltyAmountPosted = 123.40,
      penaltyAmountOutstanding = Some(123.40))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleUnpaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = None,
      penaltyAmountOutstanding = Some(23.45),
      penaltyStatus = LPPPenaltyStatusEnum.Posted,
      penaltyAmountPosted = 23.45,
      appealInformation = Some(Seq(AppealInformationType(Some(AppealStatusEnum.Under_Appeal), Some(AppealLevelEnum.HMRC)))))))
  ).get.head

  val summaryCardModelForAdditionalPenaltyDuePartiallyPaid: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleUnpaidLPP1.copy(penaltyCategory = LPPPenaltyCategoryEnum.LPP2,
      penaltyAmountPaid = Some(100.00),
      penaltyAmountOutstanding = Some(60.22),
      penaltyAmountPosted = 160.22,
      penaltyStatus = LPPPenaltyStatusEnum.Posted)))
  ).get.head

  val summaryCardModelDue: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(200), penaltyAmountPaid = Some(10))))
  ).get.head

  val summaryCardModelDueNoPaymentsMade: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(400), penaltyAmountPaid = None,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing, penaltyChargeReference = None)))
  ).get.head

  val summaryCardModelDueNoPaymentsMadeCentralAssessment: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(400), penaltyAmountPaid = None,
      penaltyStatus = LPPPenaltyStatusEnum.Accruing, penaltyChargeReference = None,
      LPPDetailsMetadata = LPPDetailsMetadata(mainTransaction = Some(CentralAssessmentFirstLPP), None, None))))
  ).get.head

  val summaryCardModelDueNoPaymentsMadePenaltyPosted: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(samplePaidLPP1.copy(principalChargeLatestClearing = None, penaltyAmountOutstanding = Some(400), penaltyAmountPaid = None,
      penaltyStatus = LPPPenaltyStatusEnum.Posted, penaltyChargeReference = Some("CHRG1234"))))
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

  val summaryCardWithManualLPP: LatePaymentPenaltySummaryCard = summaryCardHelper.populateLatePaymentPenaltyCard(
    Some(Seq(sampleManualLPP))
  ).get.head

  "summaryCard" when {
    "given a LPP1" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModel))

      "display the penalty amount and the date the VAT was due" in {
        doc.select("h4").get(0).ownText shouldBe "£400 penalty"
        doc.select("h4 span").text shouldBe "for late payment of charge due on 1 February 2020"
      }

      "display the penalty amount  and the date the VAT was due (with padded zero if whole tenths)" in {
        implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithTenths))
        doc.select("h4").get(0).ownText shouldBe "£123.40 penalty"
        doc.select("h4 span").text shouldBe "for late payment of charge due on 7 March 2020"
      }

      "display the View calculation link" in {
        doc.select("footer > div a").get(0).ownText shouldBe "View calculation"
        doc.select("footer > div span").get(0).text shouldBe "of first late payment penalty for charge due on 1 February 2020"
        doc.select("a").get(0).attr("href") shouldBe "/penalties/calculation?principalChargeReference=12345678901234&penaltyCategory=LPP1"
      }

      "display the 'PAID' status" in {
        doc.select("strong").text shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadePenaltyPosted))
        doc.select("strong").text shouldBe "due"
      }

      "display the '£200 DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDue))
        doc.select("strong").text shouldBe "£200 due"
      }

      "display the Penalty type" in {
        doc.select("dt").get(0).text shouldBe "Penalty type"
        doc.select("dd").get(0).text shouldBe "First penalty for late payment"
      }

      "display the 'Overdue charge' row" in {
        doc.select("dt").get(1).text shouldBe "Overdue charge"
        doc.select("dd").get(1).text shouldBe "VAT for period 1 January 2020 to 1 February 2020"
      }

      "display principalChargeDueDate in VAT due" in {
        doc.select("dt").get(2).text shouldBe "VAT due"
        doc.select("dd").get(2).text shouldBe "1 February 2020"
      }

      "display the date in VAT due" in {
        val docVATPaymentDate: Document = asDocument(summaryCardHtml.apply(summaryCardModelVATPaymentDate))
        docVATPaymentDate.select("dt").get(2).text shouldBe "VAT due"
        docVATPaymentDate.select("dd").get(2).text shouldBe "7 March 2020"
      }

      "display the appeal link and have the correct hidden span (LPP1)" in {
        doc.select(".app-summary-card__footer a").get(1).ownText shouldBe "Appeal this penalty"
        doc.select(".app-summary-card__footer span").get(1).text shouldBe "which is the first penalty for late payment of charge due on 1 February 2020"
      }

      "display the 'why you cannot appeal yet' drop down if the VAT has not been paid and the penalty has no charge reference" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMade))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your payment history. If you’ve already paid, keep checking back to see when the payment clears."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down if the VAT has not been paid (but penalty is posted)" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadePenaltyPosted))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your payment history. If you’ve already paid, keep checking back to see when the payment clears."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down with Central Assessment content if the VAT has not been paid and the Main Transaction is Central Assessment" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadeCentralAssessment))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until you submit the VAT Return and pay your VAT."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "If you believe you did not need to submit a VAT Return, appeal the late submission penalty for this VAT period instead."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down if the VAT has not been paid and the penalty has no charge reference when the use is an Agent" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMade)(messages, agentUser))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your client’s payment history. If they have already paid, keep checking back to see when the payment clears."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down with Central Assessment content if the VAT has not been paid and the Main Transaction is Central Assessment when the user is an Agent" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadeCentralAssessment)(messages, agentUser))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT Return is submitted and your client pays the VAT."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "If you believe a VAT Return was not due, appeal the late submission penalty for this VAT period instead."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'appeal this penalty' link if the VAT has been paid" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModel))
        doc.select(".app-summary-card__footer a").get(1).ownText shouldBe "Appeal this penalty"
        doc.select(".app-summary-card__footer span").get(1).text shouldBe "which is the first penalty for late payment of charge due on 1 February 2020"
        doc.select(".app-summary-card__footer a").get(1).attr("href").contains(summaryCardModel.penaltyChargeReference.get) shouldBe true
        doc.select("dt").eq(4).isEmpty shouldBe true
      }

      "display 'view calculation' correctly when appeal level is empty" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithUnappealableStatusAndEmptyAppealLevel))
        doc.select(".app-summary-card__footer div a").get(0).ownText shouldBe "View calculation"
        doc.select(".app-summary-card__footer span").get(0).text shouldBe "of first late payment penalty for charge due on 1 February 2020"
      }
    }

    "given a LPP2" should {
      implicit val docWithAdditionalPenalty: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaid))
      implicit val docWithAdditionalPenaltyTenthsOfPence: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyPaidWithTenths))

      "display the penalty amount and the date the VAT was due" in {
        docWithAdditionalPenalty.select("h4").get(0).ownText shouldBe "£123.45 penalty"
        docWithAdditionalPenalty.select("h4 span").text shouldBe "for late payment of charge due on 7 March 2020"
      }

      "display the penalty amount and the date the VAT was due (with padded zero for whole tenths)" in {
        docWithAdditionalPenaltyTenthsOfPence.select("h4").get(0).ownText shouldBe "£123.40 penalty"
        docWithAdditionalPenaltyTenthsOfPence.select("h4 span").text shouldBe "for late payment of charge due on 7 June 2021"

      }

      "display the View calculation link" in {
        docWithAdditionalPenalty.select("footer > div a").get(0).ownText shouldBe "View calculation"
        docWithAdditionalPenalty.select("footer > div span").get(0).text shouldBe "of second late payment penalty for charge due on 7 March 2020"
        docWithAdditionalPenalty.select("a").get(0).attr("href") shouldBe "/penalties/calculation?principalChargeReference=12345678901234&penaltyCategory=LPP2"
      }

      "display the 'PAID' status" in {
        docWithAdditionalPenalty.select("strong").text shouldBe "paid"
      }

      "display the 'DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDue))
        doc.select("strong").text shouldBe "due"
      }

      "display the '£60.22 DUE' status" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDuePartiallyPaid))
        doc.select("strong").text shouldBe "£60.22 due"
      }

      "display the Penalty type" in {
        docWithAdditionalPenalty.select("dt").get(0).text shouldBe "Penalty type"
        docWithAdditionalPenalty.select("dd").get(0).text shouldBe "Second penalty for late payment"
      }

      "display the appeal link and have the correct hidden span (LPP2)" in {
        docWithAdditionalPenalty.select(".app-summary-card__footer a").get(1).ownText shouldBe "Appeal this penalty"
        docWithAdditionalPenalty.select(".app-summary-card__footer span").get(1).text shouldBe "which is the second penalty for late payment of charge due on 7 March 2020"
      }

      "display the 'why you cannot appeal yet' drop down if the penalty is unappealable (VAT has not been paid)" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForUnappealableLPP2))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your payment history. If you’ve already paid, keep checking back to see when the payment clears."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "not display the 'why you cannot appeal yet' drop down if the penalty has an appeal status other than unappealable (e.g. under review)" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForAdditionalPenaltyDue))
        doc.select(".govuk-details__summary-text").isEmpty shouldBe true
        doc.select(".govuk-details__text p:nth-child(1)").isEmpty shouldBe true
        doc.select(".govuk-details__text p:nth-child(2)").isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down if the penalty is unappealable (VAT has not been paid) and the user is an Agent" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelForUnappealableLPP2)(messages, agentUser))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT is paid."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "It can take up to 5 days for the payment to clear and show on your client’s payment history. If they have already paid, keep checking back to see when the payment clears."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down with Central Assessment content if the VAT has not been paid and " +
        "the Main Transaction is Central Assessment" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadeLPP2CentralAssessment))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until you submit the VAT Return and pay your VAT."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "If you believe you did not need to submit a VAT Return, appeal the late submission penalty for this VAT period instead."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }

      "display the 'why you cannot appeal yet' drop down with Central Assessment content if the VAT has not been paid and the " +
        "Main Transaction is Central Assessment and the user is an Agent" in {
        val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelDueNoPaymentsMadeLPP2CentralAssessment)(messages, agentUser))
        doc.select(".govuk-details__summary-text").get(0).ownText shouldBe "Why you cannot appeal yet"
        doc.select(".govuk-details__text p:nth-child(1)").get(0).text shouldBe "You cannot appeal until the VAT Return is submitted and your client pays the VAT."
        doc.select(".govuk-details__text p:nth-child(2)").get(0).text shouldBe "If you believe a VAT Return was not due, appeal the late submission penalty for this VAT period instead."
        doc.select("dt").eq(5).isEmpty shouldBe true
      }
    }

    "given an appealed penalty" should {
      val docWithAppealedPenaltyAccepted: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyAccepted))
      val docWithAppealedPenaltyRejected: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenaltyRejected))
      val docWithAppealedPenalty: Document =
        asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPenalty))

      "have the appeal status for ACCEPTED and not have the calculation link" in {
        docWithAppealedPenaltyAccepted.select("dt").get(4).text shouldBe "Appeal status"
        docWithAppealedPenaltyAccepted.select("dd").get(4).text shouldBe "Appeal accepted"
        docWithAppealedPenaltyAccepted.select(".calculation-link").isEmpty shouldBe true
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPenaltyRejected.select("dt").get(4).text shouldBe "Appeal status"
        docWithAppealedPenaltyRejected.select("dd").get(4).text shouldBe "Appeal rejected"
        docWithAppealedPenaltyRejected.select(".calculation-link").isEmpty shouldBe false
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPenalty.select("dt").get(4).text shouldBe "Appeal status"
        docWithAppealedPenalty.select("dd").get(4).text shouldBe "Under review by HMRC"
        docWithAppealedPenalty.select(".calculation-link").isEmpty shouldBe false
      }
    }

    "given a manual LPP" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardWithManualLPP))

      "display the correct penalty type" in {
        doc.select("dt").get(0).text shouldBe "Penalty type"
        doc.select("dd").get(0).text shouldBe "Penalty for late payment – details are in the letter we sent you"
      }

      "display when it was added correctly" in {
        doc.select("dt").get(1).text shouldBe "Added on"
        doc.select("dd").get(1).text shouldBe "7 June 2021"
      }

      "display that the user is unable to appeal" in {
        doc.select("footer > div a").get(0).text shouldBe "You cannot appeal this penalty online"
      }
    }
  }
}
