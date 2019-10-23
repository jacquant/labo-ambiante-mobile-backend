package v1.event

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class EventFormInput(title: String,
                          genre: String,
                          description: String,
                          start_time: String,
                          duration: String,
                          city: String,
                          country: String,
                          lat: String,
                          lon: String)

/**
  * Takes HTTP requests and produces JSON.
  */
class EventController @Inject()(cc: EventControllerComponents)(
    implicit ec: ExecutionContext)
    extends EventBaseController(cc) {

  private val logger = Logger(getClass)

  private val form: Form[EventFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "genre" -> text,
        "description" -> text,
        "start_time" -> text,
        "duration" -> text,
        "city" -> text,
        "country" -> text,
        "lat" -> text,
        "lon" -> text
      )(EventFormInput.apply)(EventFormInput.unapply)
    )
  }

  def index: Action[AnyContent] = EventAction.async { implicit request =>
    logger.trace("index: ")
    eventResourceHandler.find.map { events =>
      Ok(Json.toJson(events))
    }
  }

  def process: Action[AnyContent] = EventAction.async { implicit request =>
    logger.trace("process: ")
    processJsonEvent()
  }

  def show(id: String): Action[AnyContent] = EventAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      eventResourceHandler.lookup(id).map { event =>
        Ok(Json.toJson(event))
      }
  }

  private def processJsonEvent[A]()(
      implicit request: EventRequest[A]): Future[Result] = {
    def failure(badForm: Form[EventFormInput]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: EventFormInput) = {
      eventResourceHandler.create(input).map { event =>
        Created(Json.toJson(event)).withHeaders(LOCATION -> event.link)
      }
    }

    form.bindFromRequest().fold(failure, success)
  }
}
