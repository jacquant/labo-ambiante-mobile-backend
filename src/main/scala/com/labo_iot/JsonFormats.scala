package com.labo_iot

import com.labo_iot.EventRegistry.ActionPerformed
import spray.json.DefaultJsonProtocol

object JsonFormats  {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val eventJsonFormat = jsonFormat17(Event)
  implicit val eventsJsonFormat = jsonFormat1(Events)

  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
