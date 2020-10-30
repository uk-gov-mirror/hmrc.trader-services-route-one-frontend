/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.traderservices.services

import javax.inject.{Inject, Singleton}
import play.api.libs.json.Format
import uk.gov.hmrc.cache.repository.CacheMongoRepository
import uk.gov.hmrc.crypto.ApplicationCrypto
import uk.gov.hmrc.traderservices.journeys.{TraderServicesFrontendJourneyModel, TraderServicesFrontendJourneyStateFormats}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.fsm.PersistentJourneyService

trait TraderServicesFrontendJourneyService[RequestContext] extends PersistentJourneyService[RequestContext] {

  val journeyKey = "TraderServicesJourney"

  override val model = TraderServicesFrontendJourneyModel

  // do not keep errors or transient states in the journey history
  override val breadcrumbsRetentionStrategy: Breadcrumbs => Breadcrumbs =
    _.filterNot(s => s.isInstanceOf[model.IsError] || s.isInstanceOf[model.IsTransient])
}

trait TraderServicesFrontendJourneyServiceWithHeaderCarrier extends TraderServicesFrontendJourneyService[HeaderCarrier]

@Singleton
case class MongoDBCachedTraderServicesFrontendJourneyService @Inject() (
  cacheMongoRepository: CacheMongoRepository,
  applicationCrypto: ApplicationCrypto
) extends MongoDBCachedJourneyService[HeaderCarrier] with TraderServicesFrontendJourneyServiceWithHeaderCarrier {

  override val stateFormats: Format[model.State] =
    TraderServicesFrontendJourneyStateFormats.formats

  override def getJourneyId(hc: HeaderCarrier): Option[String] =
    hc.extraHeaders.find(_._1 == journeyKey).map(_._2)

}
