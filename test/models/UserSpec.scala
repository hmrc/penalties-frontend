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

package models

import base.SpecBase
import uk.gov.hmrc.auth.core.{Enrolment, EnrolmentIdentifier, Enrolments}

class UserSpec extends SpecBase {
  "extractFirstMTDVatEnrolment" should {
    s"return a $Some when there is a HMRC-MTD-VAT enrolment" when {
      "there is only one HMRC-MTD-VAT enrolment" in {
        val enrolments: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Activated"
            )
          )
        )

        User.extractFirstMTDVatEnrolment(enrolments).isDefined shouldBe true
      }

      "there is a HMRC-MTD-VAT enrolment amongst other enrolments" in {
        val mtdVatEnrolment: Enrolment = Enrolment(
          "HMRC-MTD-VAT",
          Seq(
            EnrolmentIdentifier("VRN", vrn)
          ),
          "Activated"
        )
        val enrolments: Enrolments = Enrolments(
          Set(
            mtdVatEnrolment,
            Enrolment(
              "IR-SA",
              Seq(
                EnrolmentIdentifier("UTR", "FAIL")
              ),
              "Activated"
            ),
            Enrolment(
              "IR-CT",
              Seq(
                EnrolmentIdentifier("UTR", "FAIL")
              ),
              "Activated"
            )
          )
        )

        val result = User.extractFirstMTDVatEnrolment(enrolments)
        result.isDefined shouldBe true
        result.get shouldBe vrn
      }
    }

    s"return $None" when {
      "there is an HMRC-MTD-VAT enrolment but it is not activated" in {
        val enrolments: Enrolments = Enrolments(
          Set(
            Enrolment(
              "HMRC-MTD-VAT",
              Seq(
                EnrolmentIdentifier("VRN", vrn)
              ),
              "Not Activated"
            )
          )
        )

        User.extractFirstMTDVatEnrolment(enrolments).isDefined shouldBe false
      }

      "there is no HMRC-MTD-VAT enrolments" in {
        val enrolments: Enrolments = Enrolments(
          Set()
        )

        User.extractFirstMTDVatEnrolment(enrolments).isDefined shouldBe false
      }

      "there is no enrolments" in {
        val enrolments: Enrolments = Enrolments(
          Set(
            Enrolment(
              "IR-SA",
              Seq(
                EnrolmentIdentifier("UTR", "123456789")
              ),
              "Activated"
            ),
            Enrolment(
              "IR-CT",
              Seq(
                EnrolmentIdentifier("UTR", "123456789")
              ),
              "Activated"
            )
          )
        )

        User.extractFirstMTDVatEnrolment(enrolments).isDefined shouldBe false
      }
    }
  }
}
