package com.labo_iot

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._

import scala.util.{Failure, Success}

import com.labo_iot.swagger.SwaggerDocService

object LaboApp {
  private def startHttpServer(routes: Route, system: ActorSystem[_]): Unit = {
    // Akka HTTP still needs a classic ActorSystem to start
    implicit val classicSystem: akka.actor.ActorSystem = system.toClassic
    import system.executionContext

    val futureBinding = Http().bindAndHandle(routes, "0.0.0.0", 8181)
    futureBinding.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info("Server online at http://{}:{}/", address.getHostString, address.getPort)
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }

  def main(args: Array[String]): Unit = {
    val rootBehavior = Behaviors.setup[Nothing] { context =>
      val eventRegistryActor = context.spawn(EventRegistry(), "EventRegistryActor")
      context.watch(eventRegistryActor)

      val categoryRegistryActor = context.spawn(CategoryRegistry(), "CategoryRegistryActor")
      context.watch(categoryRegistryActor)

      val cityRegistryActor = context.spawn(CityRegistry(), "CityRegistryActor")
      context.watch(cityRegistryActor)

      val sourceRegistryActor = context.spawn(SourceRegistry(), "SourceRegistryActor")
      context.watch(sourceRegistryActor)

      val authRegistryActor = context.spawn(AuthRegistry(), "AuthRegistryActor")
      context.watch(authRegistryActor)

      val routes = new EventRoutes(eventRegistryActor)(context.system).eventRoutes ~
        new CategoryRoutes(categoryRegistryActor)(context.system).categoryRoutes ~
        new CityRoutes(cityRegistryActor)(context.system).cityRoutes ~
        new SourceRoutes(sourceRegistryActor)(context.system).sourceRoutes ~
        new AuthRoutes(authRegistryActor)(context.system).authRoutes ~
        SwaggerDocService.routes ~ getFromResourceDirectory("swagger-ui")

      startHttpServer(routes, context.system)

      Behaviors.empty
    }
    val system = ActorSystem[Nothing](rootBehavior, "HelloAkkaHttpServer")
  }
}
