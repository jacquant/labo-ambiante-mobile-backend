package com.labo_iot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable

final case class Category(id: String,
                          name: String)

final case class Categories(categories: immutable.Seq[Category])

object CategoryRegistry {
  // actor protocol
  sealed trait Command
  final case class GetCategories(replyTo: ActorRef[Categories]) extends Command
  final case class CreateCategory(category: Category, replyTo: ActorRef[CategoryActionPerformed]) extends Command
  final case class GetCategory(name: String, replyTo: ActorRef[GetCategoryResponse]) extends Command
  final case class DeleteCategory(name: String, replyTo: ActorRef[CategoryActionPerformed]) extends Command
  final case class UpdateCategory(category: Category, replyTo: ActorRef[CategoryActionPerformed]) extends Command

  final case class GetCategoryResponse(maybeCategory: Option[Category])
  final case class CategoryActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(mockData.toSet)

  private val mockData = Seq(
    Category("cinema","Cinéma"),
    Category("concert","Concert"),
    Category("conference","Conférence"),
    Category("exposition","Exposition"),
    Category("foire","Foire"),
    Category("spectacle","Spectacle"),
    Category("sport","Sport"),
    Category("visite","Visite")
  )

  private def registry(categories: Set[Category]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetCategories(replyTo) =>
        replyTo ! Categories(categories.toSeq)
        Behaviors.same
      case CreateCategory(category, replyTo) =>
        replyTo ! CategoryActionPerformed(s"Category ${category.id} created.")
        registry(categories + category)
      case GetCategory(id, replyTo) =>
        replyTo ! GetCategoryResponse(categories.find(_.id == id))
        Behaviors.same
      case DeleteCategory(id, replyTo) =>
        replyTo ! CategoryActionPerformed(s"Category $id deleted.")
        registry(categories.filterNot(_.id == id))
      case UpdateCategory(category, replyTo) =>
        replyTo ! CategoryActionPerformed(s"Category ${category.id} update.")
        registry(categories.filterNot(_.id == category.id) + category)
    }
  }
}