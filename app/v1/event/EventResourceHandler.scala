package v1.event

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}

/**
  * DTO for displaying event information.
  */
case class EventResource(id: String,
                         link: String,
                         title: String,
                         genre: String,
                         description: String,
                         start_time: String,
                         duration: String,
                         city: String,
                         country: String,
                         lat: String,
                         lon: String)

object EventResource {
  /**
    * Mapping to read/write a EventResource out as a JSON value.
    */
    implicit val format: Format[EventResource] = Json.format
}


/**
  * Controls access to the backend data, returning [[EventResource]]
  */
class EventResourceHandler @Inject()(
    routerProvider: Provider[EventRouter],
    eventRepository: EventRepository)(implicit ec: ExecutionContext) {

  def create(eventInput: EventFormInput)(
      implicit mc: MarkerContext): Future[EventResource] = {
    val data = EventData(EventId("999"),
      eventInput.title,
      eventInput.genre,
      eventInput.description,
      eventInput.start_time,
      eventInput.duration,
      eventInput.city,
      eventInput.country,
      eventInput.lat,
      eventInput.lon)
    // We don't actually create the event, so return what we have
    eventRepository.create(data).map { id =>
      createEventResource(data)
    }
  }

  def lookup(id: String)(
      implicit mc: MarkerContext): Future[Option[EventResource]] = {
    val eventFuture = eventRepository.get(EventId(id))
    eventFuture.map { maybeEventData =>
      maybeEventData.map { eventData =>
        createEventResource(eventData)
      }
    }
  }

  def find(implicit mc: MarkerContext): Future[Iterable[EventResource]] = {
    eventRepository.list().map { eventDataList =>
      eventDataList.map(eventData => createEventResource(eventData))
    }
  }

  private def createEventResource(e: EventData): EventResource = {
    EventResource(e.id.toString,
      routerProvider.get.link(e.id),
      e.title,
      e.genre,
      e.description,
      e.start_time,
      e.duration,
      e.city,
      e.country,
      e.lat,
      e.lon)
  }

}
