package com.labo_iot

import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{ Matchers, WordSpec }
import akka.actor.typed.scaladsl.adapter._

//set-up
class EventRoutesSpec extends WordSpec with Matchers with ScalaFutures with ScalatestRouteTest {

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.toClassic

  // Here we need to implement all the abstract members of EventRoutes.
  // We use the real EventRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val eventRegistry = testKit.spawn(EventRegistry())
  lazy val routes = new EventRoutes(eventRegistry).eventRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import JsonFormats._

  //actual-test
  "EventRoutes" should {
    "return no events if no present (GET /events)" in {
      // note that there's no need for the host part in the uri:
      val request = HttpRequest(uri = "/events")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"events":[]}""")
      }
    }

    //testing-post
    "be able to add events (POST /events)" in {
      val event = Event("1","Exposition L'Univers Face A/ Face B","Confluent des Savoirs","2019-09-02","2019-12-13","Dès septembre venez découvrir cette exposition déc...","exposition","5000","NAMUR","Rue Godefroid","5","+32 (0) 81 72 55 64","","https://www.namur.be/fr/agenda/exposition-lunivers-face-a-face-b","50.4663781507","4.8623936774", "namur-agenda-des-evenements")

      val eventEntity = Marshal(event).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/events").withEntity(eventEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.Created)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and we know what message we're expecting back:
        entityAs[String] should ===("""{"description":"Event Exposition L'Univers Face A/ Face B created."}""")
      }
    }

    "be able to remove events (DELETE /events)" in {
      // event the RequestBuilding DSL provided by ScalatestRouteSpec:
      val request = Delete(uri = "/events/1")

      request ~> routes ~> check {
        status should ===(StatusCodes.OK)

        // we expect the response to be json:
        contentType should ===(ContentTypes.`application/json`)

        // and no entries should be in the list:
        entityAs[String] should ===("""{"description":"Event Exposition L'Univers Face A/ Face B deleted."}""")
      }
    }
  }
}
