package com.labo_iot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable

final case class Source(id: String,
                          name: String)

final case class Sources(sources: immutable.Seq[Source])

object SourceRegistry {
  // actor protocol
  sealed trait Command
  final case class GetSources(replyTo: ActorRef[Sources]) extends Command
  final case class CreateSource(source: Source, replyTo: ActorRef[SourceActionPerformed]) extends Command
  final case class GetSource(name: String, replyTo: ActorRef[GetSourceResponse]) extends Command
  final case class DeleteSource(name: String, replyTo: ActorRef[SourceActionPerformed]) extends Command
  final case class UpdateSource(source: Source, replyTo: ActorRef[SourceActionPerformed]) extends Command

  final case class GetSourceResponse(maybeSource: Option[Source])
  final case class SourceActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(mockData.toSet)

  private val mockData = Seq(
    Source("namur-agenda-des-evenements","Agenda des événements de Namur"),
    Source("box", "Box")
  )

  private def registry(sources: Set[Source]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetSources(replyTo) =>
        replyTo ! Sources(sources.toSeq)
        Behaviors.same
      case CreateSource(source, replyTo) =>
        replyTo ! SourceActionPerformed(s"Source ${source.id} created.")
        registry(sources + source)
      case GetSource(id, replyTo) =>
        replyTo ! GetSourceResponse(sources.find(_.id == id))
        Behaviors.same
      case DeleteSource(id, replyTo) =>
        replyTo ! SourceActionPerformed(s"Source $id deleted.")
        registry(sources.filterNot(_.id == id))
      case UpdateSource(source, replyTo) =>
        replyTo ! SourceActionPerformed(s"Source ${source.id} update.")
        registry(sources.filterNot(_.id == source.id) + source)
    }
  }
}