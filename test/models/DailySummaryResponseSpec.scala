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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*
import java.time.LocalDate

class DailySummaryResponseSpec extends AnyFreeSpec with Matchers {
  "DailySummaryResponse" - {

    "serialize to JSON correctly" in {
      val dailySummaries = List(
        DailySummary(LocalDate.parse("2025-01-01"), 10, 5, 0, 5),
        DailySummary(LocalDate.parse("2025-01-02"), 15, 8, 1, 6)
      )
      val response = DailySummaryResponse(dailySummaries)

      val expectedJson: JsValue = Json.parse(
        """
          |{
          |  "summaries": [
          |    {
          |      "date": "2025-01-01",
          |      "submitted": 10,
          |      "forwarded": 5,
          |      "failed": 0,
          |      "completed": 5
          |    },
          |    {
          |      "date": "2025-01-02",
          |      "submitted": 15,
          |      "forwarded": 8,
          |      "failed": 1,
          |      "completed": 6
          |    }
          |  ]
          |}
          """.stripMargin
      )

      Json.toJson(response) mustBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val json: JsValue = Json.parse(
        """
          |{
          |  "summaries": [
          |    {
          |      "date": "2025-01-01",
          |      "submitted": 10,
          |      "forwarded": 5,
          |      "failed": 0,
          |      "completed": 5
          |    },
          |    {
          |      "date": "2025-01-02",
          |      "submitted": 15,
          |      "forwarded": 8,
          |      "failed": 1,
          |      "completed": 6
          |    }
          |  ]
          |}
          """.stripMargin
      )

      val expectedResponse = DailySummaryResponse(
        List(
          DailySummary(LocalDate.parse("2025-01-01"), 10, 5, 0, 5),
          DailySummary(LocalDate.parse("2025-01-02"), 15, 8, 1, 6)
        )
      )

      json.as[DailySummaryResponse] mustBe expectedResponse
    }
  }
}
