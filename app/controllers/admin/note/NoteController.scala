package controllers.admin.note

import controllers.BaseController
import io.circe.generic.auto._
import io.circe.java8.time._
import io.circe.syntax._
import java.util.UUID
import models.Application
import models.note.NoteResult
import models.result.orderBy.OrderBy
import scala.concurrent.Future
import services.note.NoteService
import util.FutureUtils.defaultContext
import util.web.ControllerUtils.acceptsCsv

@javax.inject.Singleton
class NoteController @javax.inject.Inject() (
    override val app: Application, svc: NoteService
) extends BaseController("note") {
  def createForm = withSession("create.form", admin = true) { implicit request => implicit td =>
    val cancel = controllers.admin.note.routes.NoteController.list()
    val call = controllers.admin.note.routes.NoteController.create()
    Future.successful(Ok(views.html.admin.note.noteForm(
      request.identity, models.note.Note.empty, "New Note", cancel, call, isNew = true, debug = app.config.debug
    )))
  }

  def create = withSession("create", admin = true) { implicit request => implicit td =>
    val fields = modelForm(request.body.asFormUrlEncoded)
    svc.create(fields)
    Future.successful(Ok(play.twirl.api.Html(fields.toString)))
  }

  def list(q: Option[String], orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {
    withSession("list", admin = true) { implicit request => implicit td =>
      val startMs = util.DateUtils.nowMillis
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      val r = q match {
        case Some(query) if query.nonEmpty => svc.searchWithCount(query, Nil, orderBys, limit.orElse(Some(100)), offset)
        case _ => svc.getAllWithCount(Nil, orderBys, limit.orElse(Some(100)), offset)
      }
      Future.successful(render {
        case Accepts.Html() => Ok(views.html.admin.note.noteList(
          request.identity, Some(r._1), r._2, q, orderBy, orderAsc, limit.getOrElse(100), offset.getOrElse(0)
        ))
        case Accepts.Json() => Ok(NoteResult.fromRecords(q, Nil, orderBys, limit, offset, startMs, r._1, r._2).asJson.spaces2).as(JSON)
        case acceptsCsv() => Ok(svc.csvFor("Note", r._1, r._2)).as("text/csv")
      })
    }
  }

  def byAuthor(author: UUID, orderBy: Option[String], orderAsc: Boolean, limit: Option[Int], offset: Option[Int]) = {
    withSession("get.by.author", admin = true) { implicit request => implicit td =>
      val orderBys = OrderBy.forVals(orderBy, orderAsc).toSeq
      val models = svc.getByAuthor(author, orderBys, limit, offset)
      Future.successful(render {
        case Accepts.Html() => Ok(views.html.admin.note.noteByAuthor(
          request.identity, author, models, orderBy, orderAsc, limit.getOrElse(5), offset.getOrElse(0)
        ))
        case Accepts.Json() => Ok(models.asJson.spaces2).as(JSON)
        case acceptsCsv() => Ok(svc.csvFor("Note by author", 0, models)).as("text/csv")
      })
    }
  }

  def view(id: java.util.UUID) = withSession("view", admin = true) { implicit request => implicit td =>
    svc.getByPrimaryKey(id) match {
      case Some(model) => Future.successful(render {
        case Accepts.Html() => Ok(views.html.admin.note.noteView(request.identity, model, app.config.debug))
        case Accepts.Json() => Ok(model.asJson.spaces2).as(JSON)
      })
      case None => Future.successful(NotFound(s"No Note found with id [$id]."))
    }
  }

  def editForm(id: java.util.UUID) = withSession("edit.form", admin = true) { implicit request => implicit td =>
    val cancel = controllers.admin.note.routes.NoteController.view(id)
    val call = controllers.admin.note.routes.NoteController.edit(id)
    svc.getByPrimaryKey(id) match {
      case Some(model) => Future.successful(Ok(
        views.html.admin.note.noteForm(request.identity, model, s"Note [$id]", cancel, call, debug = app.config.debug)
      ))
      case None => Future.successful(NotFound(s"No Note found with id [$id]."))
    }
  }

  def edit(id: java.util.UUID) = withSession("edit", admin = true) { implicit request => implicit td =>
    val fields = modelForm(request.body.asFormUrlEncoded)
    val res = svc.update(id = id, fields = fields)
    Future.successful(render {
      case Accepts.Html() => Redirect(controllers.admin.note.routes.NoteController.view(res.id))
      case Accepts.Json() => Ok(res.asJson.spaces2).as(JSON)
    })
  }

  def remove(id: java.util.UUID) = withSession("remove", admin = true) { implicit request => implicit td =>
    svc.remove(id = id)
    Future.successful(render {
      case Accepts.Html() => Redirect(controllers.admin.note.routes.NoteController.list())
      case Accepts.Json() => Ok("{ \"status\": \"removed\" }").as(JSON)
    })
  }
}