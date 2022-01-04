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

import models.point.AppealStatusEnum
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import uk.gov.hmrc.govukfrontend.views.viewmodels.tag.Tag

case class LatePaymentPenaltySummaryCard (
                                          cardRows: Seq[SummaryListRow],
                                          status: Tag,
                                          penaltyId: String,
                                          isPenaltyPaid: Boolean,
                                          amountDue: BigDecimal = 0,
                                          appealStatus: Option[AppealStatusEnum.Value] = None,
                                          isVatPaid: Boolean = false,
                                          isAdditionalPenalty: Boolean = false
                                         )
