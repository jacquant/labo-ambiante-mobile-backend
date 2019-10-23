package v1.event

import javax.inject.{Inject, Singleton}

import akka.actor.ActorSystem
import play.api.libs.concurrent.CustomExecutionContext
import play.api.{Logger, MarkerContext}

import scala.concurrent.Future

final case class EventData(id: EventId,
                           title: String,
                           genre: String,
                           description: String,
                           start_time: String,
                           duration: String,
                           city: String,
                           country: String,
                           lat: String,
                           lon: String)

class EventId private (val underlying: Int) extends AnyVal {
  override def toString: String = underlying.toString
}

object EventId {
  def apply(raw: String): EventId = {
    require(raw != null)
    new EventId(Integer.parseInt(raw))
  }
}

class EventExecutionContext @Inject()(actorSystem: ActorSystem)
    extends CustomExecutionContext(actorSystem, "repository.dispatcher")

/**
  * A pure non-blocking interface for the EventRepository.
  */
trait EventRepository {
  def create(data: EventData)(implicit mc: MarkerContext): Future[EventId]

  def list()(implicit mc: MarkerContext): Future[Iterable[EventData]]

  def get(id: EventId)(implicit mc: MarkerContext): Future[Option[EventData]]
}

/**
  * A trivial implementation for the Event Repository.
  *
  * A custom execution context is used here to establish that blocking operations should be
  * executed in a different thread than Play's ExecutionContext, which is used for CPU bound tasks
  * such as rendering.
  */
@Singleton
class EventRepositoryImpl @Inject()()(implicit ec: EventExecutionContext)
    extends EventRepository {

  private val logger = Logger(this.getClass)

  private val eventList = List(
    EventData(EventId("1"), "event 1", "", "", "", "", "", "", "0.0", "0.0"),
    EventData(EventId("2"), "event 2", "", "", "", "", "", "", "0.0", "0.0"),
    EventData(EventId("3"), "event 3", "", "", "", "", "", "", "0.0", "0.0"),
    EventData(EventId("4"), "event 4", "", "", "", "", "", "", "0.0", "0.0"),
    EventData(EventId("5"), "event 5", "", "", "", "", "", "", "0.0", "0.0")
  )

  override def list()(
      implicit mc: MarkerContext): Future[Iterable[EventData]] = {
    Future {
      logger.trace(s"list: ")
      eventList
    }
  }

  override def get(id: EventId)(
      implicit mc: MarkerContext): Future[Option[EventData]] = {
    Future {
      logger.trace(s"get: id = $id")
      eventList.find(event => event.id == id)
    }
  }

  def create(data: EventData)(implicit mc: MarkerContext): Future[EventId] = {
    Future {
      logger.trace(s"create: data = $data")
      data.id
    }
  }

}
