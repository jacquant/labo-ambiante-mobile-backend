package v1.event

import javax.inject.{Inject, Provider}
import play.api.MarkerContext
import play.api.libs.json._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

/**
  * DTO for displaying event information.
  */
case class EventResource(id: String,
                         link: String,
                         title: String,
                         organizers: String,
                         start_time: String,
                         end_time: String,
                         description: String,
                         category: String,
                         zip_code: String,
                         city: String,
                         street: String,
                         street_number: String,
                         phone: String,
                         mail: String,
                         website: String,
                         lat: String,
                         lon: String,
                         source: String)

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
    val data = EventData(EventId((Random.nextInt(1000) + 50).toString),
      eventInput.title,
      eventInput.organizers,
      eventInput.start_time,
      eventInput.end_time,
      eventInput.description,
      eventInput.category,
      eventInput.zip_code,
      eventInput.city,
      eventInput.street,
      eventInput.street_number,
      eventInput.phone,
      eventInput.mail,
      eventInput.website,
      eventInput.lat,
      eventInput.lon,
      eventInput.source)

    eventRepository.create(data).map { _ =>
      createEventResource(data)
    }
  }

  def update(eventUpdate: EventFormUpdate)(
    implicit mc: MarkerContext): Future[Option[EventResource]] = {

    val data = EventData(EventId(eventUpdate.id),
      eventUpdate.title,
      eventUpdate.organizers,
      eventUpdate.start_time,
      eventUpdate.end_time,
      eventUpdate.description,
      eventUpdate.category,
      eventUpdate.zip_code,
      eventUpdate.city,
      eventUpdate.street,
      eventUpdate.street_number,
      eventUpdate.phone,
      eventUpdate.mail,
      eventUpdate.website,
      eventUpdate.lat,
      eventUpdate.lon,
      eventUpdate.source)

    eventRepository.update(data)
    val eventFuture = eventRepository.get(EventId(eventUpdate.id))
    eventFuture.map { maybeEventData =>
      maybeEventData.map { eventData =>
        createEventResource(eventData)
      }
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
      e.organizers,
      e.start_time,
      e.end_time,
      e.description,
      e.category,
      e.zip_code,
      e.city,
      e.street,
      e.street_number,
      e.phone,
      e.mail,
      e.website,
      e.lat,
      e.lon,
      e.source)
  }

}
