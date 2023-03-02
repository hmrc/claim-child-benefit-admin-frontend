package connectors

import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalTo, put, urlPathEqualTo}
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.HeaderNames
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.{Authorization, HeaderCarrier}
import utils.WireMockHelper

class ClaimChildBenefitConnectorSpec extends AnyFreeSpec with Matchers with WireMockHelper with ScalaFutures with IntegrationPatience {

  private lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.claim-child-benefit.port" -> server.port()
      )
      .build()

  private lazy val connector = app.injector.instanceOf[ClaimChildBenefitConnector]

  "addAllowlistEntry" - {

    "must succeed when the server responds with OK" in {

      server.stubFor(
        put(urlPathEqualTo("/claim-child-benefit/allow-list"))
          .withHeader(HeaderNames.AUTHORIZATION, equalTo("auth"))
          .willReturn(
            aResponse()
              .withStatus(OK)
          )
      )

      val hc = HeaderCarrier(authorization = Some(Authorization("auth")))
      connector.addAllowlistEntry("some-nino")(hc).futureValue
    }

    "must fail when the server responds with INTERNAL_SERVER_ERROR" in {

      server.stubFor(
        put(urlPathEqualTo("/claim-child-benefit/allow-list"))
          .withHeader(HeaderNames.AUTHORIZATION, equalTo("auth"))
          .willReturn(
            aResponse()
              .withStatus(INTERNAL_SERVER_ERROR)
          )
      )

      val hc = HeaderCarrier(authorization = Some(Authorization("auth")))
      connector.addAllowlistEntry("some-nino")(hc).failed.futureValue
    }

    "must fail when the connection fails" in {

      server.stubFor(
        put(urlPathEqualTo("/claim-child-benefit/allow-list"))
          .withHeader(HeaderNames.AUTHORIZATION, equalTo("auth"))
          .willReturn(
            aResponse()
              .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
          )
      )

      val hc = HeaderCarrier(authorization = Some(Authorization("auth")))
      connector.addAllowlistEntry("some-nino")(hc).failed.futureValue
    }
  }
}
