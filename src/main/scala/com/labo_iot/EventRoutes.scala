package com.labo_iot

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.actor.typed.scaladsl.AskPattern._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.labo_iot.EventRegistry._
import io.swagger.v3.oas.annotations.Operation

import scala.concurrent.Future

class EventRoutes(eventRegistry: ActorRef[EventRegistry.Command])(implicit val system: ActorSystem[_]) {

  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getEvents(): Future[Events] =
    eventRegistry.ask(GetEvents)
  def getEvent(id: String): Future[GetEventResponse] =
    eventRegistry.ask(GetEvent(id, _))
  def createEvent(event: Event): Future[ActionPerformed] =
    eventRegistry.ask(CreateEvent(event, _))
  def deleteEvent(id: String): Future[ActionPerformed] =
    eventRegistry.ask(DeleteEvent(id, _))

  @Operation(summary = "Event Routes")
  val eventRoutes: Route =
    pathPrefix("events") {
      concat(
        //events-get-delete
        pathEnd {
          concat(
            get {
              complete(getEvents())
            },
            post {
              entity(as[Event]) { event =>
                onSuccess(createEvent(event)) { performed =>
                  complete((StatusCodes.Created, performed))
                }
              }
            })
        },
        //events-get-delete
        //events-get-post
        path(Segment) { id =>
          concat(
            get {
              //retrieve-event-info
              rejectEmptyResponse {
                onSuccess(getEvent(id)) { response =>
                  complete(response.maybeEvent)
                }
              }
              //retrieve-event-info
            },
            delete {
              //events-delete-logic
              onSuccess(deleteEvent(id)) { performed =>
                complete((StatusCodes.OK, performed))
              }
              //events-delete-logic
            })
        })
      //events-get-delete
    }
}
