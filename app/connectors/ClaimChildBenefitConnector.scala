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
