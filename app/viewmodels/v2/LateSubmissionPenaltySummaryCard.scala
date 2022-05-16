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

import models.v3.appealInfo.{AppealLevelEnum, AppealStatusEnum}
import play.twirl.api.Html
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

case class LateSubmissionPenaltySummaryCard(
                        cardRows: Seq[SummaryListRow],
                        status: Tag,
                        penaltyPoint: String,
                        penaltyId: String,
                        isReturnSubmitted: Boolean,
                        isFinancialPoint: Boolean = false,
                        amountDue: BigDecimal = 0,
                        isAddedPoint: Boolean = false,
                        isAppealedPoint: Boolean = false,
                        appealStatus: Option[AppealStatusEnum.Value] = None,
                        appealLevel: Option[AppealLevelEnum.Value] = None,
                        isAdjustedPoint: Boolean = false,
                        multiplePenaltyPeriod: Option[Html] = None
                      )
