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

package controllers

import connectors.ThrottleConnector
import forms.ThrottleFormProvider
import models.{Done, ThrottleData}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.{Mockito, MockitoSugar}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.internalauth.client.{FrontendAuthComponents, IAAction, Predicate, Resource, ResourceLocation, ResourceType, Retrieval}
import uk.gov.hmrc.internalauth.client.test.{FrontendAuthComponentsStub, StubBehaviour}
import views.html.ThrottleView

import scala.concurrent.ExecutionContext.global
import scala.concurrent.Future

class ThrottleControllerSpec extends AnyFreeSpec with Matchers with ScalaFutures with MockitoSugar with BeforeAndAfterEach with OptionValues {

  override def beforeEach(): Unit = {
    super.beforeEach()
    Mockito.reset[Any](mockConnector, mockStubBehaviour)
  }

  private val mockConnector: ThrottleConnector = mock[ThrottleConnector]
  private val mockStubBehaviour: StubBehaviour = mock[StubBehaviour]

  private val frontendAuthComponents: FrontendAuthComponents =
    FrontendAuthComponentsStub(mockStubBehaviour)(stubControllerComponents(), global)

  private val app = GuiceApplicationBuilder()
    .overrides(
      bind[ThrottleConnector].toInstance(mockConnector),
      bind[FrontendAuthComponents].toInstance(frontendAuthComponents)
    )
    .build()

  private implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  private val permission = Predicate.Permission(
    resource = Resource(
      resourceType = ResourceType("claim-child-benefit-admin"),
      resourceLocation = ResourceLocation("throttle")
    ),
    action = IAAction("ADMIN")
  )

  private val form = app.injector.instanceOf[ThrottleFormProvider].apply()

  "onPageLoad" - {

    "must display the page when the user is authorised" in {
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      when(mockConnector.get()(any())).thenReturn(Future.successful(ThrottleData(13, 37)))
      val request = FakeRequest(GET, routes.ThrottleController.onPageLoad().url)
        .withSession("authToken" -> "Token some-token")
      val result = route(app, request).value
      val view = app.injector.instanceOf[ThrottleView]
      status(result) mustBe OK
      contentAsString(result) mustEqual view(form, ThrottleData(13, 37))(request, implicitly).toString
      verify(mockStubBehaviour, times(1)).stubAuth(Some(permission), Retrieval.EmptyRetrieval)
      verify(mockConnector, times(1)).get()(any())
    }

    "must fail when the connector fails" in {
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      when(mockConnector.get()(any())).thenReturn(Future.failed(new RuntimeException()))
      val request = FakeRequest(GET, routes.ThrottleController.onPageLoad().url)
        .withSession("authToken" -> "Token some-token")
      route(app, request).value.failed.futureValue
    }

    "must fail when the user is not authenticated" in {
      val request = FakeRequest(GET, routes.ThrottleController.onPageLoad().url) // no auth token
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
    }

    "must fail when the user is not authorised" in {
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.failed(new RuntimeException()))
      val request = FakeRequest(GET, routes.ThrottleController.onPageLoad().url)
        .withSession("authToken" -> "Token some-token")
      route(app, request).value.failed.futureValue
    }
  }

  "onSubmit" - {

    "when a user is authenticated" - {

      "must update the throttle settings and redirect back to the throttle page when a user submits valid data" in {
        when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
        when(mockConnector.setLimit(any())(any())).thenReturn(Future.successful(Done))
        val request = FakeRequest(POST, routes.ThrottleController.onSubmit().url)
          .withSession("authToken" -> "Token some-token")
          .withFormUrlEncodedBody(
            "value" -> "50"
          )
        val result = route(app, request).value
        status(result) mustBe SEE_OTHER
        redirectLocation(result).value mustEqual routes.ThrottleController.onPageLoad().url
        flash(result).get("claim-child-benefit-admin-notification").value mustEqual messages("throttle.limit.set", 50)
        verify(mockStubBehaviour, times(1)).stubAuth(Some(permission), Retrieval.EmptyRetrieval)
        verify(mockConnector, times(1)).setLimit(eqTo(50))(any())
      }

      "must return bad request and display the page with form errors when the user submits invalid data" in {
        when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
        when(mockConnector.setLimit(any())(any())).thenReturn(Future.successful(Done))
        when(mockConnector.get()(any())).thenReturn(Future.successful(ThrottleData(13, 37)))
        val request = FakeRequest(POST, routes.ThrottleController.onSubmit().url)
          .withSession("authToken" -> "Token some-token")
          .withFormUrlEncodedBody()
        val result = route(app, request).value
        val view = app.injector.instanceOf[ThrottleView]
        status(result) mustBe BAD_REQUEST
        contentAsString(result) mustEqual view(form.bind(Map.empty[String, String]), ThrottleData(13, 37))(request, implicitly).toString
        verify(mockConnector, never).setLimit(any())(any())
      }

      "must fail when the user allow list connector fails" in {
        when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
        when(mockConnector.setLimit(any())(any())).thenReturn(Future.failed(new RuntimeException()))
        val request = FakeRequest(POST, routes.ThrottleController.onSubmit().url)
          .withSession("authToken" -> "Token some-token")
          .withFormUrlEncodedBody(
            "value" -> "50"
          )
        route(app, request).value.failed.futureValue
      }
    }

    "must fail when the user is not authenticated" in {
      val request = FakeRequest(POST, routes.ThrottleController.onSubmit().url) // no auth token
      val result = route(app, request).value
      status(result) mustBe SEE_OTHER
    }

    "must fail when the user is not authorised" in {
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.failed(new RuntimeException()))
      val request = FakeRequest(POST, routes.ThrottleController.onSubmit().url)
        .withSession("authToken" -> "Token some-token")
      route(app, request).value.failed.futureValue
    }
  }
}
