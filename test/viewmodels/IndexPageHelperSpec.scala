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

package viewmodels

import assets.messages.IndexMessages._
import base.SpecBase
import config.AppConfig
import models.lpp._
import models.lsp._
import models.{GetPenaltyDetails, Totalisations}
import org.jsoup.Jsoup
import org.mockito.Matchers
import org.mockito.Matchers.any
import org.mockito.Mockito._
import play.api.http.Status.INTERNAL_SERVER_ERROR
import play.api.mvc.Result
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout}
import play.twirl.api.Html
import services.{ComplianceService, PenaltiesService}
import views.html.components.{warningText, _}

import java.time.LocalDate
import scala.concurrent.{ExecutionContext, Future}

class IndexPageHelperSpec extends SpecBase {
  val penaltiesService: PenaltiesService = injector.instanceOf[PenaltiesService]
  val mockComplianceService: ComplianceService = mock(classOf[ComplianceService])
  val pInjector: p = injector.instanceOf[views.html.components.p]
  val strongInjector: strong = injector.instanceOf[views.html.components.strong]
  val bulletsInjector: bullets = injector.instanceOf[views.html.components.bullets]
  val linkInjector: link = injector.instanceOf[views.html.components.link]
  val warningTextInjector: warningText = injector.instanceOf[views.html.components.warningText]
  implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  val mockAppConfig: AppConfig = mock(classOf[AppConfig])

  class Setup(useRealAppConfig: Boolean = false) {
    reset(mockAppConfig)
    val appConfigToUse: AppConfig = if (useRealAppConfig) appConfig else mockAppConfig
    when(mockAppConfig.penaltyChargeAmount).thenReturn("200")
    when(mockComplianceService.getDESComplianceData(any())(any(), any(), any(), any())).thenReturn(Future.successful(Some(sampleCompliancePayload)))
    val pageHelper: IndexPageHelper = new IndexPageHelper(pInjector, strongInjector, bulletsInjector, linkInjector,
      warningTextInjector, penaltiesService, mockComplianceService, errorHandler)(appConfigToUse)
  }

  "getPluralOrSingularContentForOverview" should {
    "show the singular wording" when {
      "there is only one current point" in new Setup {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, vatTraderUser)
        result.body shouldBe singularOverviewText
      }

      "user is agent - there is only one current point" in new Setup {
        val result = pageHelper.getPluralOrSingularContentForOverview(1, 1)(implicitly, agentUser)
        result.body shouldBe singularAgentOverviewText
      }
    }

    "show the plural wording" when {
      "there is more than one current point" in new Setup {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, vatTraderUser)
        result.body shouldBe pluralOverviewText
      }

