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
import models.financial.Financial
import models.penalty.PenaltyPeriod
import models.point.{AppealStatusEnum, PenaltyPoint, PenaltyTypeEnum, PointStatusEnum}
import models.submission.{Submission, SubmissionStatusEnum}
import org.jsoup.nodes.Document
import viewmodels.LateSubmissionPenaltySummaryCard
import views.behaviours.ViewBehaviours
import views.html.components.summaryCardLSP

import java.time.LocalDateTime

class LateSubmissionPenaltySummaryCardSpec extends SpecBase with ViewBehaviours {

  object Selectors extends BaseSelectors

  implicit val user: User[_] = vatTraderUser

  val summaryCardHtml: summaryCardLSP = injector.instanceOf[summaryCardLSP]

  val summaryCardModelWithAppealedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Under_Review))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointUnderTribunalReview: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Under_Tribunal_Review))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointAccepted: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Accepted), status = PointStatusEnum.Removed)),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointAcceptedByTribunal: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Accepted_By_Tribunal), status = PointStatusEnum.Removed)),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Rejected))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAppealedPointTribunalRejected: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(
    Seq(samplePenaltyPoint.copy(appealStatus = Some(AppealStatusEnum.Tribunal_Rejected))),
    quarterlyThreshold, 1).head

  val summaryCardModelWithAddedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    None,
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Added,
    None,
    None,
    Seq.empty
  )), quarterlyThreshold, 1).head

  val summaryCardModelWithAddedPointAtThreshold: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "1",
    None,
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Added,
    None,
    None,
    Seq.empty
  )), quarterlyThreshold, 4).head

  val summaryCardModelWithRemovedPoint: LateSubmissionPenaltySummaryCard = summaryCardHelper.populateLateSubmissionPenaltyCard(Seq(PenaltyPoint(
    PenaltyTypeEnum.Point,
    "123456789",
    "2",
    None,
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Removed,
    Some("A really great reason."),
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        None,
        SubmissionStatusEnum.Overdue
      )
    )),
    Seq.empty
  )), quarterlyThreshold, 1).head

  val summaryCardModelWithFinancialPointBelowThreshold: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    None,
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress: LateSubmissionPenaltySummaryCard = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Under_Review),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealAccepted = (user: User[_]) => summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Accepted),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Removed,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)(implicitly, user)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealRejected = (user: User[_]) => summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Rejected),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)(implicitly, user)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealReinstated = (user: User[_]) => summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Reinstated),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)(implicitly, user)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Under_Tribunal_Review),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected = (user: User[_]) => summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Tribunal_Rejected),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)(implicitly, user)

  val summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalAccepted = (user: User[_]) => summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "1",
    Some(AppealStatusEnum.Accepted_By_Tribunal),
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Removed,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), quarterlyThreshold)(implicitly, user)

  val summaryCardModelWithFinancialPointAboveThreshold = summaryCardHelper.financialSummaryCard(PenaltyPoint(
    PenaltyTypeEnum.Financial,
    "123456789",
    "3",
    None,
    LocalDateTime.of(2020, 1, 1, 1, 1, 1),
    Some(LocalDateTime.of(2020, 2, 1, 1, 1, 1)),
    PointStatusEnum.Due,
    None,
    Some(PenaltyPeriod(
      LocalDateTime.of(2020, 1, 1, 1, 1, 1),
      LocalDateTime.of(2020, 2, 1, 1, 1, 1),
      Submission(
        LocalDateTime.of(2020, 1, 1, 1, 1, 1),
        Some(LocalDateTime.of(2020, 1, 1, 1, 1, 1)),
        SubmissionStatusEnum.Submitted
      )
    )),
    Seq.empty,
    financial = Some(
      Financial(
        amountDue = 200.00,
        outstandingAmountDue = 200.00,
        dueDate = LocalDateTime.of(2020, 1, 1, 1, 1, 1)
      )
    )
  ), annualThreshold)(implicitly, user)


  "summaryCard" when {
    "given an added point and the threshold has not been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPoint))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h3").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in {
        doc.select("a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
        //TODO: change this once we have the adjustment point info page
        doc.select("a").attr("href") shouldBe "#"
      }

      "display the 'active' status for an added point" in {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "1 January 2020"
      }

      "display when the point is due to expire" in {
        doc.select("dt").get(1).text() shouldBe "Point due to expire"
        doc.select("dd").get(1).text() shouldBe "February 2020"
      }

      "display that the user can not appeal an added point" in {
        doc.select("footer li").text() shouldBe "You cannot appeal this point"
      }
    }

    "given an added point and the threshold has been met" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAddedPointAtThreshold))
      "display that the point has been added i.e. Penalty point X: adjustment point" in {
        doc.select("h3").text() shouldBe "Penalty point 1: adjustment point"
      }

      "display a link to allow the user to find information about adjusted points" in {
        doc.select("a").text() shouldBe "Find out more about adjustment points (opens in a new tab)"
        //TODO: change this once we have the adjustment point info page
        doc.select("a").attr("href") shouldBe "#"
      }

      "display the 'active' status for an added point" in {
        doc.select("strong").text() shouldBe "active"
      }

      "display when the added point was added" in {
        doc.select("dt").get(0).text() shouldBe "Added on"
        doc.select("dd").get(0).text() shouldBe "1 January 2020"
      }

      "NOT display when the point is due to expire" in {
        doc.select("dt").size() shouldBe 1
        doc.select("dd").size() shouldBe 1
      }

      "display that the user can not appeal an added point" in {
        doc.select("footer li").text() shouldBe "You cannot appeal this point"
      }
    }

    "given a removed point" should {
      implicit val doc: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithRemovedPoint))
      "display that the point number as usual" in {
        doc.select("h3").text() shouldBe "Penalty point"
      }

      "display the VAT period the point was removed from" in {
        doc.select("dt").get(0).text() shouldBe "VAT Period"
        doc.select("dd").get(0).text() shouldBe "1 January 2020 to 1 February 2020"
      }

      "display the reason why the point was removed" in {
        doc.select("dt").get(1).text() shouldBe "Reason"
        doc.select("dd").get(1).text() shouldBe "A really great reason."
      }

      "not display any footer text" in {
        doc.select("footer li").hasText shouldBe false
      }
    }

    "given a financial point" should {
      val docWithFinancialPointBelowThreshold: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThreshold))
      val docWithFinancialPointAboveThreshold: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointAboveThreshold))
      val docWithFinancialPointAppealUnderReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealInProgress))
      val docWithFinancialPointAppealAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealAccepted(user)))
      val docWithFinancialPointAppealAcceptedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealAccepted(agentUser)))
      val docWithFinancialPointAppealRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealRejected(user)))
      val docWithFinancialPointAppealRejectedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealRejected(agentUser)))
      val docWithFinancialPointAppealReinstated: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealReinstated(user)))
      val docWithFinancialPointAppealReinstatedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealReinstated(agentUser)))
      val docWithFinancialPointAppealUnderTribunalReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealUnderTribunalReview))
      val docWithFinancialPointAppealTribunalRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected(user)))
      val docWithFinancialPointAppealTribunalRejectedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalRejected(agentUser)))
      val docWithFinancialPointAppealTribunalAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalAccepted(user)))
      val docWithFinancialPointAppealTribunalAcceptedAgent: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithFinancialPointBelowThresholdAndAppealTribunalAccepted(agentUser)))

      "shows the financial heading with point number when the point is below/at threshold for filing frequency" in {
        docWithFinancialPointBelowThreshold.select(".app-summary-card__title").get(0).text shouldBe "Penalty point 1: £200 penalty"
      }

      "shows the financial heading WITHOUT point number when the point is above threshold for filing frequency and a rewording of the appeal text" in {
        docWithFinancialPointAboveThreshold.select(".app-summary-card__title").get(0).text shouldBe "£200 penalty"
        docWithFinancialPointAboveThreshold.select(".app-summary-card__footer a").get(0).text shouldBe "Appeal this penalty"
      }

      "shows the appeal information when the point is being appealed - i.e. under review" in {
        docWithFinancialPointAppealUnderReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderReview.select("dd").get(3).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for ACCEPTED" in {
        docWithFinancialPointAppealAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealAccepted.select("dd").get(3).text() shouldBe "Appeal accepted Read outcome message"
      }

      "have the appeal status for ACCEPTED - no read message for agents" in {
        docWithFinancialPointAppealAcceptedAgent.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealAcceptedAgent.select("dd").get(3).text() shouldBe "Appeal accepted"
      }

      "have the appeal status for REJECTED" in {
        docWithFinancialPointAppealRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealRejected.select("dd").get(3).text() shouldBe "Appeal rejected Read outcome message"
      }

      "have the appeal status for REJECTED - no read message for agent" in {
        docWithFinancialPointAppealRejectedAgent.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealRejectedAgent.select("dd").get(3).text() shouldBe "Appeal rejected"
      }

      "have the appeal status for REINSTATED" in {
        docWithFinancialPointAppealReinstated.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealReinstated.select("dd").get(3).text() shouldBe "Appeal outcome changed Read message"
      }

      "have the appeal status for REINSTATED - no read outcome message for agents" in {
        docWithFinancialPointAppealReinstatedAgent.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealReinstatedAgent.select("dd").get(3).text() shouldBe "Appeal outcome changed"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in {
        docWithFinancialPointAppealUnderTribunalReview.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealUnderTribunalReview.select("dd").get(3).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithFinancialPointAppealTribunalRejected.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalRejected.select("dd").get(3).text() shouldBe "Appeal rejected by tax tribunal Read outcome message"
      }

      "have the appeal status for TRIBUNAL REJECTED - no read outcome message for agents" in {
        docWithFinancialPointAppealTribunalRejectedAgent.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalRejectedAgent.select("dd").get(3).text() shouldBe "Appeal rejected by tax tribunal"
      }

      "have the appeal status for ACCEPTED BY TRIBUNAL" in {
        docWithFinancialPointAppealTribunalAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalAccepted.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal Read outcome message"
      }

      "have the appeal status for ACCEPTED BY TRIBUNAL - no read outcome message for agents" in {
        docWithFinancialPointAppealTribunalAcceptedAgent.select("dt").get(3).text() shouldBe "Appeal status"
        docWithFinancialPointAppealTribunalAcceptedAgent.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal"
      }
    }

    "given an appealed point" should {
      val docWithAppealedPoint: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPoint))
      val docWithAppealedPointUnderTribunalReview: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointUnderTribunalReview))
      val docWithAppealedPointAccepted: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointAccepted))
      val docWithAppealedPointAcceptedByTribunal: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointAcceptedByTribunal))
      val docWithAppealedPointRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointRejected))
      val docWithAppealedPointUnderTribunalRejected: Document = asDocument(summaryCardHtml.apply(summaryCardModelWithAppealedPointTribunalRejected))

      "not show the appeal link" in {
        docWithAppealedPoint.select(".app-summary-card__footer a").isEmpty shouldBe true
      }

      "have the appeal status for UNDER_REVIEW" in {
        docWithAppealedPoint.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPoint.select("dd").get(4).text() shouldBe "Under review by HMRC"
      }

      "have the appeal status for UNDER_TRIBUNAL_REVIEW" in {
        docWithAppealedPointUnderTribunalReview.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalReview.select("dd").get(4).text() shouldBe "Under review by the tax tribunal"
      }

      "have the appeal status for ACCEPTED - removing the point due to expire and point number" in {
        docWithAppealedPointAccepted.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAccepted.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAccepted.select("dd").get(3).text() shouldBe "Appeal accepted Read outcome message"
        docWithAppealedPointAccepted.select("h3").get(0).text() shouldBe "Penalty point"
      }

      "have the appeal status for ACCEPTED_BY_TRIBUNAL - removing the point due to expire and point number" in {
        docWithAppealedPointAcceptedByTribunal.select("dt").text().contains("Point due to expire") shouldBe false
        docWithAppealedPointAcceptedByTribunal.select("dt").get(3).text() shouldBe "Appeal status"
        docWithAppealedPointAcceptedByTribunal.select("dd").get(3).text() shouldBe "Appeal accepted by tax tribunal Read outcome message"
        docWithAppealedPointAcceptedByTribunal.select("h3").get(0).text() shouldBe "Penalty point"
      }

      "have the appeal status for REJECTED" in {
        docWithAppealedPointRejected.select("dt").text().contains("Point due to expire") shouldBe true
        docWithAppealedPointRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointRejected.select("dd").get(4).text() shouldBe "Appeal rejected Read outcome message"
      }

      "have the appeal status for TRIBUNAL REJECTED" in {
        docWithAppealedPointUnderTribunalRejected.select("dt").get(4).text() shouldBe "Appeal status"
        docWithAppealedPointUnderTribunalRejected.select("dd").get(4).text() shouldBe "Appeal rejected by tax tribunal Read outcome message"
      }
    }
  }

}
