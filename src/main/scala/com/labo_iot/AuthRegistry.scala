package com.labo_iot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

final case class FilterData(username: String,
                            category: String,
                            city: String,
                            sound_level_max: String,
                            sound_level_min: String)

final case class EventData(source: String,
                           organizers: String,
                           zip_code: String,
                           city: String,
                           street: String,
                           street_number: String,
                           phone: String,
                           mail: String,
                           website: String,
                           lat: Double,
                           lon: Double)

object AuthRegistry {
  // actor protocol
  sealed trait Command
  //final case class GetUsers(replyTo: ActorRef[???]) extends Command
  final case class GetFilterData(username: String, replyTo: ActorRef[GetFilterDataResponse]) extends Command
  final case class GetEventData(username: String, replyTo: ActorRef[GetEventDataResponse]) extends Command

  final case class GetFilterDataResponse(maybeFilterData: Option[FilterData])
  final case class GetEventDataResponse(maybeEventData: Option[EventData])

  def apply(): Behavior[Command] = registry(mockFilterData.toSet, mockEventData.toSet)

  private val mockFilterData = Seq(
    FilterData("user","concert,cinema","NAMUR,JAMBES","50.0","110.0"),
    FilterData("user2","exposition,conference","NAMUR","50.0","110.0")
  )

  private val mockEventData = Seq(
    EventData("user","Galerie Détour","5100","JAMBES","Avenue du Bourgmestre Jean Materne","166","+32 (0) 81 24 64 43","info@galeriedetour.be","https://www.namur.be/fr/agenda/lea-mayer",50.4556754804,4.8732740158),
    EventData("user2","Confluent des Savoirs","5000","NAMUR","Rue Godefroid","5","+32 (0) 81 72 55 64","","https://www.namur.be/fr/agenda/exposition-lunivers-face-a-face-b",50.4663781507,4.8623936774)
  )

  private def registry(filterDataset: Set[FilterData], eventDataset: Set[EventData]): Behavior[Command] = {
    Behaviors.receiveMessage {
      //case GetUsers(replyTo) =>
      //  replyTo ! Cities(cities.toSeq)
      //  Behaviors.same
      case GetFilterData(username, replyTo) =>
        replyTo ! GetFilterDataResponse(filterDataset.find(_.username == username))
        Behaviors.same
      case GetEventData(username, replyTo) =>
        replyTo ! GetEventDataResponse(eventDataset.find(_.source == username))
        Behaviors.same
    }
  }
}