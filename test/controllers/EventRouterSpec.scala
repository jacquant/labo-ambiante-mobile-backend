package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.test.CSRFTokenHelper._
import play.api.test.Helpers._
import play.api.test._
import v1.event.EventResource

import scala.concurrent.Future

class EventRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "EventRouter" should {

    "render the list of events" in {
      val request = FakeRequest(GET, "/v1/events").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val events: Seq[EventResource] = Json.fromJson[Seq[EventResource]](contentAsJson(home)).get
      events.filter(_.id == "1").head mustBe (EventResource("1","/v1/events/1", "event 1", "", "", "", "", "", "", 0.0, 0.0))
    }

    "render the list of events when url ends with a trailing slash" in {
      val request = FakeRequest(GET, "/v1/events/").withHeaders(HOST -> "localhost:9000").withCSRFToken
      val home:Future[Result] = route(app, request).get

      val events: Seq[EventResource] = Json.fromJson[Seq[EventResource]](contentAsJson(home)).get
      events.filter(_.id == "1").head mustBe (EventResource("1","/v1/events/1", "event 1", "", "", "", "", "", "", 0.0, 0.0))
    }
  }

}