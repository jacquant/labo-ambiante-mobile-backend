# labo-ambiante-mobile-backend

Version 0 de l'API Rest

Appels disponibles :
 - GET /v1/events : liste des événements
 - GET /v1/events/{$id} : événement $id
 - POST /v1/events : ajout d'un événement (pas d'ajout effectif pour l'instant)
 
pour tester : curl -X POST -i http://localhost:9000/v1/events/ --data "title=titre&genre=genre&description=description&start_time=start_time&duration=duration&city=city&country=country&lat=0.0&lon=0.0"

lien pour Swagger : http://localhost:9000/docs/swagger-ui/index.html?url=/assets/swagger.json
