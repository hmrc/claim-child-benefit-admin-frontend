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

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.http.Fault
import models._
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

class ThrottleConnectorSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with WireMockHelper {

  private lazy val app: Application =
    GuiceApplicationBuilder()
      .configure(
        "microservice.services.claim-child-benefit.port" -> server.port(),
      )
      .build()

  private lazy val connector = app.injector.instanceOf[ThrottleConnector]
  private val hc = HeaderCarrier()

  ".setLimit" - {

    val url = "/claim-child-benefit/admin/throttle/limit"
    val request = SetThrottleLimitRequest(1)

    "must return successfully when the server responds with OK" in {

      server.stubFor(
        put(urlMatching(url))
          .withRequestBody(equalToJson(Json.stringify(Json.toJson(request))))
          .willReturn(aResponse().withStatus(OK))
      )

      connector.setLimit(1)(hc).futureValue
    }

    "must fail when the server response with anything else" in {

      server.stubFor(
        put(urlMatching(url))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      connector.setLimit(1)(hc).failed.futureValue
    }

    "must fail when the server connection fails" in {

      server.stubFor(
        put(urlMatching(url))
          .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE))
      )

      connector.setLimit(1)(hc).failed.futureValue
    }
  }

  ".get" - {

    val url = "/claim-child-benefit/admin/throttle"

    "must return throttle data when the server responds with OK" in {

      val response = ThrottleData(count = 1, limit = 2)
      val responseJson = """{"count": 1, "limit": 2}"""

      server.stubFor(
        get(urlMatching(url))
          .willReturn(aResponse().withStatus(OK).withBody(responseJson))
      )

      connector.get()(hc).futureValue mustEqual response
    }

    "must fail when the server response with anything else" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(aResponse().withStatus(INTERNAL_SERVER_ERROR))
      )

      connector.get()(hc).failed.futureValue
    }

    "must fail when the server connection fails" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE))
      )

      connector.get()(hc).failed.futureValue
    }
  }
}
