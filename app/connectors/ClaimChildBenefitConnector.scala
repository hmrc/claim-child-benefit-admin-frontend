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
import models.Done
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ClaimChildBenefitConnector @Inject() (
                                             httpClient: HttpClientV2,
                                             configuration: Configuration
                                           )(implicit ec: ExecutionContext) {

  private val claimChildBenefit: Service = configuration.get[Service]("microservice.services.claim-child-benefit")

  def addAllowlistEntry(nino: String)(implicit hc: HeaderCarrier): Future[Done] =
    httpClient.put(url"$claimChildBenefit/claim-child-benefit/allow-list")
      .withBody(nino)
      .execute
      .map(_ => Done)
}
