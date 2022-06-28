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

package viewmodels.v2

import assets.messages.IndexMessages._
import base.SpecBase
import models.v3.{GetPenaltyDetails, Totalisations}
import models.v3.lpp.{LPPDetails, LPPDetailsMetadata, LPPPenaltyCategoryEnum, LPPPenaltyStatusEnum, LatePaymentPenalty, MainTransactionEnum}
import models.v3.lsp._
import org.jsoup.Jsoup
import java.time.LocalDate

class IndexPageHelperSpec extends SpecBase {
  val pageHelper: IndexPageHelper = injector.instanceOf[IndexPageHelper]

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
            penaltyChargeAmount = 200
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
            penaltyChargeAmount = 200
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
            penaltyChargeAmount = 200
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
            penaltyChargeAmount = 200
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
              communicationsDate = LocalDate.of(2022, 1, 1),
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
                penaltyChargeAmount = 200
              ),
              details = Seq.empty
            )
          ),
          latePaymentPenalty = None
        )
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithNoActivePoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").text() shouldBe noActivePenaltyPoints
      }
    }

    "points are below threshold and less than warning level" should {
      "show the summary of penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the summary of penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show the singular wording when there is only one penalty point" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the singular wording when there is only one penalty point" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show the plural wording when there is multiple penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(2, 2)
      }

      "user is agent - show the plural wording when there is multiple penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith2ActivePoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiAgentActivePenaltyPoints(2, 2)
      }

      "show what happens when next submission is late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLate
      }

      "user is agent - show what happens when next submission is late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(2).text() shouldBe whatHappensWhenNextSubmissionIsLateForAgent
      }

      "show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplication
      }

      "user is agent - show the (threshold) amount of points that need to be accrued before a penalty is applied" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePoint)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(3).text() shouldBe quarterlyThresholdPlusOnePenaltyApplicationForAgent
      }
    }

    "points are at warning level (1 below threshold)" should {
      "show the summary of penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularOverviewText
      }

      "user is agent - show the summary of penalty points" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe singularAgentOverviewText
      }

      "show some warning text explaining what will happen if another submission is late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
      }

      "user is agent - show some warning text explaining what will happen if another submission is late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith1ActivePointAnnual)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningTextAgent
      }

      "show a summary of amount of points accrued and returns submitted late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(1).text() shouldBe multiActivePenaltyPoints(3, 3)
      }

      "user is agent - show a summary of amount of points accrued and returns submitted late" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith3ActivePoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
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
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
      val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly, vatTraderUser)
      val parsedHtmlResult = Jsoup.parse(result.body)

      val resultForAgent = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWith4ActivePoints)(implicitly, agentUser)
      val parsedHtmlResultForAgent = Jsoup.parse(resultForAgent.body)

      "show the financial penalty threshold reached text" in {
        parsedHtmlResult.select("p.govuk-body").get(0).text shouldBe thresholdReached
        parsedHtmlResult.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "user is agent - show the financial penalty threshold reached text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).text shouldBe thresholdReachedAgent
        parsedHtmlResultForAgent.select("p.govuk-body").get(0).hasClass("govuk-body govuk-!-font-size-24") shouldBe true
      }

      "show the penalty amount until account is updated text" in {
        parsedHtmlResult.select("p.govuk-body").get(1).text shouldBe lateReturnPenalty
        parsedHtmlResult.select("ul li").get(0).text shouldBe lateReturnPenaltyBullet1
        parsedHtmlResult.select("ul li").get(1).text shouldBe lateReturnPenaltyBullet2
      }

      "user is agent - show the penalty amount until account is updated text" in {
        parsedHtmlResultForAgent.select("p.govuk-body").get(1).text shouldBe lateReturnPenaltyAgent
        parsedHtmlResultForAgent.select("ul li").get(0).text shouldBe lateReturnPenaltyBullet1Agent
        parsedHtmlResultForAgent.select("ul li").get(1).text shouldBe lateReturnPenaltyBullet2Agent
      }

      "show the guidance link text" in {
        parsedHtmlResult.select("a.govuk-link").text shouldBe bringAccountUpToDate
        parsedHtmlResult.select("a.govuk-link").attr("href") shouldBe controllers.routes.ComplianceController.onPageLoad.url
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
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions + adjustmentPointsTotal)" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted a VAT Return late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we added 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithAddedPointsAtPenultimate)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
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
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - show the total of ALL POINTS (i.e. lateSubmissions - adjustmentPointsTotal)" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPoints)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "Your client has 2 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "they have submitted 2 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent them a letter explaining why"
      }

      "all points are 1 below the threshold - show some warning text" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(implicitly, vatTraderUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
        parsedHtmlResult.select("strong").text() shouldBe warningText
        parsedHtmlResult.select("p.govuk-body").get(0).text() shouldBe "You have 3 penalty points. This is because:"
        parsedHtmlResult.select("ul li").get(0).text() shouldBe "you have submitted 4 VAT Returns late"
        parsedHtmlResult.select("ul li").get(1).text() shouldBe "we removed 1 point and sent you a letter explaining why"
      }

      "user is agent - all points are 1 below the threshold - show some warning text" in {
        val result = pageHelper.getContentBasedOnPointsFromModel(penaltyDetailsWithRemovedPointsAtPenultimate)(implicitly, agentUser)
        val parsedHtmlResult = Jsoup.parse(result.body)
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
                penaltyChargeCreationDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
                penaltyChargeDueDate = LocalDate.of(2022, 1, 1),
                appealInformation = None,
                principalChargeBillingFrom = LocalDate.of(2022, 1, 1),
                principalChargeBillingTo = LocalDate.of(2022, 1, 1),
                principalChargeDueDate = LocalDate.of(2022, 1, 1),
                principalChargeLatestClearing = Some(LocalDate.of(2022, 1, 1)),
                penaltyChargeReference = Some("PEN1234567"),
                LPPDetailsMetadata = LPPDetailsMetadata(
                  mainTransaction = Some(MainTransactionEnum.VATReturnCharge),
                  outstandingAmount = Some(99)
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
              LPIPostedTotal = Some(0),
              LPIEstimatedTotal = Some(0)
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 3,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 2,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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
                communicationsDate = LocalDate.of(2022, 1, 1),
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

    "the user has crystalized and estimated interest on penalties" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(0),
            LPPPostedTotal = Some(0),
            LPPEstimatedTotal = Some(0),
            LPIPostedTotal = Some(100),
            LPIEstimatedTotal = Some(20.23)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 0,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
            ),
            details = Seq()
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£120.23 in estimated interest on penalties") shouldBe true
    }

    "the user has just crystalized interest on penalties" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(0),
            LPPPostedTotal = Some(0),
            LPPEstimatedTotal = Some(0),
            LPIPostedTotal = Some(45.10),
            LPIEstimatedTotal = Some(0)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 0,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
            ),
            details = Seq()
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£45.10 in interest on penalties") shouldBe true
    }

    "the user has no estimated or crystalized interest on penalties" in {
      val penaltyDetails: GetPenaltyDetails = GetPenaltyDetails(
        totalisations = Some(
          Totalisations(
            LSPTotalValue = Some(400),
            penalisedPrincipalTotal = Some(223.45),
            LPPPostedTotal = Some(0),
            LPPEstimatedTotal = Some(0),
            LPIPostedTotal = Some(0),
            LPIEstimatedTotal = Some(0)
          )
        ),
        latePaymentPenalty = None,
        lateSubmissionPenalty = Some(
          LateSubmissionPenalty(
            summary = LSPSummary(
              activePenaltyPoints = 0,
              inactivePenaltyPoints = 0,
              regimeThreshold = 2,
              penaltyChargeAmount = 200
            ),
            details = Seq()
          )
        )
      )
      val result = pageHelper.getWhatYouOweBreakdown(penaltyDetails)
      result.isDefined shouldBe true
      result.get.body.contains("£223.45 in late VAT") shouldBe true
      result.get.body.contains("in interest on penalties") shouldBe false
      result.get.body.contains("in estimated interest on penalties") shouldBe false
    }
  }
}
