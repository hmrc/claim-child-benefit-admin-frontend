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

package forms

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers

class ThrottleFormProviderSpec extends AnyFreeSpec with Matchers with OptionValues {

  private val form = new ThrottleFormProvider()()

  "must bind when given valid data" in {

    val data = Map(
      "value" -> "1337"
    )

    val expected = 1337

    val boundForm = form.bind(data)

    boundForm.errors mustBe empty
    boundForm.value.value mustEqual expected
  }

  "must fail to bind when the value is missing" in {

    val boundForm = form.bind(Map.empty[String, String])
    val field = boundForm("value")

    field.errors.length mustBe 1

    val error = field.error.value

    error.message mustEqual "error.required"
    error.key mustEqual "value"
  }

  "must fail when value is invalid" in {

    val boundForm = form.bind(Map("value" -> "foobar"))
    val field = boundForm("value")

    field.errors.length mustBe 1

    val error = field.error.value

    error.message mustEqual "error.number"
    error.key mustEqual "value"
  }

  "must fail when the number is less than 0" in {

    val boundForm = form.bind(Map("value" -> "-1"))
    val field = boundForm("value")

    field.errors.length mustBe 1

    val error = field.error.value

    error.message mustEqual "error.min"
    error.key mustEqual "value"
  }
}
