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
import io.swagger.v3.oas.annotations.parameters.RequestBody
import javax.ws.rs.{Consumes, DELETE, GET, POST, PUT, Path}

import scala.concurrent.Future

class EventRoutes(eventRegistry: ActorRef[EventRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getEvents(params: Params): Future[Events] = eventRegistry.ask(GetEvents(params, _))

  def getEvent(id: String): Future[GetEventResponse] = eventRegistry.ask(GetEvent(id, _))

  def createEvent(event: Event): Future[ActionPerformed] = eventRegistry.ask(CreateEvent(event, _))

  def updateEvent(event: Event): Future[ActionPerformed] = eventRegistry.ask(UpdateEvent(event, _))

  def deleteEvent(id: String): Future[ActionPerformed] = eventRegistry.ask(DeleteEvent(id, _))

  @GET
  @Path("/events")
  @Operation(summary = "Get all events",
    parameters = Array(
      new Parameter(name = "category", in = ParameterIn.QUERY, required = false, description = "Filter on category"),
      new Parameter(name = "city", in = ParameterIn.QUERY, required = false, description = "Filter on city")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Events])))))
  )
  def getEventsRoute =
    path("events") {
      parameters(Symbol("category").?, Symbol("city").?) { (category, city) =>
        get {
          complete(getEvents(new Params(category, city)))
        }
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
    path("events" / Segment) {
      eventId =>
        rejectEmptyResponse {
          onSuccess(getEvent(eventId)) { response =>
            complete(response.maybeEvent)
          }
        }
    }
  }

  @Path("/events")
  @POST
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Create an event",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Event])))),
    responses = Array(new ApiResponse(responseCode = "201", description = "Created"))
  )
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
  @PUT
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Update an event",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Event])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Ok"))
  )
  def updateEventRoute = put {
    path("events") {
      entity(as[Event]) { event =>
        onSuccess(updateEvent(event)) { performed =>
          complete((StatusCodes.OK, performed))
        }
      }
    }
  }

  @Path("/events/{eventId}")
  @DELETE
  @Operation(
    summary = "Delete an event by ID",
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
  def deleteEventRoute = delete {
    path("events" / Segment) {
      eventId =>
        onSuccess(deleteEvent(eventId)) { performed =>
          complete((StatusCodes.OK, performed))
        }
    }
  }

  val eventRoutes: Route = concat(
    getEventsRoute,
    getEventRoute,
    createEventRoute,
    updateEventRoute,
    deleteEventRoute
  )
}
