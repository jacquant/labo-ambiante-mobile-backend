package com.labo_iot

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.labo_iot.SourceRegistry._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._

import scala.concurrent.Future

class SourceRoutes(sourceRegistry: ActorRef[SourceRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getSources(): Future[Sources] = sourceRegistry.ask(GetSources(_))

  def getSource(id: String): Future[GetSourceResponse] = sourceRegistry.ask(GetSource(id, _))

  def createSource(source: Source): Future[SourceActionPerformed] = sourceRegistry.ask(CreateSource(source, _))

  def updateSource(source: Source): Future[SourceActionPerformed] = sourceRegistry.ask(UpdateSource(source, _))

  def deleteSource(id: String): Future[SourceActionPerformed] = sourceRegistry.ask(DeleteSource(id, _))

  @GET
  @Path("/sources")
  @Operation(summary = "Get all sources",
    tags = Array("Sources"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Sources])))))
  )
  def getSourcesRoute =
    path("sources") { get {
          complete(getSources())
        }
      }

  @GET
  @Path("/sources/{sourceId}")
  @Operation(
    summary = "Find a source by ID",
    tags = Array("Sources"),
    parameters = Array(
      new Parameter(name = "sourceId", in = ParameterIn.PATH, required = true, description = "Id of the source to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Source])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "Source not found")
    )
  )
  def getSourceRoute = get {
    path("sources" / Segment) {
      sourceId =>
        rejectEmptyResponse {
          onSuccess(getSource(sourceId)) { response =>
            complete(response.maybeSource)
          }
        }
    }
  }

  @Path("/sources")
  @POST
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Create a source",
    tags = Array("Sources"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Source])))),
    responses = Array(new ApiResponse(responseCode = "201", description = "Created"))
  )
  def createSourceRoute = post {
    path("sources") {
      entity(as[Source]) { source =>
        onSuccess(createSource(source)) { performed =>
          complete((StatusCodes.Created, performed))
        }
      }
    }
  }

  @Path("/sources")
  @PUT
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Update an source",
    tags = Array("Sources"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Source])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Ok"))
  )
  def updateSourceRoute = put {
    path("sources") {
      entity(as[Source]) { source =>
        onSuccess(updateSource(source)) { performed =>
          complete((StatusCodes.OK, performed))
        }
      }
    }
  }

  @Path("/sources/{sourceId}")
  @DELETE
  @Operation(
    summary = "Delete an source by ID",
    tags = Array("Sources"),
    parameters = Array(
      new Parameter(name = "sourceId", in = ParameterIn.PATH, required = true, description = "Id of the source to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Source])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "Source not found")
    )
  )
  def deleteSourceRoute = delete {
    path("sources" / Segment) {
      sourceId =>
        onSuccess(deleteSource(sourceId)) { performed =>
          complete((StatusCodes.OK, performed))
        }
    }
  }

  val sourceRoutes: Route = concat(
    getSourcesRoute,
    getSourceRoute,
    createSourceRoute,
    updateSourceRoute,
    deleteSourceRoute
  )
}
