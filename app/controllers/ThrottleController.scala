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
import play.api.data.Form
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.MessagesControllerComponents
import uk.gov.hmrc.internalauth.client.{FrontendAuthComponents, IAAction, Resource, ResourceLocation, ResourceType}
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ThrottleView

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ThrottleController @Inject() (
                                     override val controllerComponents: MessagesControllerComponents,
                                     auth: FrontendAuthComponents,
                                     view: ThrottleView,
                                     connector: ThrottleConnector,
                                     formProvider: ThrottleFormProvider
                                   )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def form: Form[Int] = formProvider()

  private val authorised =
    auth.authorizedAction(
      continueUrl = routes.ThrottleController.onPageLoad(),
      predicate = Permission(
        Resource(
          ResourceType("claim-child-benefit-admin"),
          ResourceLocation("throttle"),
        ),
        IAAction("ADMIN")
      )
    )

  def onPageLoad() = authorised.async { implicit request =>
    connector.get().map { throttleData =>
      Ok(view(form, throttleData))
    }
  }

  def onSubmit() =
    authorised.async { implicit request =>
      form.bindFromRequest().fold(
        formWithErrors =>
          connector.get().map { throttleData =>
            BadRequest(view(formWithErrors, throttleData))
          },
        limit =>
          connector.setLimit(limit).map { _ =>
            Redirect(routes.ThrottleController.onPageLoad())
              .flashing("claim-child-benefit-admin-notification" -> Messages("throttle.limit.set", limit))
          }
      )
    }
}
