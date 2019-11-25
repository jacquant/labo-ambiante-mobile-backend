package com.labo_iot

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.labo_iot.EventRegistry._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs.{DELETE, GET, POST, Path}

import scala.concurrent.Future

class EventRoutes(eventRegistry: ActorRef[EventRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getEvents(): Future[Events] =
    eventRegistry.ask(GetEvents)

  def getEvent(id: String): Future[GetEventResponse] = eventRegistry.ask(GetEvent(id, _))

  def createEvent(event: Event): Future[ActionPerformed] = eventRegistry.ask(CreateEvent(event, _))

  def deleteEvent(id: String): Future[ActionPerformed] = eventRegistry.ask(DeleteEvent(id, _))

  @GET
  @Path("/events")
  @Operation(summary = "Get all events",
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Events])))))
  )
  def getEventsRoute = get {
    path("events") {
      complete(getEvents())
    }
  }

  @GET
  @Path("/events/{eventId}")
  @Operation(
    summary = "Find an event by ID",
    parameters = Array(
      new Parameter(name = "eventId", in = ParameterIn.PATH, required = true, description = "Id of the event to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Event])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "Event not found")
    )
  )
  def getEventRoute = get {
    path("events" / IntNumber) {
      eventId =>
        rejectEmptyResponse {
          onSuccess(getEvent(eventId.toString)) { response =>
            complete(response.maybeEvent)
          }
        }
    }
  }

  @Path("/events")
  @POST
  @Operation(summary = "Create an event")
  def createEventRoute = post {
    path("events") {
      entity(as[Event]) { event =>
        onSuccess(createEvent(event)) { performed =>
          complete((StatusCodes.Created, performed))
        }
      }
    }
  }

  @Path("/events")
  @DELETE
  @Operation(summary = "Delete an event by ID")
  def deleteEventRoute = delete {
    path("events" / IntNumber) {
      eventId =>
        onSuccess(deleteEvent(eventId.toString)) { performed =>
          complete((StatusCodes.OK, performed))
        }
    }
  }

  val eventRoutes: Route = concat(
    getEventsRoute,
    getEventRoute,
    createEventRoute,
    deleteEventRoute
  )
}
