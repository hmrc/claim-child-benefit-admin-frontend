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
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.DailySummariesView

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class SupplementaryDataDailySummaryController @Inject()(
                                        val controllerComponents: MessagesControllerComponents,
                                        auth: FrontendAuthComponents,
                                        view: DailySummariesView,
                                        connector: SupplementaryDataConnector
                                      )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {


  private val authorised =
    auth.authorizedAction(
      continueUrl = routes.SupplementaryDataDailySummaryController.onPageLoad(),
      predicate = Permission(
        Resource(
          ResourceType("claim-child-benefit-admin"),
          ResourceLocation("supplementary-data")
        ),
        IAAction("ADMIN")
      )
    )

  def onPageLoad(): Action[AnyContent] = authorised.async { implicit request =>
    connector.dailySummaries.map { response =>
      Ok(view(response))
    }
  }
}