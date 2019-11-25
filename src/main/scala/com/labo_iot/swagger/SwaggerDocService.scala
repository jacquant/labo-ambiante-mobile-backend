package com.labo_iot.swagger

import com.github.swagger.akka.SwaggerHttpService
import com.github.swagger.akka.model.Info
import com.labo_iot.EventRoutes

object SwaggerDocService extends SwaggerHttpService {
  override val apiClasses: Set[Class[_]] = Set(classOf[EventRoutes])
  override val host = "127.0.0.1:8080" // the url of your api, not swagger's json endpoint
  override val apiDocsPath = "api-docs" // where you want the swagger-json endpoint exposed
  override val info = Info("Labo IOT - Events API",
    "1.0",
    "Labo IOT - Events API",
    "",
    None,
    None,
    Map.empty) // provides license and other description details
}
