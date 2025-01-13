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

class DailySummarySpec extends AnyFreeSpec with Matchers {
  "DailySummary" - {

    "serialize to JSON correctly" in {
      val dailySummary = DailySummary(LocalDate.parse("2025-01-09"), 10, 5, 2, 8)

      val expectedJson: JsValue = Json.parse(
        """
          |{
          |  "date": "2025-01-09",
          |  "submitted": 10,
          |  "forwarded": 5,
          |  "failed": 2,
          |  "completed": 8
          |}
         """.stripMargin
      )

      Json.toJson(dailySummary) mustBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val json: JsValue = Json.parse(
        """
          |{
          |  "date": "2025-01-09",
          |  "submitted": 10,
          |  "forwarded": 5,
          |  "failed": 2,
          |  "completed": 8
          |}
         """.stripMargin
      )

      val expectedDailySummary = DailySummary(LocalDate.parse("2025-01-09"), 10, 5, 2, 8)

      json.as[DailySummary] mustBe expectedDailySummary
    }

    "fail to deserialize invalid JSON" in {
      val invalidJson: JsValue = Json.parse(
        """
          |{
          |  "date": "invalid-date",
          |  "submitted": 10,
          |  "forwarded": 5,
          |  "failed": 2,
          |  "completed": 8
          |}
         """.stripMargin
      )

      invalidJson.validate[DailySummary].isError mustBe true
    }
  }
}
