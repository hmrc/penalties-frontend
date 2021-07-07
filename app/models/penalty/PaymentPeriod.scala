
package models.penalty

import java.time.LocalDateTime

import play.api.libs.json.{Json, OFormat}

case class PaymentPeriod (
                           startDate: LocalDateTime,
                           endDate: LocalDateTime,
                           paymentStatus: PaymentStatusEnum.Value
                         )

object PaymentPeriod {
  implicit val format: OFormat[PaymentPeriod] = Json.format[PaymentPeriod]
}