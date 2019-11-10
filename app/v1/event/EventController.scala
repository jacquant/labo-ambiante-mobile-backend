package v1.event

import javax.inject.Inject
import play.api.Logger
import play.api.data.Form
import play.api.libs.json.Json
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future}

case class EventFormInput(title: String,
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

case class EventFormUpdate(id: String,
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

/**
  * Takes HTTP requests and produces JSON.
  */
class EventController @Inject()(cc: EventControllerComponents)(
    implicit ec: ExecutionContext)
    extends EventBaseController(cc) {

  private val logger = Logger(getClass)

  private val createForm: Form[EventFormInput] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "title" -> nonEmptyText,
        "organizers" -> text,
        "start_time" -> text,
        "end_time" -> text,
        "description" -> text,
        "category" -> text,
        "zip_code" -> text,
        "city" -> text,
        "street" -> text,
        "street_number" -> text,
        "phone" -> text,
        "mail" -> text,
        "website" -> text,
        "lat" -> text,
        "lon" -> text,
        "source" -> text
      )(EventFormInput.apply)(EventFormInput.unapply)
    )
  }

  private val updateForm: Form[EventFormUpdate] = {
    import play.api.data.Forms._

    Form(
      mapping(
        "id" -> nonEmptyText,
        "title" -> nonEmptyText,
        "organizers" -> text,
        "start_time" -> text,
        "end_time" -> text,
        "description" -> text,
        "category" -> text,
        "zip_code" -> text,
        "city" -> text,
        "street" -> text,
        "street_number" -> text,
        "phone" -> text,
        "mail" -> text,
        "website" -> text,
        "lat" -> text,
        "lon" -> text,
        "source" -> text
      )(EventFormUpdate.apply)(EventFormUpdate.unapply)
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

  def update: Action[AnyContent] = EventAction.async { implicit request =>
    logger.trace("update: ")
    updateJsonEvent()
  }

  def show(id: String): Action[AnyContent] = EventAction.async {
    implicit request =>
      logger.trace(s"show: id = $id")
      eventResourceHandler.lookup(id).map {
        case Some(event) => Ok(Json.toJson(event))
        case None => NotFound
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

    createForm.bindFromRequest().fold(failure, success)
  }

  private def updateJsonEvent[A]()(
    implicit request: EventRequest[A]): Future[Result] = {
    def failure(badForm: Form[EventFormUpdate]) = {
      Future.successful(BadRequest(badForm.errorsAsJson))
    }

    def success(input: EventFormUpdate) = {
      eventResourceHandler.update(input).map {
        case Some(event) => Ok(Json.toJson(event)).withHeaders(LOCATION -> event.link)
        case None => NotFound
      }
    }

    updateForm.bindFromRequest().fold(failure, success)
  }
}
