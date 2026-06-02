# PROJECTE DSA GRUP 1 QP 26

## Backend - Asistente IA

Per l'exercici 1, en el que s'havia d'implementar un assistent IA/LLM, he afegit una nova funcionalitat al backend per poder rebre preguntes desde l'app Android i tornar una resposta.

La idea principal es que Android no parli directament amb el LLM, sino que faci una peticio a la nostra API REST. A partir d'aqui, el backend rep la pregunta, la prepara i la envia al LLM de la UPC. Quan el LLM respon, el backend retorna aquesta resposta a Android.

## Que he afegit

He creat un nou servei:

```text
src/main/java/edu/upc/dsa/services/AssistentServicio.java
```

Aquest servei afegeix la ruta:

```text
POST /dsaApp/assistant/ask
```

Tambee he creat els models necessaris per enviar i rebre dades:

```text
src/main/java/edu/upc/dsa/models/AssistentRequest.java
src/main/java/edu/upc/dsa/models/AssistentResponse.java
src/main/java/edu/upc/dsa/models/LLMRequest.java
src/main/java/edu/upc/dsa/models/LLMResponse.java
```

## Com funciona

Android envia una pregunta al backend amb aquest format:

```json
{
  "question": "Que articles puc comprar a la botiga?"
}
```

El backend rep la pregunta i fa una peticio al LLM de la UPC:

```text
http://10.4.119.50:8080/api/generate
```

Amb el model:

```text
qwen2.5:14b
```

Despres, el backend retorna a Android una resposta amb aquest format:

```json
{
  "answer": "Resposta generada per l'assistent"
}
```

## Primeres proves

Primer he comprovat que el LLM funcionava fora de l'aplicacio, fent una peticio manual desde terminal. Quan he vist que responia correctament, ho he integrat dins del backend.

Exemple de prova del nostre endpoint:

```bash
curl http://10.4.119.50:8080/api/generate -d '{
  "model": "qwen2.5:14b",
  "prompt": "Explica que es Docker",
  "stream": true
}'
```

Amb aquesta prova es pot veure que la resposta no es nomes una resposta fixa, perque Docker no forma part de les preguntes dummy de la botiga. Per tant, si respon sobre Docker, vol dir que esta passant pel LLM.

## Que funciona

- El backend te una nova ruta per l'assistent IA.
- Android pot enviar preguntes a aquesta ruta mitjancant Retrofit.
- El backend pot comunicar-se amb el LLM de la UPC.
- El backend retorna la resposta en un format que Android pot mostrar.
- S'ha provat amb preguntes sobre la botiga i tambe amb preguntes externes com Docker.
- Si el LLM no esta disponible, el backend encara pot retornar una resposta dummy basica.

##  dummy

He deixat una resposta dummy per si de cas, la primera prova. Aixo serveix perque, si el LLM no esta disponible o hi ha algun problema de xarxa, l'assistent no queda completament inutilitzat.

El dummy pot respondre preguntes que té a la seva llista, de compra, ects..., d'aquesta manera el backend pot donar una resposta basica encara que el LLM falli.

## Fallos coneguts

Les proves fetes estan el local per el moment, per no crear problemes amb el git dels meus companys. Per tant, si es prova fora de local no funcionarà. 
S'ha de canviar la base URI de Android i del Main, a més de pujar-ho a producció.
