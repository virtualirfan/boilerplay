package controllers.admin.rest

import controllers.BaseController
import io.circe.syntax._
import models.Application
import models.rest.request.{RequestFolder, RestRequest}
import play.api.mvc.AnyContent
import services.rest.{RestRepository, RestRequestOps, RestRequestService}
import util.tracing.TraceData
import util.web.ControllerUtils

import scala.concurrent.Future

@javax.inject.Singleton
class RestController @javax.inject.Inject() (override val app: Application, svc: RestRequestService) extends BaseController("rest") {
  import app.contexts.webContext

  def viewFor(resource: Either[RequestFolder, RestRequest], q: Option[String] = None)(implicit request: Req, td: TraceData) = resource match {
    case Left(folder) =>
      td.tag("loc", folder.location)
      Future.successful(Ok(views.html.admin.rest.list(request.identity, folder, q)))
    case Right(req) =>
      td.tag("loc", req.fileLocation)
      Future.successful(Ok(views.html.admin.rest.detail(request.identity, req)))
  }

  def root(q: Option[String]) = withSession("index", admin = true) { implicit request => implicit td =>
    viewFor(Left(services.rest.RestRepository.repo.requests), q)
  }

  def reloadRoot() = withSession("reload", admin = true) { implicit request => implicit td =>
    RestRepository.reload(None)
    Future.successful(Redirect(controllers.admin.rest.routes.RestController.root()))
  }

  def dump() = withSession("dump", admin = true) { implicit request => implicit td =>
    Future.successful(Ok(RestRepository.repo.asJson))
  }

  def view(location: String) = withSession("view", admin = true) { implicit request => implicit td =>
    viewFor(RestRepository.repo.requests.getResource(location))
  }

  def reload(location: String) = withSession("reload", admin = true) { implicit request => implicit td =>
    RestRepository.reload(Some(location))
    Future.successful(Redirect(controllers.admin.rest.routes.RestController.view(location)))
  }

  def raw(location: String) = withSession("raw", admin = true) { implicit request => implicit td =>
    RestRepository.repo.requests.getResource(location) match {
      case Right(file) => Future.successful(Ok(file.asJson))
      case Left(folder) => Future.successful(Ok(folder.asJson))
    }
  }

  def form(location: String) = withSession("form", admin = true) { implicit request => implicit td =>
    td.tag("location", location)
    val req = RestRepository.repo.requests.getFile(location)
    Future.successful(Ok(views.html.admin.rest.form(request.identity, req)))
  }

  def save(location: String) = withSession("save", admin = true) { implicit request => implicit td =>
    val req = requestFromBody(location, request.body)
    td.tag("location", location)
    val result = RestRequestOps.saveRequest(req)
    Future.successful(Redirect(controllers.admin.rest.routes.RestController.view(result.fileLocation)))
  }

  def run(location: String) = withSession("run", admin = true) { implicit request => implicit td =>
    td.tag("location", location)
    val req = RestRepository.repo.requests.getFile(location)
    svc.call(req, println)
    Future.successful(Ok(io.circe.syntax.EncoderOps(req).asJson))
  }

  private[this] def requestFromBody(location: String, body: AnyContent)(implicit request: Req, td: TraceData) = try {
    ControllerUtils.jsonFormOrBody(request.body, "json").as[RestRequest] match {
      case Right(r) => r
      case Left(x) => throw x
    }
  } catch {
    case _: IllegalStateException =>
      val form = ControllerUtils.getForm(request.body)
      val (p, n) = location.lastIndexOf('/') match {
        case -1 => "" -> location
        case x => location.substring(0, x) -> location.substring(x + 1)
      }
      RestRequest(name = n, title = form.getOrElse("title", n), folder = Some(p))
  }
}
