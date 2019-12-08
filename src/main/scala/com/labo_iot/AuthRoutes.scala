package com.labo_iot

import java.time.LocalDateTime

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.Credentials
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.{Content, Schema}
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import javax.ws.rs.core.HttpHeaders
import javax.ws.rs.{GET, HeaderParam, Path}

import scala.collection.mutable.ArrayBuffer

case class BasicAuthCredentials(username: String, password: String)

case class OAuthToken(access_token: String = java.util.UUID.randomUUID().toString,
                      token_type: String = "bearer",
                      expires_in: Int = 3600)

case class LoggedInUser(basicAuthCredentials: BasicAuthCredentials,
                        oAuthToken: OAuthToken = new OAuthToken,
                        loggedInAt: LocalDateTime = LocalDateTime.now())

class AuthRoutes(/*authRegistry: ActorRef[AuthRegistry.Command]*/)(implicit val system: ActorSystem[_]) {

  import JsonFormats._
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

  private val validBasicAuthCredentials = Seq(BasicAuthCredentials("user", "pass"))
  private val loggedInUsers = ArrayBuffer.empty[LoggedInUser]

  private def BasicAuthAuthenticator(credentials: Credentials) =
    credentials match {
      case p @ Credentials.Provided(_) =>
        validBasicAuthCredentials
          .find(user => user.username == p.identifier && p.verify(user.password))
      case _ => None
    }

  private def oAuthAuthenticator(credentials: Credentials): Option[LoggedInUser] =
    credentials match {
      case p @ Credentials.Provided(_) =>
        loggedInUsers.find(user => p.verify(user.oAuthToken.access_token))
      case _ => None
    }
  //def getFilterDataRoute() = ???

  //def getProfilDataRoute() = ???

  @GET
  @Path("/auth")
  @HeaderParam(HttpHeaders.AUTHORIZATION)
  @Operation(
    tags = Array("Auth"),
    summary = "Authentication",
    description = "Get token (credentials for testing purpose : user:pass) ",
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
          complete(loggedInUser.oAuthToken)
        }
      }
    }

  def getFilterDataRoute =
    path("auth/filter_data") {
      authenticateOAuth2(realm = "api", oAuthAuthenticator) { validToken =>
        complete(s"It worked! user = $validToken")
      }
    }

  def getProfilDataRoute =
    path("auth/profil_data") {
      authenticateOAuth2(realm = "api", oAuthAuthenticator) { validToken =>
        complete(s"It worked! user = $validToken")
      }
    }

  val authRoutes: Route = concat(
    authenticateRoute,
    getFilterDataRoute,
    getProfilDataRoute
  )
}
