package com.labo_iot

import java.time.LocalDateTime

import akka.actor.typed.scaladsl.AskPattern._
import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.cors
import com.labo_iot.AuthRegistry._
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.{GET, HeaderParam, Path}

import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Future

case class BasicAuthCredentials(username: String, password: String)

case class OAuthToken(access_token: String = java.util.UUID.randomUUID().toString,
                      token_type: String = "bearer",
                      expires_in: Int = 3600)

case class LoggedInUser(basicAuthCredentials: BasicAuthCredentials,
                        oAuthToken: OAuthToken = new OAuthToken,
                        loggedInAt: LocalDateTime = LocalDateTime.now())

class AuthRoutes(authRegistry: ActorRef[AuthRegistry.Command])(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private val validBasicAuthCredentials = Seq(BasicAuthCredentials("user", "pass"),BasicAuthCredentials("user2", "pass2"))
  private val loggedInUsers = ArrayBuffer.empty[LoggedInUser]

  private def BasicAuthAuthenticator(credentials: Credentials) =
    credentials match {
      case p @ Credentials.Provided(_) =>
        validBasicAuthCredentials
          .find(user => user.username == p.identifier && p.verify(user.password))
      case _ => None
    }

  private def OAuthAuthenticator(credentials: Credentials): Option[LoggedInUser] =
    credentials match {
      case p @ Credentials.Provided(_) =>
        loggedInUsers.find(user => p.verify(user.oAuthToken.access_token))
      case _ => None
    }

  // If ask takes more time than this to complete the request is failed
  private implicit val timeout = Timeout.create(system.settings.config.getDuration("my-app.routes.ask-timeout"))

  def getFilterData(username: String): Future[GetFilterDataResponse] =
    authRegistry.ask(GetFilterData(username, _))

  def getEventData(username: String): Future[GetEventDataResponse] =
    authRegistry.ask(GetEventData(username, _))

  @GET
  @Path("/auth")
  @Operation(
    tags = Array("Auth"),
    summary = "Authentication",
    description = "Get token, credentials for testing purpose : user:pass ou user2:pass2",
    security = Array(
      new SecurityRequirement(name = "Basic Authentication")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[OAuthToken]))))
    )
  )
  def authenticateRoute =
    path("auth") {
      authenticateBasic(realm = "auth", BasicAuthAuthenticator) {
        user => get {
          val loggedInUser = LoggedInUser(user)
          loggedInUsers.append(loggedInUser)
          cors() {
            complete(loggedInUser.oAuthToken)
          }
        }
      }
    }

  @GET
  @Path("/auth/filter_data")
  @HeaderParam(HttpHeaders.AUTHORIZATION)
  @Operation(
    tags = Array("Auth"),
    summary = "Get user's filter data",
    description = "Get user's filter data which can be used to filter events on the map",
    security = Array(
      new SecurityRequirement(name = "Basic Authentication")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[FilterData]))))
    )
  )
  def getFilterDataRoute =
    path("auth" / "filter_data") {
      authenticateOAuth2(realm = "api", OAuthAuthenticator) { validToken =>
        rejectEmptyResponse {
          onSuccess(getFilterData(validToken.basicAuthCredentials.username)) {
            response => cors() {
              complete(response.maybeFilterData)
            }
          }
        }
      }
    }

  @GET
  @Path("/auth/event_data")
  @HeaderParam(HttpHeaders.AUTHORIZATION)
  @Operation(
    tags = Array("Auth"),
    summary = "Get user's event data",
    description = "Get user's event data which can be used to create custom event",
    security = Array(
      new SecurityRequirement(name = "Basic Authentication")
    ),
    responses = Array(
      new ApiResponse(responseCode = "200", description = "Ok",
        content = Array(new Content(mediaType = "application/json", schema = new Schema(implementation = classOf[EventData]))))
    )
  )
  def getProfilDataRoute =
    path("auth" / "event_data") {
      authenticateOAuth2(realm = "api", OAuthAuthenticator) { validToken =>
        rejectEmptyResponse {
          onSuccess(getEventData(validToken.basicAuthCredentials.username)) {
            response => cors() {
              complete(response.maybeEventData)
            }
          }
        }
      }
    }

  val authRoutes: Route = concat(
    authenticateRoute,
    getFilterDataRoute,
    getProfilDataRoute
  )
}
