package com.labo_iot

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.labo_iot.CityRegistry._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._

import scala.concurrent.Future

class CityRoutes(cityRegistry: ActorRef[CityRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getCities(): Future[Cities] = cityRegistry.ask(GetCities(_))

  def getCity(id: String): Future[GetCityResponse] = cityRegistry.ask(GetCity(id, _))

  def createCity(city: City): Future[CityActionPerformed] = cityRegistry.ask(CreateCity(city, _))

  def updateCity(city: City): Future[CityActionPerformed] = cityRegistry.ask(UpdateCity(city, _))

  def deleteCity(id: String): Future[CityActionPerformed] = cityRegistry.ask(DeleteCity(id, _))

  @GET
  @Path("/cities")
  @Operation(summary = "Get all cities",
    tags = Array("Cities"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Cities])))))
  )
  def getCitiesRoute =
    path("cities") { get {
          cors() {
            complete(getCities())
          }
        }
      }

  @GET
  @Path("/cities/{cityId}")
  @Operation(
    summary = "Find a city by ID",
    tags = Array("Cities"),
    parameters = Array(
      new Parameter(name = "cityId", in = ParameterIn.PATH, required = true, description = "Id of the city to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[City])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "City not found")
    )
  )
  def getCityRoute = get {
    path("cities" / Segment) {
      cityId =>
        rejectEmptyResponse {
          onSuccess(getCity(cityId)) { response =>
            cors() {
              complete(response.maybeCity)
            }
          }
        }
    }
  }

  @Path("/cities")
  @POST
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Create a city",
    tags = Array("Cities"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[City])))),
    responses = Array(new ApiResponse(responseCode = "201", description = "Created"))
  )
  def createCityRoute = post {
    path("cities") {
      entity(as[City]) { city =>
        onSuccess(createCity(city)) { performed =>
          cors() {
            complete((StatusCodes.Created, performed))
          }
        }
      }
    }
  }

  @Path("/cities")
  @PUT
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Update an city",
    tags = Array("Cities"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[City])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Ok"))
  )
  def updateCityRoute = put {
    path("cities") {
      entity(as[City]) { city =>
        onSuccess(updateCity(city)) { performed =>
          cors() {
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }
  }

  @Path("/cities/{cityId}")
  @DELETE
  @Operation(
    summary = "Delete an city by ID",
    tags = Array("Cities"),
    parameters = Array(
      new Parameter(name = "cityId", in = ParameterIn.PATH, required = true, description = "Id of the city to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[City])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "City not found")
    )
  )
  def deleteCityRoute = delete {
    path("cities" / Segment) {
      cityId =>
        onSuccess(deleteCity(cityId)) { performed =>
          cors() {
            complete((StatusCodes.OK, performed))
          }
        }
    }
  }

  val cityRoutes: Route = concat(
    getCitiesRoute,
    getCityRoute,
    createCityRoute,
    updateCityRoute,
    deleteCityRoute
  )
}