      "user is agent - there is more than one current point" in new Setup {
        val result = pageHelper.getPluralOrSingularContentForOverview(2, 2)(implicitly, agentUser)
        result.body shouldBe pluralAgentOverviewText
      }
    }
  }

  "getPluralOrSingular" should {
    "show the singular wording" when {
      "there is only one total passed in" in new Setup {
        val result = pageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.singular"
      }

      "user is agent - there is only one total passed in" in new Setup {
        val result = pageHelper.getPluralOrSingular(1)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.singular"
      }
    }

    "show the plural wording" when {
      "there is more than one total passed in" in new Setup {
        val result = pageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, vatTraderUser)
        result.body shouldBe "this.is.a.message.plural"
      }

      "user is agent - there is only one total passed in" in new Setup {
        val result = pageHelper.getPluralOrSingular(2)("this.is.a.message.singular", "this.is.a.message.plural")(implicitly, agentUser)
        result.body shouldBe "agent.this.is.a.message.plural"
      }
    }
  }

  "renderPointsTotal" should {
    "show the text 'Penalty points total' and have the total amount in a span (with a bold class name)" in new Setup {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p.govuk-body").text().contains(penaltyPointsTotal) shouldBe true
      parsedHtmlResult.select("span").text shouldBe "1"
      parsedHtmlResult.select("span").hasClass("govuk-!-font-weight-bold") shouldBe true
    }

    "the p class should have a larger font i.e. 27pt" in new Setup {
      val result = pageHelper.renderPointsTotal(1)
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("p").hasClass("govuk-!-font-size-27") shouldBe true
    }
  }

  "getGuidanceLink" should {
    "show the text 'Find out how late payment penalties are calculated (opens in a new tab)' and have a" +
      " link to external guidance which opens in a new tab for LPP guidance" in new Setup(useRealAppConfig = true) {
      val result = pageHelper.getGuidanceLink(appConfig.lppCalculationGuidanceLink, messages("lpp.penaltiesSummary.howLppCalculated.link"))
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("a").text shouldBe howLppCalculatedLinkText
      parsedHtmlResult.select("a").attr("href") shouldBe appConfig.lppCalculationGuidanceLink
      parsedHtmlResult.select("a").attr("target") shouldBe "_blank"
    }

    "show the text 'Read the guidance about late submission penalties (opens in a new tab)' and have a" +
      " link to external guidance which opens in a new tab for LSP guidance" in new Setup(useRealAppConfig = true) {
      val result = pageHelper.getGuidanceLink(appConfig.lspGuidanceLink, messages("index.guidance.link"))
      val parsedHtmlResult = Jsoup.parse(result.body)
      parsedHtmlResult.select("a").text shouldBe externalLSPGuidanceLinkText
      parsedHtmlResult.select("a").attr("href") shouldBe appConfig.lspGuidanceLink
      parsedHtmlResult.select("a").attr("target") shouldBe "_blank"
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
      latePaymentPenalty = None,
      breathingSpace = None
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
      latePaymentPenalty = None,
      breathingSpace = None
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
      latePaymentPenalty = None,
      breathingSpace = None
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
      latePaymentPenalty = None,
      breathingSpace = None
    )

    "no active penalty points" should {
      "display a message in a <p> tag" in new Setup {
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
          latePaymentPenalty = None,
          breathingSpace = None
        )
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithNoActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "display only the guidance link" when {
      "the user is in Breathing Space (and has LSPs)" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints, isUserInBreathingSpace = true)(implicitly, vatTraderUser, implicitly, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("a").text() shouldBe externalLSPGuidanceLinkText
        parsedHtmlResult.childrenSize() shouldBe 1
      }
    }

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the summary of penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the singular wording when there is only one penalty point" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show the plural wording when there is multiple penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the plural wording when there is multiple penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "user is agent - show what happens when next submission is late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLateForAgent
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }

      "user is agent - show the (threshold) amount of points that need to be accrued before a penalty is applied" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplicationForAgent
      }

      "have a link to the guidance for LSP" in new Setup(useRealAppConfig = true) {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body > a").text shouldBe externalLSPGuidanceLinkText
        parsedHtmlResult.select("p.govuk-body > a").attr("href") shouldBe appConfig.lspGuidanceLink
        parsedHtmlResult.select("p.govuk-body > a").attr("target") shouldBe "_blank"
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the summary of penalty points" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "user is agent - show some warning text explaining what will happen if another submission is late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
      }

      "show a summary of amount of points accrued and returns submitted late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }

      "user is agent - show a summary of amount of points accrued and returns submitted late" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(3, 3)
      }

      "have a link to the guidance for LSP" in new Setup(useRealAppConfig = true) {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body > a").text shouldBe externalLSPGuidanceLinkText
        parsedHtmlResult.select("p.govuk-body > a").attr("href") shouldBe appConfig.lspGuidanceLink
        parsedHtmlResult.select("p.govuk-body > a").attr("target") shouldBe "_blank"
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
        latePaymentPenalty = None,
        breathingSpace = None
      )

      "show the correct content" in new Setup {
        lazy val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
          vatTraderUser, hc, implicitly))
        lazy val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe thresholdReached
        parsedHtmlResult.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
        verify(mockAppConfig, times(1)).penaltyChargeAmount
        parsedHtmlResult.select("p.govuk-body").get(1).text shouldBe lateReturnPenalty
        parsedHtmlResult.select("a.govuk-link").text shouldBe bringAccountUpToDate
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad.url
        parsedHtmlResult.select("p.govuk-body").get(2).text shouldBe lspOnThresholdMessage
        parsedHtmlResult.select("p.govuk-body strong").text shouldBe "January 2022"
      }

      "show the correct content for agent" in new Setup {
        lazy val resultForAgent = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
          agentUser, hc, implicitly))
        lazy val parsedHtmlResultForAgent = Jsoup.parse(contentAsString(resultForAgent.getOrElse(Html(""))))
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).text shouldBe thresholdReachedAgent
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
        parsedHtmlResultForAgent.select("p.govuk-body").get(1).text shouldBe lateReturnPenaltyAgent
        parsedHtmlResultForAgent.select("a.govuk-link").text shouldBe bringAccountUpToDateAgent
        parsedHtmlResultForAgent.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad.url
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
        val result: Either[Result, Html] = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly,
          vatTraderUser, hc, implicitly))
        for (left <- result.left) yield left.header.status shouldBe INTERNAL_SERVER_ERROR
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
                lateSubmissions = Some(Seq(LateSubmission(
                  taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
                  taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
                  taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
                  returnReceiptDate = None,
                  taxReturnStatus = TaxReturnStatusEnum.AddedFAP
                ))),
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
        latePaymentPenalty = None,
        breathingSpace = None
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
        latePaymentPenalty = None,
        breathingSpace = None
      )

      "show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "have a link to the guidance for LSP" in new Setup(useRealAppConfig = true) {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body > a").text shouldBe externalLSPGuidanceLinkText
        parsedHtmlResult.select("p.govuk-body > a").attr("href") shouldBe appConfig.lspGuidanceLink
        parsedHtmlResult.select("p.govuk-body > a").attr("target") shouldBe "_blank"
      }
    }

    "points have been removed" should {
      val penaltyDetailsWithRemovedPoints: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 1,
              inactivePenaltyPoints = 2,
              regimeThreshold = 4,
              penaltyChargeAmount = 200,
              PoCAchievementDate = LocalDate.of(2022, 1, 1)
            ),
            details = Seq(
              LSPDetails(
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyNumber = "123456789",
                penaltyOrder = "3",
                penaltyStatus = LSPPenaltyStatusEnum.Inactive,
                FAPIndicator = Some("X"),
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = Some(ExpiryReasonEnum.Adjustment),
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
                penaltyCategory = LSPPenaltyCategoryEnum.Point,
                penaltyNumber = "123456788",
                penaltyOrder = "2",
                penaltyStatus = LSPPenaltyStatusEnum.Inactive,
                FAPIndicator = None,
                penaltyCreationDate = LocalDate.of(2022, 1, 1),
                penaltyExpiryDate = LocalDate.of(2022, 1, 1),
                expiryReason = Some(ExpiryReasonEnum.Appeal),
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
                penaltyNumber = "123456787",
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
                      returnReceiptDate = None,
                      taxReturnStatus = TaxReturnStatusEnum.Open
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
        latePaymentPenalty = None,
        breathingSpace = None
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
                expiryReason = Some(ExpiryReasonEnum.Adjustment),
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
        latePaymentPenalty = None,
        breathingSpace = None
      )

      "show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 1 penalty point. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 3 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 2 points and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 1 penalty point. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 3 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 2 points and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(
          implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in new Setup {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(
          implicitly, agentUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }

      "have a link to the guidance for LSP" in new Setup(useRealAppConfig = true) {
        val result = await(pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(implicitly, vatTraderUser, hc, implicitly))
        val parsedHtmlResult = Jsoup.parse(contentAsString(result.getOrElse(Html(""))))
        parsedHtmlResult.select("p.govuk-body > a").text shouldBe externalLSPGuidanceLinkText
        parsedHtmlResult.select("p.govuk-body > a").attr("href") shouldBe appConfig.lspGuidanceLink
        parsedHtmlResult.select("p.govuk-body > a").attr("target") shouldBe "_blank"
      }
    }
  }

  "getContentBasedOnLatePaymentPenaltiesFromModel" should {
    "no active payment penalties" should {
      "display a message in a <p> tag" in new Setup {
        val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          lateSubmissionPenalty = None,
          latePaymentPenalty = Some(
            LatePaymentPenalty(
              details = Seq.empty
            )
          ),
          breathingSpace = None
        )
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetails)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePaymentPenalty
      }
    }

    "display only 'how lpp calculated' link" when {
      "the user has LPPs but is in breathing space" in new Setup {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(samplePenaltyDetailsModel, isUserInBreathingSpace = true)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("a").text() shouldBe howLppCalculatedLinkText
        parsedHtmlResult.childrenSize() shouldBe 1
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
                penaltyAmountPaid = None,
                penaltyAmountOutstanding = None,
                penaltyAmountPosted = 0,
                penaltyAmountAccruing = 144.21,
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
        ),
        breathingSpace = None
      )
      "user has outstanding vat to pay" in new Setup(useRealAppConfig = true) {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetailsUnpaidVAT)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe unpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "https://www.gov.uk/guidance/how-late-payment-penalties-work-if-you-pay-vat-late"
      }

      "client has outstanding vat to pay" in new Setup(useRealAppConfig = true) {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetailsUnpaidVAT)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe agentClientUnpaidVATText
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "https://www.gov.uk/guidance/how-late-payment-penalties-work-if-you-pay-vat-late"
      }
    }

    "display just the LPP guidance link" when {
      val penaltyDetailsPaidVAT: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = None,
        lateSubmissionPenalty = None,
        latePaymentPenalty = Some(
          LatePaymentPenalty(
            details = Seq(
              LPPDetails(
                principalChargeReference = "12345678",
                penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
                penaltyChargeCreationDate = Some(LocalDate.of(2022, 1, 1)),
                penaltyStatus = LPPPenaltyStatusEnum.Posted,
                penaltyAmountPaid = None,
                penaltyAmountOutstanding = Some(144.21),
                penaltyAmountPosted = 144.21,
                penaltyAmountAccruing = 0,
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
                principalChargeLatestClearing = Some(LocalDate.of(2022, 1, 1)),
                penaltyChargeReference = Some("PEN1234567"),
                LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
                  outstandingAmount = Some(99),
                  timeToPay = None
                )
              )
            )
          )
        ),
        breathingSpace = None
      )
      "the user has paid their LPP's" in new Setup(useRealAppConfig = true) {
        val result = pageHelper.getContentBasedOnLatePaymentPenaltiesFromModel(penaltyDetailsPaidVAT)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").size() shouldBe 1 //Only contains the link
        parsedHtmlResult.select("p.govuk-body").get(0).childrenSize() shouldBe 1 //Only contains the link
        parsedHtmlResult.select("a.govuk-link").text shouldBe howLppCalculatedLinkText
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe "https://www.gov.uk/guidance/how-late-payment-penalties-work-if-you-pay-vat-late"
      }
    }
  }

  "getWhatYouOweBreakdown" should {

    "return None" when {
      "the user has no outstanding items" in new Setup {
        val penaltyDetailsWithNoOutstandingPayments: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None, lateSubmissionPenalty = None, latePaymentPenalty = None,
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithNoOutstandingPayments)
        result.isEmpty shouldBe true
      }
    }

    "return Some" when {
      "the user has outstanding VAT to pay" in new Setup {
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
          ),
          lateSubmissionPenalty = None,
          latePaymentPenalty = None,
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithOutstandingVAT)
        result.isDefined shouldBe true
        result.get.content.body.contains("unpaid VAT charges") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      "the user has outstanding interest to pay" in new Setup {
        val penaltyDetailsWithOutstandingVAT: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = Some(
            Totalisations(
              LSPTotalValue = Some(100),
              penalisedPrincipalTotal = None,
              LPPPostedTotal = Some(0),
              LPPEstimatedTotal = Some(0),
              totalAccountOverdue = None,
              totalAccountPostedInterest = Some(100),
              totalAccountAccruingInterest = Some(10)
            )
          ),
          lateSubmissionPenalty = None,
          latePaymentPenalty = None,
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithOutstandingVAT)
        result.isDefined shouldBe true
        result.get.content.body.contains("unpaid interest") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      val sampleLPP: LPPDetails = LPPDetails(principalChargeReference = "123456789",
        penaltyCategory = LPPPenaltyCategoryEnum.LPP1,
        penaltyChargeCreationDate = Some(LocalDate.of(2022, 1, 1)),
        penaltyStatus = LPPPenaltyStatusEnum.Posted,
        penaltyAmountPaid = Some(BigDecimal(400)),
        penaltyAmountOutstanding = Some(BigDecimal(10)),
        penaltyAmountPosted = 410,
        penaltyAmountAccruing = 0,
        LPP1LRDays = Some("15"),
        LPP1HRDays = Some("30"),
        LPP2Days = None,
        LPP1LRCalculationAmount = None,
        LPP1HRCalculationAmount = None,
        LPP1LRPercentage = Some(BigDecimal(0.02)),
        LPP1HRPercentage = Some(BigDecimal(0.02)),
        LPP2Percentage = None,
        communicationsDate = Some(LocalDate.of(2022, 1, 1)),
        penaltyChargeDueDate = Some(LocalDate.of(2022, 1, 1)),
        appealInformation = None,
        principalChargeBillingFrom = LocalDate.of(2022, 1, 1),
        principalChargeBillingTo = LocalDate.of(2022, 1, 1).plusMonths(1),
        principalChargeDueDate = LocalDate.of(2022, 1, 1).plusMonths(2).plusDays(6),
        penaltyChargeReference = Some("123456789"),
        principalChargeLatestClearing = Some(LocalDate.of(2022, 1, 1).plusMonths(2).plusDays(7)),
        LPPDetailsMetadata = LPPDetailsMetadata(
          mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
          outstandingAmount = Some(99),
          timeToPay = None
        )
      )

      "the user has 1 unpaid (and not successfully appealed) LPP" in new Setup {
        val penaltyDetailsWithUnpaidLPP: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None, lateSubmissionPenalty = None,
          latePaymentPenalty = Some(LatePaymentPenalty(
            Seq(sampleLPP)
          )),
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithUnpaidLPP)
        result.isDefined shouldBe true
        result.get.content.body.contains("a late payment penalty") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      "the user has > 1 unpaid (and not successfully appealed) LPPs" in new Setup {
        val penaltyDetailsWithUnpaidLPPs: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None, lateSubmissionPenalty = None,
          latePaymentPenalty = Some(LatePaymentPenalty(
            Seq(sampleLPP, sampleLPP)
          )),
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithUnpaidLPPs)
        result.isDefined shouldBe true
        result.get.content.body.contains("late payment penalties") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      val sampleLSP: LSPDetails = LSPDetails(
        penaltyNumber = "123456789",
        penaltyOrder = "1",
        penaltyCategory = LSPPenaltyCategoryEnum.Charge,
        penaltyStatus = LSPPenaltyStatusEnum.Active,
        FAPIndicator = None,
        penaltyCreationDate = LocalDate.of(2022, 1, 1),
        penaltyExpiryDate = LocalDate.of(2024, 1, 1),
        expiryReason = None,
        communicationsDate = Some(LocalDate.of(2022, 1, 1)),
        lateSubmissions = Some(
          Seq(
            LateSubmission(
              taxPeriodStartDate = Some(LocalDate.of(2022, 1, 1)),
              taxPeriodEndDate = Some(LocalDate.of(2022, 1, 1)),
              taxPeriodDueDate = Some(LocalDate.of(2022, 1, 1)),
              returnReceiptDate = None,
              taxReturnStatus = TaxReturnStatusEnum.Open
            )
          )
        ),
        appealInformation = None,
        chargeAmount = Some(200),
        chargeOutstandingAmount = Some(10),
        chargeDueDate = Some(LocalDate.of(2022, 1, 1))
      )
      val sampleSummary = LSPSummary(activePenaltyPoints = 3,
        inactivePenaltyPoints = 0,
        regimeThreshold = 4,
        penaltyChargeAmount = 200,
        PoCAchievementDate = LocalDate.of(2022, 1, 1))

      "the user has 1 unpaid (and not successfully appealed) LSP" in new Setup {
        val penaltyDetailsWithUnpaidLSP: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          lateSubmissionPenalty = Some(LateSubmissionPenalty(
            sampleSummary, Seq(sampleLSP)
          )),
          latePaymentPenalty = None,
          breathingSpace = None)
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithUnpaidLSP)
        result.isDefined shouldBe true
        result.get.content.body.contains("a late submission penalty") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      "the user has > 1 unpaid (and not successfully appealed) LSP" in new Setup {
        val penaltyDetailsWithUnpaidLSP: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          lateSubmissionPenalty = Some(LateSubmissionPenalty(
            sampleSummary, Seq(sampleLSP, sampleLSP)
          )),
          latePaymentPenalty = None,
          breathingSpace = None)
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWithUnpaidLSP)
        result.isDefined shouldBe true
        result.get.content.body.contains("late submission penalties") shouldBe true
        result.get.isAnyFinancialElements shouldBe true
      }

      "the user has 1 LSP" in new Setup {
        val penaltyDetailsWith1LSP: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = None,
          lateSubmissionPenalty = Some(
            LateSubmissionPenalty(
              summary = LSPSummary(
                activePenaltyPoints = 1,
                inactivePenaltyPoints = 0,
                regimeThreshold = 4,
                penaltyChargeAmount = 200,
                PoCAchievementDate = LocalDate.of(2022, 1, 1)
              ),
              details = Seq()
            )
          ),
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWith1LSP)
        result.isDefined shouldBe true
        result.get.content.body.contains("1 late submission penalty point") shouldBe true
        result.get.isAnyFinancialElements shouldBe false
      }

      "the user has > 1 LSP (but less than threshold)" in new Setup {
        val penaltyDetailsWith2LSPs: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = None,
          lateSubmissionPenalty = Some(
            LateSubmissionPenalty(
              summary = LSPSummary(
                activePenaltyPoints = 2,
                inactivePenaltyPoints = 0,
                regimeThreshold = 4,
                penaltyChargeAmount = 200,
                PoCAchievementDate = LocalDate.of(2022, 1, 1)
              ),
              details = Seq()
            )
          ),
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWith2LSPs)
        result.isDefined shouldBe true
        result.get.content.body.contains("2 late submission penalty points") shouldBe true
        result.get.isAnyFinancialElements shouldBe false
      }

      "the user has reached the threshold" in new Setup {
        val penaltyDetailsWith2LSPs: GetPenaltyDetails = GetPenaltyDetails(
          totalisations = None,
          latePaymentPenalty = None,
          lateSubmissionPenalty = Some(
            LateSubmissionPenalty(
              summary = LSPSummary(
                activePenaltyPoints = 4,
                inactivePenaltyPoints = 0,
                regimeThreshold = 4,
                penaltyChargeAmount = 200,
                PoCAchievementDate = LocalDate.of(2022, 1, 1)
              ),
              details = Seq()
            )
          ),
          breathingSpace = None
        )
        val result = pageHelper.getWhatYouOweBreakdown(penaltyDetailsWith2LSPs)
        result.isDefined shouldBe true
        result.get.content.body.contains("the maximum number of late submission penalty points") shouldBe true
        result.get.isAnyFinancialElements shouldBe false
      }
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
    "sort penalty points in descending order" in new Setup {
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

  "isAnyFinancialElementsOwed" should {
    "return true" when {
      "there is VAT due" in new Setup {
        val result: Boolean = pageHelper.isAnyFinancialElementsOwed(100, 0, 0, 0)
        result shouldBe true
      }

      "there is interest due" in new Setup {
        val result: Boolean = pageHelper.isAnyFinancialElementsOwed(0, 100, 0, 0)
        result shouldBe true
      }

      "there is an LPP due" in new Setup {
        val result: Boolean = pageHelper.isAnyFinancialElementsOwed(0, 0, 1, 0)
        result shouldBe true
      }

      "there is an LSP (charge) due" in new Setup {
        val result: Boolean = pageHelper.isAnyFinancialElementsOwed(0, 0, 0, 1)
        result shouldBe true
      }
    }

    "return false" when {
      "there is no VAT, interest, LPP or LSP (charge) due" in new Setup {
        val result: Boolean = pageHelper.isAnyFinancialElementsOwed(0, 0, 0, 0)
        result shouldBe false
      }
    }
  }
}
