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

import connectors.ClaimChildBenefitConnector
import forms.NinoFormProvider
import generators.NinoGenerator
import models.Done
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, when}
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.inject.bind
import play.api.test.{FakeRequest, Helpers}
import play.api.test.Helpers._
import uk.gov.hmrc.internalauth.client.{FrontendAuthComponents, IAAction, Resource, ResourceLocation, ResourceType, Retrieval}
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client.test.{FrontendAuthComponentsStub, StubBehaviour}
import views.html.AddAllowlistView

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AddAllowlistControllerSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with OptionValues
    with MockitoSugar
    with BeforeAndAfterEach {

  private val mockClaimChildBenefitConnector = mock[ClaimChildBenefitConnector]
  private val mockStubBehaviour = mock[StubBehaviour]
  private val stubFrontendAuthComponents = FrontendAuthComponentsStub(mockStubBehaviour)(Helpers.stubControllerComponents(), implicitly)

  private val app = GuiceApplicationBuilder()
    .overrides(
      bind[FrontendAuthComponents].toInstance(stubFrontendAuthComponents),
      bind[ClaimChildBenefitConnector].toInstance(mockClaimChildBenefitConnector)
    )
    .build()

  private implicit val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  private val predicate = Permission(Resource(ResourceType("claim-child-benefit-admin"), ResourceLocation("allow-list")), IAAction("ADMIN"))

  override def beforeEach(): Unit = {
    Mockito.reset[Any](
      mockClaimChildBenefitConnector,
      mockStubBehaviour
    )
  }

  "onPageLoad" - {

    "must return OK and the correct view when the user is authorised" in {
      val request = FakeRequest(GET, routes.AddAllowlistController.onPageLoad().url)
        .withSession("authToken" -> "Token some-token")
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      val result = route(app, request).value
      val view = app.injector.instanceOf[AddAllowlistView]
      val form = app.injector.instanceOf[NinoFormProvider]

      status(result) mustEqual OK
      contentAsString(result) mustEqual view(form())(request, implicitly).toString
      verify(mockStubBehaviour, times(1)).stubAuth(eqTo(Some(predicate)), eqTo(Retrieval.EmptyRetrieval))
    }

    "must redirect to login when the user is not authenticated" in {
      val request = FakeRequest(GET, routes.AddAllowlistController.onPageLoad().url) // No authToken in session
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual s"/internal-auth-frontend/sign-in?continue_url=%2Fclaim-child-benefit-admin-frontend%2Fallow-list%2Fadd"
    }

    "must fail when the user is not authorised" in {
      when(mockStubBehaviour.stubAuth(eqTo(Some(predicate)), eqTo(Retrieval.EmptyRetrieval))).thenReturn(Future.failed(new Exception("foo")))
      val request = FakeRequest(GET, routes.AddAllowlistController.onPageLoad().url)
        .withSession("authToken" -> "Token some-token")
      route(app, request).value.failed.futureValue
    }
  }

  "onSubmit" - {

    "must return SEE_OTHER and add a flash to indicate the nino was added when the user is authenticated and submits a valid nino" in {
      val nino = NinoGenerator.randomNino()
      val request = FakeRequest(POST, routes.AddAllowlistController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> nino)
        .withSession("authToken" -> "Token some-token")
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      when(mockClaimChildBenefitConnector.addAllowlistEntry(any())(any())).thenReturn(Future.successful(Done))
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual routes.AddAllowlistController.onPageLoad().url
      flash(result).get("claim-child-benefit-allow-list").value mustEqual "add"
      verify(mockStubBehaviour, times(1)).stubAuth(eqTo(Some(predicate)), eqTo(Retrieval.EmptyRetrieval))
      verify(mockClaimChildBenefitConnector, times(1)).addAllowlistEntry(eqTo(nino))(any())
    }

    "must fail when the connector fails" in {
      val nino = NinoGenerator.randomNino()
      val request = FakeRequest(POST, routes.AddAllowlistController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> nino)
        .withSession("authToken" -> "Token some-token")
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      when(mockClaimChildBenefitConnector.addAllowlistEntry(any())(any())).thenReturn(Future.failed(new RuntimeException()))
      route(app, request).value.failed.futureValue
    }

    "must return BAD_REQUEST when the user is authenticated but submits an invalid nino" in {
      val request = FakeRequest(POST, routes.AddAllowlistController.onSubmit().url)
        .withFormUrlEncodedBody("value" -> "foobar")
        .withSession("authToken" -> "Token some-token")
      when(mockStubBehaviour.stubAuth[Unit](any(), any())).thenReturn(Future.unit)
      val result = route(app, request).value
      val view = app.injector.instanceOf[AddAllowlistView]
      val formProvider = app.injector.instanceOf[NinoFormProvider]
      val form = formProvider().bind(Map("value" -> "foobar"))

      status(result) mustEqual BAD_REQUEST
      contentAsString(result) mustEqual view(form)(request, implicitly).toString()
    }

    "must redirect to login when the user is not authenticated" in {
      val request = FakeRequest(POST, routes.AddAllowlistController.onSubmit().url) // No authToken in session
      val result = route(app, request).value

      status(result) mustEqual SEE_OTHER
      redirectLocation(result).value mustEqual s"/internal-auth-frontend/sign-in?continue_url=%2Fclaim-child-benefit-admin-frontend%2Fallow-list%2Fadd"
    }

    "must fail when the user is not authorised" in {
      when(mockStubBehaviour.stubAuth(eqTo(Some(predicate)), eqTo(Retrieval.EmptyRetrieval))).thenReturn(Future.failed(new Exception("foo")))
      val request = FakeRequest(POST, routes.AddAllowlistController.onSubmit().url)
        .withSession("authToken" -> "Token some-token")

      route(app, request).value.failed.futureValue
    }
  }
}
