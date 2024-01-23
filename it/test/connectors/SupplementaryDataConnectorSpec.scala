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
import models.{DailySummary, DailySummaryResponse, ListResult, Metadata, ObjectSummary, SubmissionItem, SubmissionItemStatus, SubmissionSummary}
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.Application
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier
import utils.WireMockHelper

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, LocalDate, ZoneOffset}

class SupplementaryDataConnectorSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with WireMockHelper
    with OptionValues {

  private lazy val app: Application =
    GuiceApplicationBuilder()
      .configure("microservice.services.claim-child-benefit.port" -> server.port())
      .build()

  private lazy val connector = app.injector.instanceOf[SupplementaryDataConnector]

  private val clock = Clock.fixed(Instant.now(), ZoneOffset.UTC)

  "get" - {

    val hc = HeaderCarrier()
    val id = "id"
    val url = s"/claim-child-benefit/supplementary-data/$id"

    val item = SubmissionItem(
      id = id,
      status = SubmissionItemStatus.Submitted,
      objectSummary = ObjectSummary(
        location = "file",
        contentLength = 1337L,
        contentMd5 = "hash",
        lastModified = clock.instant().minus(2, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS)
      ),
      failureReason = None,
      metadata = Metadata(clock.instant().truncatedTo(ChronoUnit.MILLIS), "correlationId"),
      created = clock.instant().truncatedTo(ChronoUnit.SECONDS),
      lastUpdated = clock.instant().truncatedTo(ChronoUnit.SECONDS),
      sdesCorrelationId = "sdesCorrelationId"
    )

    "must return a submission item when the server returns OK and some submission" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(ok(Json.stringify(Json.toJson(item))))
      )

      val result = connector.get(id)(hc).futureValue

      result.value mustEqual item
    }

    "must return None when the server returns NOT_FOUND" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(notFound())
      )

      val result = connector.get(id)(hc).futureValue

      result mustBe None
    }

    "must return a failed future when the server returns another status" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(serverError())
      )

      connector.get(id)(hc).failed.futureValue
    }
  }

  "list" - {

    val hc = HeaderCarrier()
    val url = s"/claim-child-benefit/supplementary-data"
    val listResult = ListResult(
      totalCount = 2,
      List(
        SubmissionSummary("id1", "Submitted", None, Instant.now.truncatedTo(ChronoUnit.MILLIS)),
        SubmissionSummary("id2", "Processed", None, Instant.now.truncatedTo(ChronoUnit.MILLIS))
      )
    )

    "must return a list of submissions when the server returns OK and some submissions" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(ok(Json.toJson(listResult).toString))
      )

      val result = connector.list()(hc).futureValue

      result mustEqual listResult
    }

    "must add filters to the request when passed" in {

      server.stubFor(
        get(urlPathMatching(url))
          .withQueryParam("status", equalTo("completed"))
          .withQueryParam("created", equalTo("2022-02-01"))
          .withQueryParam("limit", equalTo("10"))
          .withQueryParam("offset", equalTo("5"))
          .willReturn(ok(Json.toJson(listResult).toString))
      )

      connector.list(
        status = Some(SubmissionItemStatus.Completed),
        created = Some(LocalDate.of(2022, 2, 1)),
        limit = Some(10),
        offset = Some(5)
      )(hc).futureValue
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(serverError())
      )

      connector.list()(hc).failed.futureValue
    }
  }

  "dailySummaries" - {

    val hc = HeaderCarrier()
    val url = s"/claim-child-benefit/supplementary-data/summaries"

    "must return a list of summaries when the server returns some" in {

      val summaries = List(
        DailySummary(LocalDate.of(2022, 1, 2), 1, 2, 3, 4),
        DailySummary(LocalDate.of(2022, 1, 1), 6, 7, 8, 9)
      )

      val json = Json.obj(
        "summaries" -> summaries
      )

      server.stubFor(
        get(urlMatching(url))
          .willReturn(ok(json.toString))
      )

      val result = connector.dailySummaries(hc).futureValue

      result mustEqual DailySummaryResponse(summaries)
    }

    "must return a failed future when the server returns an error" in {

      server.stubFor(
        get(urlMatching(url))
          .willReturn(serverError())
      )

      connector.dailySummaries(hc).failed.futureValue
    }
  }

  "retry" - {

    val hc = HeaderCarrier()
    val id = "id"
    val url = s"/claim-child-benefit/supplementary-data/$id/retry"

    "must be successful when the server responds with OK" in {

      server.stubFor(
        post(urlMatching(url))
          .willReturn(ok())
      )

      connector.retry(id)(hc).futureValue
    }

    "must fail when the server responds with NOT_FOUND" in {

      server.stubFor(
        post(urlMatching(url))
          .willReturn(notFound())
      )

      connector.retry(id)(hc).failed.futureValue
    }

    "must fail when the server returns an error" in {

      server.stubFor(
        post(urlMatching(url))
          .willReturn(serverError())
      )

      connector.retry(id)(hc).failed.futureValue
    }
  }
}
