package com.labo_iot

import com.labo_iot.CategoryRegistry.CategoryActionPerformed
import com.labo_iot.CityRegistry.CityActionPerformed
import com.labo_iot.EventRegistry.ActionPerformed
import com.labo_iot.SourceRegistry.SourceActionPerformed
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val eventJsonFormat = jsonFormat18(Event)
  implicit val eventsJsonFormat = jsonFormat1(Events)

  implicit val categoryJsonFormat = jsonFormat2(Category)
  implicit val categoriesJsonFormat = jsonFormat1(Categories)

  implicit val cityJsonFormat = jsonFormat2(City)
  implicit val citiesJsonFormat = jsonFormat1(Cities)

  implicit val sourceJsonFormat = jsonFormat2(Source)
  implicit val sourcesJsonFormat = jsonFormat1(Sources)

  implicit val oAuthTokenJsonFormat = jsonFormat3(OAuthToken)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
  implicit val categoryActionPerformedJsonFormat = jsonFormat1(CategoryActionPerformed)
  implicit val cityActionPerformedJsonFormat = jsonFormat1(CityActionPerformed)
  implicit val sourceActionPerformedJsonFormat = jsonFormat1(SourceActionPerformed)

}
