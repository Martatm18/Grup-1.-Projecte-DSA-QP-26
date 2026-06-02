PROJECTE DSA GRUP 1 QP 26:
- COMMIT 22 -> He modificado el crear cuenta: añadiendo el campo correo, que un usuario no pueda tener el mismo correo que otro, hacer que las dos contraseñas tengan que ser iguales para asegurar así la doble verificación, en caso contrario aparecerá un mensaje eb pantalla, cuando ponemos una contraseña que nos diga que falta para que sea robusta, ya sean caracteres, símbolos...

## Segon minim - EJ2

Implementado en backend:
- Ruta dummy `GET /user/{idUser}/team`.
- Devuelve el nombre del equipo y un listado de miembros con `name`, `avatar` y `points`.
- Imprime en consola la consulta recibida, tal como pide el enunciado.

Ejemplo de respuesta:

```json
{
  "team": "porxinos",
  "members": [
    {
      "name": "Juan",
      "avatar": "https://cdn.pixabay.com/photo/2017/07/11/15/51/kermit-2493979_1280.png",
      "points": 250
    }
  ]
}
```

Pendiente:
- Acabar de implementar y comprobar que la conexion con Android funciona bien mediante Retrofit.
- Sustituir los datos dummy por datos reales si mas adelante se pide persistencia.
