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

import models.SubmissionItemStatus.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.*
import play.api.mvc.QueryStringBindable


class SubmissionItemStatusSpec extends AnyFreeSpec with Matchers {

  "SubmissionItemStatus" - {
    "serialize from JSON correctly" in {
      Json.toJson(SubmissionItemStatus.Submitted: SubmissionItemStatus) mustBe JsString("Submitted")
      Json.toJson(SubmissionItemStatus.Forwarded: SubmissionItemStatus) mustBe JsString("Forwarded")
      Json.toJson(SubmissionItemStatus.Processed: SubmissionItemStatus) mustBe JsString("Processed")
      Json.toJson(SubmissionItemStatus.Failed:    SubmissionItemStatus) mustBe JsString("Failed")
      Json.toJson(SubmissionItemStatus.Completed: SubmissionItemStatus) mustBe JsString("Completed")
    }

    "deserialize from JSON correctly" in {
      JsString("Submitted").validate[SubmissionItemStatus] mustBe JsSuccess(Submitted)
      JsString("Forwarded").validate[SubmissionItemStatus] mustBe JsSuccess(Forwarded)
      JsString("Processed").validate[SubmissionItemStatus] mustBe JsSuccess(Processed)
      JsString("Failed").validate[SubmissionItemStatus] mustBe JsSuccess(Failed)
      JsString("Completed").validate[SubmissionItemStatus] mustBe JsSuccess(Completed)
    }

    "fail to deserialize invalid JSON" in {
      Seq(
        JsString("InvalidStatus").validate[SubmissionItemStatus].isError mustBe true,
        JsNumber(123).validate[SubmissionItemStatus].isError mustBe true,
        JsNull.validate[SubmissionItemStatus].isError mustBe true
      )
    }

    "bind from query string correctly" in {
      val bindable = implicitly[QueryStringBindable[SubmissionItemStatus]]
      Seq(
        bindable.bind("status", Map("status" -> Seq("submitted"))) mustBe Some(Right(Submitted)),
        bindable.bind("status", Map("status" -> Seq("forwarded"))) mustBe Some(Right(Forwarded)),
        bindable.bind("status", Map("status" -> Seq("processed"))) mustBe Some(Right(Processed)),
        bindable.bind("status", Map("status" -> Seq("failed"))) mustBe Some(Right(Failed)),
        bindable.bind("status", Map("status" -> Seq("completed"))) mustBe Some(Right(Completed))
      )
    }

    "fail to bind invalid query string" in {
      val bindable = implicitly[QueryStringBindable[SubmissionItemStatus]]
      Seq(
        bindable.bind("status", Map("status" -> Seq("invalid"))) mustBe Some(Left("status: invalid status")),
        bindable.bind("status", Map.empty) mustBe None
      )
    }

    "unbind to query string correctly" in {
      val bindable = implicitly[QueryStringBindable[SubmissionItemStatus]]
      Seq(
        bindable.unbind("status", Submitted) mustBe "status=submitted",
        bindable.unbind("status", Forwarded) mustBe "status=forwarded",
        bindable.unbind("status", Processed) mustBe "status=processed",
        bindable.unbind("status", Failed) mustBe "status=failed",
        bindable.unbind("status", Completed) mustBe "status=completed"
      )
    }
  }
}
