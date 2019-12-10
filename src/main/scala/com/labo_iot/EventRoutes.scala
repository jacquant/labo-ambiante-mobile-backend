package com.labo_iot

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.HttpMethods
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import ch.megard.akka.http.cors.scaladsl.settings.CorsSettings
import com.labo_iot.EventRegistry._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._

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
    tags = Array("Events"),
    parameters = Array(
      new Parameter(name = "category", in = ParameterIn.QUERY, required = false, description = "Filter on category"),
      new Parameter(name = "city", in = ParameterIn.QUERY, required = false, description = "Filter on city"),
      new Parameter(name = "source", in = ParameterIn.QUERY, required = false, description = "Filter on source"),
      new Parameter(name = "start_time_max", in = ParameterIn.QUERY, required = false, description = "Filter on maximum start time (yyyy-mm-ddThh:mm:ss format)"),
      new Parameter(name = "start_time_min", in = ParameterIn.QUERY, required = false, description = "Filter on minimun start time (yyyy-mm-ddThh:mm:ss format)"),
      new Parameter(name = "sound_level_max", in = ParameterIn.QUERY, required = false, description = "Filter on maximum sound level"),
      new Parameter(name = "sound_level_min", in = ParameterIn.QUERY, required = false, description = "Filter on minimum sound level")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Events])))))
  )
  def getEventsRoute =
    path("events") {
      parameters(Symbol("category").?, Symbol("city").?, Symbol("source").?,
        Symbol("start_time_max").?, Symbol("start_time_min").?,
        Symbol("sound_level_max").?, Symbol("sound_level_min").?)
      { (category, city, source, start_time_max, start_time_min, sound_level_max, sound_level_min) =>
        get {
          cors() {
            complete(getEvents(new Params(category, city, source, start_time_max, start_time_min,
              sound_level_max, sound_level_min)))
          }
        }
      }
    }

  @GET
  @Path("/events/{eventId}")
  @Operation(
    summary = "Find an event by ID",
    tags = Array("Events"),
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
            cors() {
              complete(response.maybeEvent)
            }
          }
        }
    }
  }

  @Path("/events")
  @POST
  @Consumes(value = Array("application/json"))
  @Operation(
    tags = Array("Events"),
    summary = "Create an event",
    description = "start_time and end_time must be on yyyy-mm-ddThh:mm:ss format to work properly",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Event])))),
    responses = Array(new ApiResponse(responseCode = "201", description = "Created"))
  )
  def createEventRoute = post {
    path("events") {
      entity(as[Event]) { event =>
        onSuccess(createEvent(event)) { performed =>
          val methods = CorsSettings.defaultSettings.allowedMethods :+ HttpMethods.OPTIONS :+ HttpMethods.PUT
          cors(settings = CorsSettings.defaultSettings.withAllowedMethods(methods)) {
            complete((StatusCodes.Created, performed))
          }
        }
      }
    }
  }

  @Path("/events")
  @PUT
  @Consumes(value = Array("application/json"))
  @Operation(
    tags = Array("Events"),
    summary = "Update an event",
    description = "start_time and end_time must be on yyyy-mm-ddThh:mm:ss format to work properly",
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Event])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Ok"))
  )
  def updateEventRoute = put {
    path("events") {
      entity(as[Event]) { event =>
        onSuccess(updateEvent(event)) { performed =>
          cors() {
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }
  }

  @Path("/events/{eventId}")
  @DELETE
  @Operation(
    tags = Array("Events"),
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
          cors() {
            complete((StatusCodes.OK, performed))
          }
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
