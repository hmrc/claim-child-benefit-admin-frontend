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
import models.SubmissionItemStatus
import play.api.Configuration
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.internalauth.client.Predicate.Permission
import uk.gov.hmrc.internalauth.client._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SubmissionsView

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class SupplementaryDataSubmissionsController @Inject()(
                                       val controllerComponents: MessagesControllerComponents,
                                       configuration: Configuration,
                                       auth: FrontendAuthComponents,
                                       view: SubmissionsView,
                                       connector: SupplementaryDataConnector
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val limit: Int = configuration.get[Int]("submissions.limit")

  private val authorised =
    auth.authorizedAction(
      continueUrl = routes.SupplementaryDataSubmissionsController.onPageLoad(),
      predicate = Permission(
        Resource(
          ResourceType("claim-child-benefit-admin"),
          ResourceLocation("supplementary-data")
        ),
        IAAction("ADMIN")
      )
    )

  def onPageLoad(
                  status: Option[SubmissionItemStatus],
                  created: Option[LocalDate],
                  offset: Option[Int]
                ): Action[AnyContent] = authorised.async { implicit request =>
    connector.list(status, created, Some(limit), offset).map { listResult =>
      Ok(view(listResult.summaries, status, created, limit, offset.getOrElse(0), listResult.totalCount))
    }
  }
}
