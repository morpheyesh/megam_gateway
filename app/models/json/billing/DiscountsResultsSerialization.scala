/* 
** Copyright [2013-2015] [Megam Systems]
**
** Licensed under the Apache License, Version 2.0 (the "License");
** you may not use this file except in compliance with the License.
** You may obtain a copy of the License at
**
** http://www.apache.org/licenses/LICENSE-2.0
**
** Unless required by applicable law or agreed to in writing, software
** distributed under the License is distributed on an "AS IS" BASIS,
** WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
** See the License for the specific language governing permissions and
** limitations under the License.
*/
package models.json.tosca


import scalaz._
import scalaz.NonEmptyList._
import Scalaz._
import net.liftweb.json._
import net.liftweb.json.scalaz.JsonScalaz._
import controllers.funnel.SerializationBase
import models.billing._

/**
 * @author morpheyesh
 *
 */
object DiscountsResultsSerialization extends SerializationBase[DiscountsResults] {
  protected val JSONClazKey = controllers.Constants.JSON_CLAZ
  protected val ResultsKey = "results"

  implicit override val writer = new JSONW[DiscountsResults] {
    override def write(h: DiscountsResults): JValue = {
      val nrsList: NonEmptyList[JValue] = h.map {
        nrOpt: Option[DiscountsResult] =>         
            (nrOpt.map { nr: DiscountsResult => nr.toJValue }).getOrElse(JNothing)
      }
      JObject(JField(JSONClazKey,JString("Megam::DiscountsCollection")) :: JField(ResultsKey,JArray(nrsList.list)) :: Nil)
    }
  }

  implicit override val reader = new JSONR[DiscountsResults] {
    override def read(json: JValue): Result[DiscountsResults] = {
      json match {
        case JArray(jObjectList) => {
          val list = jObjectList.flatMap { jValue: JValue =>
            DiscountsResult.fromJValue(jValue) match {
              case Success(nr)   => List(nr)
              case Failure(fail) => List[DiscountsResult]()
            }
          } map { x: DiscountsResult => x.some }
          val nrs: DiscountsResults = list.toNel.getOrElse(nels(none))
          nrs.successNel[Error]
        }
        case j => UnexpectedJSONError(j, classOf[JArray]).failureNel[DiscountsResults]
      }
    }
  }
}