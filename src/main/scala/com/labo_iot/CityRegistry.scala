package com.labo_iot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable

final case class City(id: String,
                          name: String)

final case class Cities(cities: immutable.Seq[City])

object CityRegistry {
  // actor protocol
  sealed trait Command
  final case class GetCities(replyTo: ActorRef[Cities]) extends Command
  final case class CreateCity(city: City, replyTo: ActorRef[CityActionPerformed]) extends Command
  final case class GetCity(name: String, replyTo: ActorRef[GetCityResponse]) extends Command
  final case class DeleteCity(name: String, replyTo: ActorRef[CityActionPerformed]) extends Command
  final case class UpdateCity(city: City, replyTo: ActorRef[CityActionPerformed]) extends Command

  final case class GetCityResponse(maybeCity: Option[City])
  final case class CityActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(mockData.toSet)

  private val mockData = Seq(
    City("GELBRESSEE","Gelbressée"),
    City("JAMBES","Jambes"),
    City("NAMUR","Namur"),
    City("WEPION","Wépion")
  )

  private def registry(cities: Set[City]): Behavior[Command] = {
    Behaviors.receiveMessage {
      case GetCities(replyTo) =>
        replyTo ! Cities(cities.toSeq)
        Behaviors.same
      case CreateCity(city, replyTo) =>
        replyTo ! CityActionPerformed(s"City ${city.id} created.")
        registry(cities + city)
      case GetCity(id, replyTo) =>
        replyTo ! GetCityResponse(cities.find(_.id == id))
        Behaviors.same
      case DeleteCity(id, replyTo) =>
        replyTo ! CityActionPerformed(s"City $id deleted.")
        registry(cities.filterNot(_.id == id))
      case UpdateCity(city, replyTo) =>
        replyTo ! CityActionPerformed(s"City ${city.id} update.")
        registry(cities.filterNot(_.id == city.id) + city)
    }
  }
}