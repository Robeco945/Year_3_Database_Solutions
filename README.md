# Esimerkki Spring Boot -sovelluksesta
Sovellus käyttää Spring Web- ja Spring Data JPA -kirjastoja.

Mukana endpointit:
- GET localhost:8080/tili/{id}
- GET localhost:8080/tili/saldoainakin/{raja}
- POST localhost:8080/tili/korko. Parametriksi esim. {"prosentti": 5}
- POST localhost:8080/haltija. Parametriksi esim. {"id": 5,"etunimi": "Maija","sukunimi": "Meikäläinen"}
- GET localhost:8080/haltija/{id}
- GET localhost:8080/haltija/etunimi/{etunimen_alku}

Koodi refaktoroitu pakkauksiin:
- entity
- controller
- service
- repository
- dto
- converter
- entitylistener

