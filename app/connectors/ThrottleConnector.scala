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

package connectors

import config.Service
import connectors.ThrottleConnector.UnexpectedResponseException
import models.{Done, SetThrottleLimitRequest, ThrottleData}
import play.api.Configuration
import play.api.http.Status.OK
import play.api.libs.json.Json
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.HttpReads.Implicits.{readRaw, readFromJson}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class ThrottleConnector @Inject()(configuration: Configuration, httpClient: HttpClientV2)
                                 (implicit ec: ExecutionContext) {

  private val claimChildBenefitService: Service = configuration.get[Service]("microservice.services.claim-child-benefit")

  def setLimit(limit: Int)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient.put(url"$claimChildBenefitService/claim-child-benefit/admin/throttle/limit")
      .withBody(Json.toJson(SetThrottleLimitRequest(limit)))
      .execute[HttpResponse]
      .flatMap { response =>
        if (response.status == OK) {
          Future.successful(Done)
        } else {
          Future.failed(UnexpectedResponseException(response.status))
        }
      }

  def get()(implicit hc: HeaderCarrier): Future[ThrottleData] =
    httpClient.get(url"$claimChildBenefitService/claim-child-benefit/admin/throttle")
      .execute[ThrottleData]
}

object ThrottleConnector {

  final case class UnexpectedResponseException(status: Int) extends Exception with NoStackTrace {
    override def getMessage: String = s"Unexpected status: $status"
  }
}