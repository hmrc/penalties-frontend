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

package viewmodels

import assets.messages.IndexMessages._
import base.SpecBase
import config.featureSwitches.FeatureSwitching
import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito.{mock, when}
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import services.{ComplianceService, PenaltiesService}
import views.html.components.{warningText, _}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class IndexPageHelperSpec extends SpecBase with FeatureSwitching {
  val penaltiesService: PenaltiesService = injector.instanceOf[PenaltiesService]
  val mockComplianceService: ComplianceService = mock(classOf[ComplianceService])
  val pInjector: p = injector.instanceOf[views.html.components.p]
  val strongInjector: strong = injector.instanceOf[views.html.components.strong]
  val bulletsInjector: bullets = injector.instanceOf[views.html.components.bullets]
  val linkInjector: link = injector.instanceOf[views.html.components.link]
  val warningTextInjector: warningText = injector.instanceOf[views.html.components.warningText]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val pageHelper: IndexPageHelper = new IndexPageHelper(pInjector, strongInjector, bulletsInjector, linkInjector,
    warningTextInjector, penaltiesService, mockComplianceService, errorHandler)

  class Setup {
    when(mockComplianceService.getDESComplianceData(any())(any(), any(), any(), any())).thenReturn(Future.successful(Some(sampleCompliancePayload)))
  }

  "getPluralOrSingularContentForOverview" should {
    "show the singular wording" when {
      "there is only one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, vatTraderUser)
        result.body shouldBe singularOverviewText
      }

      "user is agent - there is only one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, agentUser)
        result.body shouldBe singularAgentOverviewText
      }
    }

    "show the plural wording" when {
      "there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, vatTraderUser)
        result.body shouldBe pluralOverviewText
      }

      "user is agent - there is more than one current point" in {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, agentUser)
        result.body shouldBe pluralAgentOverviewText
      }
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.singular"
      }

      "user is agent - there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.singular"
      }
    }

    "show the plural wording" when {
      "there is more than one total passed in" in {
        val result = pageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.plural"
      }

      "user is agent - there is only one total passed in" in {
        val result = pageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.plural"
      }
    }
  }

  "renderPointsTotal" should {
    "show the text 'Penalty points total' and have the total amount in a span (with a bold class name)" in {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p.govuk-body").text().contains(penaltyPointsTotal) shouldBe true
      parsedHtmlResult.select("span").text shouldBe "1"
      parsedHtmlResult.select("span").hasClass("govuk-!-font-weight-bold") shouldBe true
    }

    "the p class should have a larger font i.e. 27pt" in {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p").hasClass("govuk-!-font-size-27") shouldBe true
    }
  }

  "getGuidanceLink" should {
    "show the text 'Read the guidance about late submission penalties (opens in a new tab)' and have a link to external guidance which opens in a new tab" in {
      val result = pageHelper.getGuidanceLink
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("#guidance-link").text shouldBe externalGuidanceLinkText
      //TODO: change this when we have a GOV.UK guidance page
      parsedHtmlResult.select("#guidance-link").attr("href") shouldBe "#"
      parsedHtmlResult.select("#guidance-link").attr("target") shouldBe "_blank"
    }
  }


  "getContentBasedOnPointsFromModel" should {
    val penaltyDetailsWith3ActivePoints: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = Some(
        LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 3,
            inactivePenaltyPoints = 0,
            regimeThreshold = 4,
            penaltyChargeAmount = 200,
            PoCAchievementDate = LocalDate.of(2022, 1, 1)
          ),
          details = Seq(
            LSPDetails(
              penaltyNumber = "12345679",
              penaltyOrder = "3",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            ),
            LSPDetails(
              penaltyNumber = "12345678",
              penaltyOrder = "2",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            ),
            LSPDetails(
              penaltyNumber = "12345677",
              penaltyOrder = "1",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            )
          )
        )
      ),
      latePaymentPenalty = None
    )

    val penaltyDetailsWith2ActivePoints: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = Some(
        LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 2,
            inactivePenaltyPoints = 0,
            regimeThreshold = 4,
            penaltyChargeAmount = 200,
            PoCAchievementDate = LocalDate.of(2022, 1, 1)
          ),
          details = Seq(
            LSPDetails(
              penaltyNumber = "12345678",
              penaltyOrder = "2",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            ),
            LSPDetails(
              penaltyNumber = "12345677",
              penaltyOrder = "1",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            )
          )
        )
      ),
      latePaymentPenalty = None
    )

    val penaltyDetailsWith1ActivePoint: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = Some(
        LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 1,
            inactivePenaltyPoints = 0,
            regimeThreshold = 4,
            penaltyChargeAmount = 200,
            PoCAchievementDate = LocalDate.of(2022, 1, 1)
          ),
          details = Seq(
            LSPDetails(
              penaltyNumber = "12345678",
              penaltyOrder = "1",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            )
          )
        )
      ),
      latePaymentPenalty = None
    )

    val penaltyDetailsWith1ActivePointAnnual: GetPenaltyDetails = GetPenaltyDetails(
      totalisations = None,
      lateSubmissionPenalty = Some(
        LateSubmissionPenalty(
          summary = LSPSummary(
            activePenaltyPoints = 1,
            inactivePenaltyPoints = 0,
            regimeThreshold = 2,
            penaltyChargeAmount = 200,
            PoCAchievementDate = LocalDate.of(2022, 1, 1)
          ),
          details = Seq(
            LSPDetails(
              penaltyNumber = "12345678",
              penaltyOrder = "1",
              penaltyCategory = LSPPenaltyCategoryEnum.Point,
              penaltyStatus = LSPPenaltyStatusEnum.Active,
              FAPIndicator = None,
              penaltyCreationDate = LocalDate.of(2022, 1, 1),
              penaltyExpiryDate = LocalDate.of(2022, 1, 1),
              expiryReason = None,
              communicationsDate = Some(LocalDate.of(2022, 1, 1)),
              lateSubmissions = Some(
                Seq(
                  LateSubmission(
                    taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                    taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                    returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                    taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                  )
                )
              ),
              appealInformation = None,
              chargeAmount = None,
              chargeOutstandingAmount = None,
              chargeDueDate = None
            )
          )
        )
      ),
      latePaymentPenalty = None
    )

    "no active penalty points" should {
      "display a message in a <p> tag" in {
        val penaltyDetailsWithNoActivePoints: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          lateSubmissionPenalty = Some(
            LateSubmissionPenalty(
              summary = LSPSummary(
                activePenaltyPoints = 0,
                inactivePenaltyPoints = 0,
                regimeThreshold = 5,
                penaltyChargeAmount = 200,
                PoCAchievementDate = LocalDate.of(2022, 1, 1)
              ),
              details = Seq.empty
            )
          ),
          latePaymentPenalty = None
        )
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithNoActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the summary of penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the singular wording when there is only one penalty point" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the plural wording when there is multiple penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "user is agent - show what happens when next submission is late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLateForAgent
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }

      "user is agent - show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplicationForAgent
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the summary of penalty points" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "user is agent - show some warning text explaining what will happen if another submission is late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }

      "user is agent - show a summary of amount of points accrued and returns submitted late" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(3, 3)
      }
    }

    "points are at or above the threshold" should {
      val penaltyDetailsWith4ActivePoints: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 4,
              inactivePenaltyPoints = 0,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345680",
                penaltyOrder = "4",
                penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345679",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345678",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        ),
        latePaymentPenalty = None
      )


      lazy val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
        vatTraderUser, hc, implicitly))
      lazy val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
      lazy val resultForAgent = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
        agentUser, hc, implicitly))
      lazy val parsedHtmlResultForAgent = Jsoup.parse(contentAsString(resultForAgent.getOrElse(Html(""))))

      "show the financial penalty threshold reached text" in new Setup {
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe thresholdReached
        parsedHtmlResult.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "user is agent - show the financial penalty threshold reached text" in new Setup {
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).text shouldBe thresholdReachedAgent
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "show the penalty amount until account is updated text" in {
        parsedHtmlResult.select("p.govuk-body").get(1).text shouldBe lateReturnPenalty
      }

      "user is agent - show the penalty amount until account is updated text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(1).text shouldBe lateReturnPenaltyAgent
      }

      "show the guidance link text" in {
        parsedHtmlResult.select("a.govuk-link").text shouldBe bringAccountUpToDate
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad.url
      }

      "show the LSP OnThreshold message with POCAchievementDate text" in {
        parsedHtmlResult.select("p.govuk-body").get(2).text shouldBe lspOnThresholdMessage
        parsedHtmlResult.select("p.govuk-body strong").text shouldBe "January 2022"
      }

      "user is agent - show the guidance link text" in {
        parsedHtmlResultForAgent.select("a.govuk-link").text shouldBe bringAccountUpToDateAgent
        parsedHtmlResultForAgent.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad.url
      }

      "user is agent - show the LSP OnThreshold message with POCAchievementDate text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(2).text shouldBe lspOnThresholdMessageAgent
        parsedHtmlResultForAgent.select("p.govuk-body strong").text shouldBe "January 2022"
      }

      "show the correct content when there are no open obligations" in new Setup {
        when(mockComplianceService.getDESComplianceData(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(compliancePayloadObligationsFulfilled)))
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(
          implicitly, vatTraderUser, hc, implicitly))
        result.isRight shouldBe true
        val parsedResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedResult.select("p").get(0).text shouldBe traderCompliantContentP
        parsedResult.select("ul li").get(0).text shouldBe traderCompliantBullet1
        parsedResult.select("ul li").get(1).text shouldBe traderCompliantBullet2
      }

      "user is agent - show the correct content when there are no open obligations" in new Setup {
        when(mockComplianceService.getDESComplianceData(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(Some(compliancePayloadObligationsFulfilled)))
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(
          implicitly, agentUser, hc, implicitly))
        result.isRight shouldBe true
        val parsedResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedResult.select("p").get(0).text shouldBe agentCompliantContentP
        parsedResult.select("ul li").get(0).text shouldBe agentCompliantBullet1
        parsedResult.select("ul li").get(1).text shouldBe agentCompliantBullet2
      }

      s"return $Left ISE when the obligation call returns None (no data/network error)" in new Setup {
        when(mockComplianceService.getDESComplianceData(Matchers.any())(Matchers.any(), Matchers.any(), Matchers.any(), Matchers.any()))
          .thenReturn(Future.successful(None))
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
          vatTraderUser, hc, implicitly))
        result.left.get.header.status shouldBe INTERNAL_SERVER_ERROR
      }
    }

    "points have been added" should {
      val penaltyDetailsWithAddedPoints: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 2,
              inactivePenaltyPoints = 0,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345678",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = Some("X"),
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        ),
        latePaymentPenalty = None
      )

      val penaltyDetailsWithAddedPointsAtPenultimate: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 0,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345679",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = Some("X"),
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        ),
        latePaymentPenalty = None
      )

      "show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }
    }

    "points have been removed" should {
      val penaltyDetailsWithRemovedPoints: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 2,
              inactivePenaltyPoints = 1,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345679",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Inactive,
                FAPIndicator = Some("X"),
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345676",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        ),
        latePaymentPenalty = None
      )

      val penaltyDetailsWithRemovedPointsAtPenultimate: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 1,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345679",
                penaltyOrder = "4",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Inactive,
                FAPIndicator = Some("X"),
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345678",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              ),
              LSPDetails(
                penaltyNumber = "12345676",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = Some(
                  Seq(
                    LateSubmission(
                      taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                      taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                      returnReceiptDate = Some(LocalDate.of(2022, 1, 1)),
                      taxReturnStatus = TaxReturnStatusEnum.Fulfilled
                    )
                  )
                ),
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        ),
        latePaymentPenalty = None
      )

      "show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }
    }
  }

  "getContentBasedOnLatePaymentPenaltiesFromModel" should {
    "no active payment penalties" should {
      "display a message in a <p> tag" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          lateSubmissionPenalty = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              details = Seq.empty
            )
          )
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetails)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePaymentPenalty
      }
    }

    "display unpaid VAT text and 'how lpp calculated' link" when {
      val penaltyDetailsUnpaidVAT: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(
          LatePaymentPenalty(
            details = Seq(
              LPPDetails(
                principalChargeReference = "12345678",
                penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
                penaltyChargeCreationDate = Some(LocalDate.of(2022, 1, 1)),
                penaltyStatus = LPPPenaltyStatusEnum.Accruing,
                penaltyAmountPaid = Some(0),
                penaltyAmountOutstanding = Some(144.21),
                LPP1LRDays = None,
                LPP1HRDays = None,
                LPP2Days = None,
                LPP1LRCalculationAmount = None,
                LPP1HRCalculationAmount = None,
                LPP1LRPercentage = None,
                LPP1HRPercentage = None,
                LPP2Percentage = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                penaltyChargeDueDate = Some(LocalDate.of(2022, 1, 1)),
                appealInformation = None,
                principalChargeBillingFrom = LocalDate.of(2022, 1, 1),
                principalChargeBillingTo = LocalDate.of(2022, 1, 1),
                principalChargeDueDate = LocalDate.of(2022, 1, 1),
                principalChargeLatestClearing = None,
                penaltyChargeReference = Some("PEN1234567"),
                LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
                  outstandingAmount = Some(99),
                  timeToPay = None //TODO Create case for user with TTP (PRM-1720)
                )
              )
            )
          )
        )
      )
      "user has outstanding vat to pay" in {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetailsUnpaidVAT)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe unpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        //TODO: change this when we have link to calculation (external guidance) page
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }

      "client has outstanding vat to pay" in {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetailsUnpaidVAT)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe agentClientUnpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        //TODO: change this when we have link to calculation (external guidance) page
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "#"
      }
    }
  }

  //TODO: remove when new WYO complete
  "getWhatYouOweBreakdown" should {

    "return None" when {
      "the user has no outstanding payments" in {
        val penaltyDetailsWithNoOutstandingPayments: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None, lateSubmissionPenalty = None, latePaymentPenalty = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithNoOutstandingPayments)
        result.isEmpty shouldBe true
      }
    }

    "return Some" when {
      "the user has outstanding VAT to pay" in {
        val penaltyDetailsWithOutstandingVAT: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = Some(
            Totalisations(
              LSPTotalValue = Some(100),
              penalisedPrincipalTotal = Some(223.45),
              LPPPostedTotal = Some(0),
              LPPEstimatedTotal = Some(0),
              totalAccountOverdue = None,
              totalAccountPostedInterest = None,
              totalAccountAccruingInterest = None
            )
          ), lateSubmissionPenalty = None, latePaymentPenalty = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithOutstandingVAT)
        result.isDefined shouldBe true
        result.get.body.contains("£223.45 in late VAT") shouldBe true
      }
    }

    "the user has outstanding LPP's to pay - no estimates" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(100),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(144.21),
            LPPEstimatedTotal = Some(0),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = None
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£144.21 in late payment penalties") shouldBe true
    }

    "the user has outstanding LPP's to pay - with estimates" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(100),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(144.21),
            LPPEstimatedTotal = Some(60.24),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = None
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£60.24 in estimated late payment penalties") shouldBe true
    }

    "the user has outstanding VAT and outstanding LPP's" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(100),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(144.21),
            LPPEstimatedTotal = Some(71.57),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = None
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£71.57 in estimated late payment penalties") shouldBe true
      result.get.body.contains("£223.45 in late VAT") shouldBe true
    }

    "the user has outstanding VAT to pay and has other unrelated penalties" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(100),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(144.21),
            LPPEstimatedTotal = Some(71.57),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = None
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£223.45 in late VAT") shouldBe true
      //            result.get.body.contains("other penalties not related to late submission or late payment") shouldBe true
    }
    //
    //      "the user has other unrelated penalties only" in {
    //        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
    //          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 200, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3, penaltyPoints = Seq.empty, latePaymentPenalties = None,
    //          vatOverview = None, otherPenalties = Some(true))
    //        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
    //        result.isDefined shouldBe false
    //      }

    "the user has outstanding VAT to pay and outstanding LSP's" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(144.21),
            LPPEstimatedTotal = Some(71.57),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345678",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Charge,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345676",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£223.45 in late VAT") shouldBe true
      result.get.body.contains("£400 fixed penalties for late submission") shouldBe true
      //            result.get.body.contains("£43.27 in estimated VAT interest") shouldBe true
    }

    "the user has outstanding LSP's" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(0),
            LPPPostedTotal = Some(0),
            LPPEstimatedTotal = Some(0),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345678",
                penaltyOrder = "3",
                penaltyCategory = LSPPenaltyCategoryEnum.Charge,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345676",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£400 fixed penalties for late submission") shouldBe true
    }

    "the user has a single outstanding LSP" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(200),
            penalisedPrincipalTotal = Some(0),
            LPPPostedTotal = Some(0),
            LPPEstimatedTotal = Some(0),
            totalAccountOverdue = None,
            totalAccountPostedInterest = None,
            totalAccountAccruingInterest = None
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 2,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyNumber = "12345677",
                penaltyOrder = "2",
                penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = Some(200),
                chargeOutstandingAmount = Some(200),
                chargeDueDate = Some(LocalDate.of(2022, 1, 1))
              ),
              LSPDetails(
                penaltyNumber = "12345676",
                penaltyOrder = "1",
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyStatus = LSPPenaltyStatusEnum.Active,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = None,
                communicationsDate = Some(LocalDate.of(2022, 1, 1)),
                lateSubmissions = None,
                appealInformation = None,
                chargeAmount = None,
                chargeOutstandingAmount = None,
                chargeDueDate = None
              )
            )
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£200 fixed penalty for late submission") shouldBe true
    }
    //
    //      "the user has outstanding VAT Interest to pay - no estimated interest " in {
    //        val etmpPayloadWithOutstandingPayments: ETMPPayload = ETMPPayload(
    //          pointsTotal = 0, lateSubmissions = 0, adjustmentPointsTotal = 0, fixedPenaltyAmount = 200, penaltyAmountsTotal = 0, penaltyPointsThreshold = 3,
    //          penaltyPoints = Seq.empty,
    //          latePaymentPenalties = None,
    //          vatOverview = Some(
    //            Seq(
    //              OverviewElement(
    //                `type` = AmountTypeEnum.VAT,
    //                amount = 100.00,
    //                crystalizedInterest = Some(10.00)
    //              ),
    //              OverviewElement(
    //                `type` = AmountTypeEnum.Central_Assessment,
    //                amount = 123.45,
    //                crystalizedInterest = Some(11.23)
    //              )
    //            )
    //          ))
    //        val result = pageHelper.getWhatYouOweBreakdown(etmpPayloadWithOutstandingPayments)
    //        result.isDefined shouldBe true
    //        result.get.body.contains("£21.23 in VAT interest") shouldBe true
    //      }
  }

  //TODO: remove V2 suffix when new WYO complete
  "getWhatYouOweBreakdownV2" should {

    "return None" when {
      "the user has no outstanding payments" in {
        val penaltyDetailsWithNoOutstandingPayments: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None, lateSubmissionPenalty = None, latePaymentPenalty = None
        )
        val result = pageHelper.getWhatYouOweBreakdownV2(penaltyDetailsWithNoOutstandingPayments)
        result.isEmpty shouldBe true
      }
    }

    "return Some" when {
      "the user has outstanding VAT to pay" in {
        val penaltyDetailsWithOutstandingVAT: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = Some(
            Totalisations(
              LSPTotalValue = Some(100),
              penalisedPrincipalTotal = None,
              LPPPostedTotal = Some(0),
              LPPEstimatedTotal = Some(0),
              totalAccountOverdue = Some(100.23),
              totalAccountPostedInterest = None,
              totalAccountAccruingInterest = None
            )
          ), lateSubmissionPenalty = None, latePaymentPenalty = None
        )
        val result = pageHelper.getWhatYouOweBreakdownV2(penaltyDetailsWithOutstandingVAT)
        result.isDefined shouldBe true
        result.get.body.contains("unpaid VAT charges") shouldBe true
      }
    }
  }

  "isTTPActive" should {
    "return true" when {
      "a TTP is active and ends today" in new Setup {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 2)))
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }

      "a TTP is active and ends in the future" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 1)))
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }

      "a TTP has been applied in the past but also has a TTP active now" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 6, 20))
                      ),
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 6, 24),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 6, 25)))
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe true
      }
    }

    "return false" when {
      "no TTP field is present" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = None
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      "a TTP was active but has since expired" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 1, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 7, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 3)))
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      "a TTP is going to be active but start date is in the future" in {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              Seq(
                sampleLatePaymentPenalty.copy(LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = None,
                  outstandingAmount = None,
                  timeToPay = Some(
                    Seq(
                      TimeToPay(
                        TTPStartDate = LocalDate.of(2022, 8, 1),
                        TTPEndDate = Some(LocalDate.of(2022, 9, 2))
                      )
                    )
                  )
                ))
              )
            )
          ),
          lateSubmissionPenalty = None
        )
        setFeatureDate(Some(LocalDate.of(2022, 7, 3)))
        val result = pageHelper.isTTPActive(penaltyDetails)
        result shouldBe false
      }

      setFeatureDate(None)
    }
  }

  "filteredExpiredPoints" should {
    def penaltyDetailsWithReason(expiryReason: ExpiryReasonEnum.Value): Seq[LSPDetails] = Seq(
      LSPDetails(
        penaltyNumber = "12345678",
        penaltyOrder = "3",
        penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
        penaltyStatus = LSPPenaltyStatusEnum.Active,
        FAPIndicator = None,
        penaltyCreationDate = LocalDate.of(2022, 1, 1),
        penaltyExpiryDate = LocalDate.of(2022, 1, 1),
        expiryReason = None,
        communicationsDate = Some(LocalDate.of(2022, 1, 1)),
        lateSubmissions = None,
        appealInformation = None,
        chargeAmount = Some(200),
        chargeOutstandingAmount = Some(200),
        chargeDueDate = Some(LocalDate.of(2022, 1, 1))
      ),
      LSPDetails(
        penaltyNumber = "12345677",
        penaltyOrder = "2",
        penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
        penaltyStatus = LSPPenaltyStatusEnum.Inactive,
        FAPIndicator = None,
        penaltyCreationDate = LocalDate.of(2022, 1, 1),
        penaltyExpiryDate = LocalDate.of(2022, 1, 1),
        expiryReason = Some(expiryReason),
        communicationsDate = Some(LocalDate.of(2022, 1, 1)),
        lateSubmissions = None,
        appealInformation = None,
        chargeAmount = Some(200),
        chargeOutstandingAmount = Some(200),
        chargeDueDate = Some(LocalDate.of(2022, 1, 1))
      ),
      LSPDetails(
        penaltyNumber = "12345676",
        penaltyOrder = "1",
        penaltyCategory = LSPPenaltyCategoryEnum.Point,
        penaltyStatus = LSPPenaltyStatusEnum.Active,
        FAPIndicator = None,
        penaltyCreationDate = LocalDate.of(2022, 1, 1),
        penaltyExpiryDate = LocalDate.of(2022, 1, 1),
        expiryReason = None,
        communicationsDate = Some(LocalDate.of(2022, 1, 1)),
        lateSubmissions = None,
        appealInformation = None,
        chargeAmount = None,
        chargeOutstandingAmount = None,
        chargeDueDate = None
      )
    )

    "not remove points that have not expired (or are not covered under the removal list)" in new Setup {
      def expectedResult(expiryReason: ExpiryReasonEnum.Value): Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "12345678",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345677",
          penaltyOrder = "2",
          penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
          penaltyStatus = LSPPenaltyStatusEnum.Inactive,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = Some(expiryReason),
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345676",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      )
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Adjustment)) shouldBe expectedResult(ExpiryReasonEnum.Adjustment)
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Appeal)) shouldBe expectedResult(ExpiryReasonEnum.Appeal)
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Manual)) shouldBe expectedResult(ExpiryReasonEnum.Manual)
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Reset)) shouldBe expectedResult(ExpiryReasonEnum.Reset)
    }

    "filter out points where expiry reason is covered under the removal list" in new Setup {
      val expectedResult: Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "12345678",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Threshold,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345676",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      )
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Reversal)) shouldBe expectedResult
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.NaturalExpiration)) shouldBe expectedResult
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.SubmissionOnTime)) shouldBe expectedResult
      pageHelper.filteredExpiredPoints(penaltyDetailsWithReason(ExpiryReasonEnum.Compliance)) shouldBe expectedResult
    }
  }

  "sortPointsInDescendingOrder" should {
    "sort penalty points in descending order" in {
      val penaltiesOutOfOrder: Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "12345678",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        ),
        LSPDetails(
          penaltyNumber = "12345677",
          penaltyOrder = "2",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345676",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        )
      )

      val penaltiesInOrder: Seq[LSPDetails] = Seq(
        LSPDetails(
          penaltyNumber = "12345676",
          penaltyOrder = "3",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345677",
          penaltyOrder = "2",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = Some(200),
          chargeOutstandingAmount = Some(200),
          chargeDueDate = Some(LocalDate.of(2022, 1, 1))
        ),
        LSPDetails(
          penaltyNumber = "12345678",
          penaltyOrder = "1",
          penaltyCategory = LSPPenaltyCategoryEnum.Point,
          penaltyStatus = LSPPenaltyStatusEnum.Active,
          FAPIndicator = None,
          penaltyCreationDate = LocalDate.of(2022, 1, 1),
          penaltyExpiryDate = LocalDate.of(2022, 1, 1),
          expiryReason = None,
          communicationsDate = Some(LocalDate.of(2022, 1, 1)),
          lateSubmissions = None,
          appealInformation = None,
          chargeAmount = None,
          chargeOutstandingAmount = None,
          chargeDueDate = None
        )
      )

      val result: Seq[LSPDetails] = pageHelper.sortPointsInDescendingOrder(penaltiesOutOfOrder)
      result shouldBe penaltiesInOrder
    }
  }
}
