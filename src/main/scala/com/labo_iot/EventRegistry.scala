package com.labo_iot

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.collection.immutable

final case class Event(id: String,
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

final case class Events(events: immutable.Seq[Event])

object EventRegistry {
  // actor protocol
  sealed trait Command
  final case class GetEvents(replyTo: ActorRef[Events]) extends Command
  final case class CreateEvent(event: Event, replyTo: ActorRef[ActionPerformed]) extends Command
  final case class GetEvent(name: String, replyTo: ActorRef[GetEventResponse]) extends Command
  final case class DeleteEvent(name: String, replyTo: ActorRef[ActionPerformed]) extends Command

  final case class GetEventResponse(maybeEvent: Option[Event])
  final case class ActionPerformed(description: String)

  def apply(): Behavior[Command] = registry(mockData.toSet)

  private val mockData = Seq(
    Event("1","Exposition L'Univers Face A/ Face B","Confluent des Savoirs","2019-09-02","2019-12-13","Dès septembre venez découvrir cette exposition déc...","exposition","5000","NAMUR","Rue Godefroid","5","+32 (0) 81 72 55 64","","https://www.namur.be/fr/agenda/exposition-lunivers-face-a-face-b","50.4663781507","4.8623936774", "namur-agenda-des-evenements"),
    Event("2","Léa Mayer","Galerie Détour","2019-10-15","2019-11-16","Thème: à venir...","exposition","5100","JAMBES","Avenue du Bourgmestre Jean Materne","166","+32 (0) 81 24 64 43","info@galeriedetour.be","https://www.namur.be/fr/agenda/lea-mayer","50.4556754804","4.8732740158", "namur-agenda-des-evenements"),
    Event("3","Halte! Taviers. Une étape sur la voie romaine","Confluent des Savoirs","2019-10-15","2020-01-10","À Namur, une exposition consacrée au Taviers gallo...","exposition","5000","NAMUR","Rue Godefroid","5","+32 (0) 81 72 55 64","","https://www.namur.be/fr/agenda/halte-taviers-une-etape-sur-la-voie-romaine","50.4663781507","4.8623936774", "namur-agenda-des-evenements"),
    Event("4","Remontée du temps, au pied de l'Enjambée","Tour d'Anhaive","2019-10-28","2020-01-26","Le projet d’aménagement du Grognon à Namur prévoya...","visite","5100","JAMBES","Avenue de Luxembourg","1","+32 (0) 81 32 23 30","","https://www.namur.be/fr/agenda/remontee-du-temps-au-pied-de-lenjambee","50.4636477532","4.8877876077", "namur-agenda-des-evenements"),
    Event("5","7ème Aaaargh Retro Film Festival","","2019-10-31","2019-11-03","7 films de différents genres étalés sur 3 jours et...","cinema","5000","NAMUR","Rue du Séminaire","22","","","https://www.namur.be/fr/agenda/7eme-aaaargh-retro-film-festival","50.4633814277","4.8586271036", "namur-agenda-des-evenements"),
    Event("6","Carmen","Billetterie du Théâtre","2019-11-02","2019-11-02","L’histoire de Carmen est le choix éternel entre l’...","spectacle","5000","NAMUR","Place du Théâtre","2","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/carmen","50.4641842279","4.8679311206", "namur-agenda-des-evenements"),
    Event("7","Zombies","Théâtre Jardin Passion","2019-11-04","2019-11-09","La Compagnie Gérard Gérard avait surpris et scotch...","spectacle","5000","NAMUR","Rue Marie Henriette","39","","info@theatrejardinpassion.be","https://www.namur.be/fr/agenda/zombies","50.4711108458","4.8577257074", "namur-agenda-des-evenements"),
    Event("8","La fiancée du pirate","Province de Namur - Cinéma","2019-11-04","2019-11-05","Fille d'une bohémienne, Marie vit avec sa mère dan...","spectacle","5000","NAMUR","Avenue Fernand Golenvaux","14","+32 (0) 81 77 67 73","cinema@province.namur.be","https://www.namur.be/fr/agenda/la-fiancee-du-pirate","50.4625553424","4.8688280306", "namur-agenda-des-evenements"),
    Event("9","Islande","Exploration du Monde","2019-11-05","2019-11-06","Une longue balade à travers l'Islande: déserts de ...","exposition","5000","NAMUR","Place du Théâtre","2","+32 (0) 2 648 38 10","","https://www.namur.be/fr/agenda/islande","50.4641842279","4.8679311206", "namur-agenda-des-evenements"),
    Event("10","Mylène Farmer 2019 - Le film","Acinapolis","2019-11-07","2019-11-07","Le nouveau spectacle grandiose de Mylène Farmer di...","spectacle","5100","JAMBES","Rue de la Gare Fleurie","16","+32 (0) 81 32 04 40","info@acinapolis.be","https://www.namur.be/fr/agenda/mylene-farmer-2019-le-film","50.4537942079","4.8739491098", "namur-agenda-des-evenements"),
    Event("11","Ciné-débat - Sortir du rang (documentaire)","","2019-11-08","2019-11-08","Dans le cadre du Mois du Doc, la Maison de la Laïc...","cinema","5000","NAMUR","Rue Lelièvre","5","","","https://www.namur.be/fr/agenda/cine-debat-sortir-du-rang-documentaire","50.4648537361","4.8608943358", "namur-agenda-des-evenements"),
    Event("12","Exploration Michaux","Maison de la Poésie","2019-11-09","2019-11-09","Dans le cadre du Festival Mots-aïque de la Maison ...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/exploration-michaux","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("13","Deux tickets pour le Paradis","Centre Culturel Marcel Hicter La Marlagne","2019-11-09","2019-11-09","Compagnie théâtrale « Bons Baisers de Flawinne » (...","spectacle","5100","WEPION","Chemin des Marronniers","26","+32 (0) 81 46 05 36","nathalie.devouge@cfwb.be","https://www.namur.be/fr/agenda/deux-tickets-pour-le-paradis","50.4266101774","4.8479298845", "namur-agenda-des-evenements"),
    Event("14","Camping Sauvach - Krakin'Kellys - Super Hérisson","Belvédère","2019-11-09","2019-11-09","Quoi de plus naturel que de revenir au belvédère p...","concert","5000","NAMUR","Avenue du Milieu du Monde","1","+32 (0) 81 81 39 00","panama@belvedere-namur.be","https://www.namur.be/fr/agenda/camping-sauvach-krakinkellys-super-herisson","50.4553525663","4.8562912834", "namur-agenda-des-evenements"),
    Event("15","Picnic","Maison de la Poésie","2019-11-10","2019-11-10","Les sens en éveil, le langage encore mystérieux, e...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/picnic","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("16","Michelle Durvaux","Galerie du Beffroi","2019-11-11","2019-11-24","« On décompose, on recompose. On consomme, on accu...","exposition","5000","NAMUR","Rue du Beffroi","13","+32 (0) 81 22 84 76","galeriedubeffroi@ville.namur.be","https://www.namur.be/fr/agenda/michelle-durvaux","50.4638121093","4.8668103448", "namur-agenda-des-evenements"),
    Event("17","Devenez les héros de demain!","","2019-11-12","2019-11-12","La Cité des Métiers de Namur, en collaboration ave...","exposition","5000","NAMUR","Place d'Armes","1","","","https://www.namur.be/fr/agenda/devenez-les-heros-de-demain","50.4634827452","4.8673780954", "namur-agenda-des-evenements"),
    Event("18","Visite du Fort Saint-Héribert","Fort Saint-Héribert","2019-11-10","2019-11-11","Un verre de vin chaud sera offert aux ...","visite","5100","JAMBES","Route des Forts","","","","https://www.namur.be/fr/agenda/visite-du-fort","50.4286636816","4.8330471438", "namur-agenda-des-evenements"),
    Event("19","Evelyne Axell - Méthodes Pop","Province de Namur -  Animations culturelles","2019-09-20","2020-01-26","La saison artistique du Delta s’ouvre sur une gran...","spectacle","5000","NAMUR","Avenue Fernand Golenvaux","14","+32 (0) 81 77 51 21","maryse.mathy@province.namur.be","https://www.namur.be/fr/agenda/evelyne-axell-methodes-pop","50.4625553424","4.8688280306", "namur-agenda-des-evenements"),
    Event("20","Le Saint-Désert de la Marlagne","Musée de la Fraise","2019-10-06","2020-01-31","Le Désert de la Marlagne, fondé en 1969 par Thomas...","exposition","5100","WEPION","Chaussée de Dinant","1037","+32 (0) 81 46 20 07","info@museedelafraise.eu","https://www.namur.be/fr/agenda/le-saint-desert-de-la-marlagne","50.4166880345","4.8790703377", "namur-agenda-des-evenements"),
    Event("21","Alexandre de Belgique. Dans l'intimité d'un Prince","Les Bateliers","2019-10-24","2020-03-01","A l’occasion du 10e anniversaire de la mort du Pri...","exposition","5000","NAMUR","Rue Joseph Saintraint","3","+32 (0) 81 24 87 20","","https://www.namur.be/fr/agenda/alexandre-de-belgique-dans-lintimite-dun-prince","50.4633593455","4.8611707302", "namur-agenda-des-evenements"),
    Event("22","DER Menschenfresser Berg ou à la Montagne","Théâtre Jardin Passion","2019-10-28","2019-11-02","Ce spectacle raconte une quête : l’adaptation, par...","spectacle","5000","NAMUR","Rue Marie Henriette","39","","info@theatrejardinpassion.be","https://www.namur.be/fr/agenda/der-menschenfresser-berg-ou-a-la-montagne","50.4711108458","4.8577257074", "namur-agenda-des-evenements"),
    Event("23","Halloween à la Citadelle","Comité Animation Citadelle - (CAC)","2019-10-30","2019-11-03","Du 31 octobre au 03 novembre, les fantômes de l’Ôd...","spectacle","5000","NAMUR","Route Merveilleuse","64","+32 (0) 81 24 73 70","info@citadelle.namur.be","https://www.namur.be/fr/loisirs/tourisme/citadelle-de-namur/agenda-citadelle/halloween-a-la-citadelle","50.4589723863","4.8617568796", "namur-agenda-des-evenements"),
    Event("24","Stout sur Meuse - 2019","","2019-10-31","2019-11-02","Voilà un magnifique évènement rassemblant les bièr...","foire","5000","NAMUR","Avenue de la Plante","49","","","https://www.namur.be/fr/agenda/stout-sur-meuse-2019","50.4531100218","4.8606620889", "namur-agenda-des-evenements"),
    Event("25","Au Bolchoï","Institut Supérieur de Musique et Pédagogie - IMEP","2019-11-02","2019-11-02","Å l'occasion de la sortie du disque  Clarinetti a...","concert","5000","NAMUR","Rue Juppin","28","+32 (0) 81 73 64 37","info@imep.be","https://www.namur.be/fr/agenda/au-bolchoi","50.4644989154","4.845506734", "namur-agenda-des-evenements"),
    Event("26","Il est où le A du Zébu","Billetterie du Théâtre","2019-11-06","2019-11-06","Sur un plateau noir brillant délimité par des boit...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/il-est-ou-le-a-du-zebu","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("27","The Extraordinary Film Festival - 2019","EOP! asbl","2019-11-06","2019-11-07","Unique en Belgique, The Extraordinary Film Festiva...","cinema","5000","NAMUR","Place d'Armes","1","+32 (0) 2 673 27 89","roland.gauvry@skynet.be","https://www.namur.be/fr/agenda/the-extraordinary-film-festival-2019","50.4634827452","4.8673780954", "namur-agenda-des-evenements"),
    Event("28","Lou Doillon","Billetterie du Théâtre","2019-11-07","2019-11-07","Fille du réalisateur Jacques Doillon et de l’actri...","concert","5000","NAMUR","Place du Théâtre","2","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/lou-doillon","50.4641842279","4.8679311206", "namur-agenda-des-evenements"),
    Event("29","MOTS-AÏQUE : Festival de poésie et langue française","Maison de la Poésie","2019-11-08","2019-11-10","La Maison de la Poésie de Namur accueille le publi...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/mots-aique-festival-de-poesie-et-langue-francaise","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("30","Concert symphonique","Institut Supérieur de Musique et Pédagogie - IMEP","2019-11-08","2019-11-10","Concerto n°1 en mi bémol majeur de Franz Liszt - C...","concert","5000","NAMUR","Rue Juppin","28","+32 (0) 81 73 64 37","info@imep.be","https://www.namur.be/fr/agenda/concert-symphonique","50.4644989154","4.845506734", "namur-agenda-des-evenements"),
    Event("31","Festival de piano","Institut Supérieur de Musique et Pédagogie - IMEP","2019-11-08","2019-11-15","Artistes invités: Réservatios obligatoires: billet...","concert","5000","NAMUR","Rue Juppin","28","+32 (0) 81 73 64 37","info@imep.be","https://www.namur.be/fr/agenda/festival-de-piano","50.4644989154","4.845506734", "namur-agenda-des-evenements"),
    Event("32","conférence du samedi","","2019-11-09","2019-11-09","Nature à genoux, citoyens debout !","conference","5000","NAMUR","Place du Théâtre","2","","","https://www.namur.be/fr/agenda/conference","50.4641842279","4.8679311206", "namur-agenda-des-evenements"),
    Event("33","Sarrasine","Cavatine asbl","2019-11-10","2019-11-10","Céline Bodson et Sara Picavet, deux musiciennes ay...","concert","5000","NAMUR","Rue Fumal","28","+32 (0) 487 36 84 51","info@cavatineasbl.be","https://www.namur.be/fr/agenda/sarrasine","50.4626602296","4.8626294756", "namur-agenda-des-evenements"),
    Event("34","Jack et le haricot magique","Maison de la Poésie","2019-11-10","2019-11-10","Présentation détaillée : Approchez, approchez sous...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/jack-et-le-haricot-magique","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("35","Blue Bird","Billetterie du Théâtre","2019-11-01","2019-11-03","Installé au coeur de la scénographie, le spectateu...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/blue-bird","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("36","Fleuve","Maison de la Poésie","2019-11-07","2019-11-08","Aux abords d’une forêt, il y avait des femmes. Ell...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/fleuve","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("37","Antica Namur - 2019","Namur Expo","2019-11-08","2019-11-17","Cette année encore un soin tout particulier a été ...","exposition","5000","NAMUR","Avenue Sergent Vrithoff","2","+32 (0) 81 47 93 47","namurexpo@artexis.com","https://www.namur.be/fr/agenda/antica-namur-2019","50.4671123912","4.8495209977", "namur-agenda-des-evenements"),
    Event("38","Guillaume Vierset - Harvest Group","Animation Gelbressoise","2019-11-09","2019-11-09","Mené par le guitariste Guillaume Vierset, Harvest ...","concert","5024","GELBRESSEE","Rue Ernest Moëns","57","","","https://www.namur.be/fr/agenda/guillaume-vierset-harvest-group","50.509051736","4.9542756489", "namur-agenda-des-evenements"),
    Event("39","Les mots s'improsent par Félix Radu","Maison de la Poésie","2019-11-09","2019-11-09","Digne héritier de Raymond Devos, Félix Radu se jou...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 53 49","info@maisondelapoesie.be","https://www.namur.be/fr/agenda/spectacle-les-mots-simprosent-par-felix-radu","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("40","Chhht...","Billetterie du Théâtre","2019-11-10","2019-11-11","Chhht… Les enfants dorment enfin. Commence alors p...","spectacle","5000","NAMUR","Traverse des Muses","18","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/chhht","50.4727078892","4.862993455", "namur-agenda-des-evenements"),
    Event("41","Dimanche","Billetterie du Théâtre","2019-11-11","2019-11-16","Dans un vieil appartement d’un centre-ville, une f...","spectacle","5000","NAMUR","Place du Théâtre","2","+32 (0) 81 22 60 26","","https://www.namur.be/fr/agenda/dimanche","50.4641842279","4.8679311206", "namur-agenda-des-evenements"),
    Event("42","Jogging La Gelbressoise","","2019-11-11","2019-11-11","5è édition...","sport","5024","GELBRESSEE","Rue Notre-Dame du Vivier","","","","https://www.namur.be/fr/ma-ville/administration/services-communaux/sports/agenda-sports/jogging-la-gelbressoise","50.4886807897","4.9580080704", "namur-agenda-des-evenements"),
    Event("43","Festival du Cirque de Namur - 2019","","2019-10-25","2019-11-11","D’année en année, Monsieur Horwood et son équipe r...","spectacle","5000","NAMUR","Esplanade de la Citadelle","","","","https://www.namur.be/fr/agenda/festival-du-cirque-de-namur-2019","50.457527615","4.8578601982", "namur-agenda-des-evenements"),
    Event("44","Marchés aux Chrysanthèmes","Affaires économiques","2019-10-25","2019-11-01","Sans oublier la vente de fleurs sur les marchés de...","foire","5000","NAMUR","Place de l'Ange","","+32 (0) 81 24 72 26","affaires.economiques@ville.namur.be","https://www.namur.be/fr/agenda/marches-au-chrysanthemes","50.4639902487","4.8656460502", "namur-agenda-des-evenements")
  )

  private def registry(events: Set[Event]): Behavior[Command] =
    Behaviors.receiveMessage {
      case GetEvents(replyTo) =>
        replyTo ! Events(events.toSeq)
        Behaviors.same
      case CreateEvent(event, replyTo) =>
        replyTo ! ActionPerformed(s"Event ${event.id} created.")
        registry(events + event)
      case GetEvent(id, replyTo) =>
        replyTo ! GetEventResponse(events.find(_.id == id))
        Behaviors.same
      case DeleteEvent(id, replyTo) =>
        replyTo ! ActionPerformed(s"Event $id deleted.")
        registry(events.filterNot(_.id == id))
    }
}