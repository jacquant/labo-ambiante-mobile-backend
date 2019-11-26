package com.labo_iot.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.labo_iot.{CategoryRoutes, CityRoutes, EventRoutes, SourceRoutes}

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(classOf[EventRoutes], classOf[CategoryRoutes], classOf[CityRoutes], classOf[SourceRoutes])
 // override val host = "0.0.0.0:8181" // the url of your api, not swagger's json endpoint
  override val apiDocsPath = "api-docs" // where you want the swagger-json endpoint exposed
  override val info = Info("Labo IOT - Events API",
    "1.0",
    "Labo IOT - Events API",
    "",
    None,
    None,
    Map.empty) // provides license and other description details
}
