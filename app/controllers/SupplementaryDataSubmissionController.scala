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

import connectors.SupplementaryDataConnector
import play.api.i18n.{I18nSupport, Messages}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmissionView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SupplementaryDataSubmissionController @Inject()(
                                                       val controllerComponents: MessagesControllerComponents,
                                                       connector: SupplementaryDataConnector,
                                                       view: SubmissionView,
                                                       auth: FrontendAuthComponents
                                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private def authorised(id: String) =
    auth.authorizedAction(
      continueUrl = routes.SupplementaryDataSubmissionController.onPageLoad(id),
      predicate = Permission(
        Resource(
          ResourceType("claim-child-benefit-admin"),
          ResourceLocation("supplementary-data")
        ),
        IAAction("ADMIN")
      ),
    )

  def onPageLoad(id: String): Action[AnyContent] =
    authorised(id).async { implicit request =>
      connector.get(id).map {
        _.map(item => Ok(view(item)))
          .getOrElse(NotFound)
      }
    }

  def retry(id: String): Action[AnyContent] =
    authorised(id).async { implicit request =>
      connector.retry(id).map { _ =>
        Redirect(routes.SupplementaryDataSubmissionController.onPageLoad(id))
          .flashing("claim-child-benefit-admin-notification" -> Messages("submission.retryComplete", id))
      }
    }
}
