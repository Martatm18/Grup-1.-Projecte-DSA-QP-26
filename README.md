# Projecte DSA - Grup 1 - Segon minim EJ2

## Estat de l'entrega

Entrega EJ2 acabada.

La funcionalitat demanada al segon minim permet consultar els membres de l'equip del qual forma part un usuari. La part de backend esta implementada en aquest repositori i la part d'Android esta implementada al repositori seguent:

https://github.com/MartiSabater/Grup1.ProjecteAndroid/tree/MINIM2MARTI

## Enunciat EJ2

L'EJ2 demana una nova funcionalitat per consultar els membres de l'equip d'un usuari.

Tasques demanades:
- T1: Afegir a l'aplicacio Android una nova activitat que mostri un llistat dels usuaris que comparteixen equip amb l'usuari actual.
- T2: Afegir una nova ruta al backend que rebi una peticio `GET /user/xxxx/team`.

La resposta havia d'incloure:
- Nom de l'equip.
- Llistat de membres.
- Per cada membre: imatge/avatar, nom i punts.

Segons l'enunciat, les rutes REST podien ser dummy, sense implementacio de base de dades, pero havien d'atendre la peticio i mostrar un missatge per consola.

## Backend implementat

En aquest backend s'ha afegit la ruta:

```http
GET /user/{idUser}/team
```

Amb la configuracio actual del servidor, la URL completa es:

```http
http://192.168.10.92:8080/dsaApp/user/{idUser}/team
```

Exemple:

```http
GET http://192.168.10.92:8080/dsaApp/user/marti/team
```

Quan es fa la peticio, el backend:
- Rep l'identificador de l'usuari per path param.
- Escriu un missatge a la consola indicant que s'ha rebut la consulta de l'EJ2.
- Retorna una resposta JSON dummy amb el nom de l'equip i els membres.

## Fitxers modificats al backend

S'han afegit aquests fitxers:

- `src/main/java/edu/upc/dsa/services/UserServicio.java`
- `src/main/java/edu/upc/dsa/models/TeamResponse.java`
- `src/main/java/edu/upc/dsa/models/TeamMember.java`

## Resposta JSON

La resposta del backend te aquest format:

```json
{
  "team": "porxinos",
  "members": [
    {
      "name": "Juan",
      "avatar": "https://cdn.pixabay.com/photo/2017/07/11/15/51/kermit-2493979_1280.png",
      "points": 250
    },
    {
      "name": "Palomo",
      "avatar": "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_960_720.png",
      "points": 200
    },
    {
      "name": "Marti",
      "avatar": "https://cdn.pixabay.com/photo/2016/11/14/17/39/person-1824144_960_720.png",
      "points": 180
    }
  ]
}
```

## Android implementat

La part Android esta implementada en aquest repositori i branca:

https://github.com/MartiSabater/Grup1.ProjecteAndroid/tree/MINIM2MARTI

A Android s'ha implementat la funcionalitat per consultar el backend mitjancant Retrofit i mostrar els membres de l'equip amb:

- Avatar o imatge.
- Nom de l'usuari.
- Punts.

## Que funciona

- El backend exposa la ruta `GET /user/{idUser}/team`.
- La ruta retorna una resposta JSON amb l'estructura demanada a l'enunciat.
- El backend imprimeix un missatge per consola quan rep la peticio.
- La funcionalitat d'Android esta feta al repositori indicat.
- La comunicacio Android-backend es fa mitjancant Retrofit, tal com demana l'enunciat.

## Que queda pendent

No queda cap part obligatoria de l'EJ2 pendent per a l'entrega.

Com a millora futura, es podria substituir la resposta dummy per dades reals guardades en base de dades si mes endavant el projecte ho necessita.

## Evidencies

Per a l'entrega s'han de presentar captures de pantalla del funcionament:

- Captura de la pantalla Android mostrant els membres de l'equip.
- Captura o prova de la peticio al backend.
- Captura de la consola del backend mostrant el missatge rebut per la ruta de l'EJ2.

## Conclusio

L'EJ2 esta completat. El backend proporciona la ruta REST requerida i Android consumeix aquesta ruta per mostrar els membres de l'equip de l'usuari.
