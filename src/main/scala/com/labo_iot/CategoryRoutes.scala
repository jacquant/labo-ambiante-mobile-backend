package com.labo_iot

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.labo_iot.CategoryRegistry._
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.{Operation, Parameter}
import javax.ws.rs._

import scala.concurrent.Future

class CategoryRoutes(categoryRegistry: ActorRef[CategoryRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getCategories(): Future[Categories] = categoryRegistry.ask(GetCategories(_))

  def getCategory(id: String): Future[GetCategoryResponse] = categoryRegistry.ask(GetCategory(id, _))

  def createCategory(category: Category): Future[CategoryActionPerformed] = categoryRegistry.ask(CreateCategory(category, _))

  def updateCategory(category: Category): Future[CategoryActionPerformed] = categoryRegistry.ask(UpdateCategory(category, _))

  def deleteCategory(id: String): Future[CategoryActionPerformed] = categoryRegistry.ask(DeleteCategory(id, _))

  @GET
  @Path("/categories")
  @Operation(summary = "Get all categories",
    tags = Array("Categories"),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Categories])))))
  )
  def getCategoriesRoute =
    path("categories") { get {
          cors() {
            complete(getCategories())
          }
        }
      }

  @GET
  @Path("/categories/{categoryId}")
  @Operation(
    summary = "Find a category by ID",
    tags = Array("Categories"),
    parameters = Array(
      new Parameter(name = "categoryId", in = ParameterIn.PATH, required = true, description = "Id of the category to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Category])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "Category not found")
    )
  )
  def getCategoryRoute = get {
    path("categories" / Segment) {
      categoryId =>
        rejectEmptyResponse {
          onSuccess(getCategory(categoryId)) { response =>
            cors() {
              complete(response.maybeCategory)
            }
          }
        }
    }
  }

  @Path("/categories")
  @POST
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Create a category",
    tags = Array("Categories"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Category])))),
    responses = Array(new ApiResponse(responseCode = "201", description = "Created"))
  )
  def createCategoryRoute = post {
    path("categories") {
      entity(as[Category]) { category =>
        onSuccess(createCategory(category)) { performed =>
          cors() {
            complete((StatusCodes.Created, performed))
          }
        }
      }
    }
  }

  @Path("/categories")
  @PUT
  @Consumes(value = Array("application/json"))
  @Operation(
    summary = "Update an category",
    tags = Array("Categories"),
    requestBody = new RequestBody(content = Array(new Content(schema = new Schema(implementation = classOf[Category])))),
    responses = Array(new ApiResponse(responseCode = "200", description = "Ok"))
  )
  def updateCategoryRoute = put {
    path("categories") {
      entity(as[Category]) { category =>
        onSuccess(updateCategory(category)) { performed =>
          cors() {
            complete((StatusCodes.OK, performed))
          }
        }
      }
    }
  }

  @Path("/categories/{categoryId}")
  @DELETE
  @Operation(
    summary = "Delete an category by ID",
    tags = Array("Categories"),
    parameters = Array(
      new Parameter(name = "categoryId", in = ParameterIn.PATH, required = true, description = "Id of the category to be fetched")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[Category])))),
      new ApiResponse(responseCode = "400", description = "Invalid ID supplied"),
      new ApiResponse(responseCode = "404", description = "Category not found")
    )
  )
  def deleteCategoryRoute = delete {
    path("categories" / Segment) {
      categoryId =>
        onSuccess(deleteCategory(categoryId)) { performed =>
          cors() {
            complete((StatusCodes.OK, performed))
          }
        }
    }
  }

  val categoryRoutes: Route = concat(
    getCategoriesRoute,
    getCategoryRoute,
    createCategoryRoute,
    updateCategoryRoute,
    deleteCategoryRoute
  )
}
