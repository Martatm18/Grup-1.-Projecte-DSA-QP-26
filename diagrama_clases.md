classDiagram

class Partida {
  -String id
  -Jugador jugador
  -Mapa mapa
  -SIGMA sigma
  -List~Mision~ misiones
  -Mision misionActual
  -int actoActual
  -EstadoPartida estado
  +iniciar() void
  +pausar() void
  +reanudar() void
  +finalizar() void
  +actualizarProgreso() void
  +cambiarActo(int nuevoActo) void
  +iniciarMision(Mision mision) void
  +completarMisionActual() void
}

class Jugador {
  -String id
  -String nombre
  -int vida
  -int vidaMaxima
  -int ects
  -Zona ubicacionActual
  -Inventario inventario
  +moverse(Zona zona) void
  +recogerObjeto(Objeto objeto) void
  +usarObjeto(Objeto objeto) void
  +hablarCon(NPC npc) void
  +interactuarCon(Terminal terminal) void
  +ganarECTS(int cantidad) void
  +gastarECTS(int cantidad) boolean
  +recibirDanio(int cantidad) void
  +curarse(int cantidad) void
  +estaVivo() boolean
}

class Inventario {
  -List~Objeto~ objetos
  -int capacidadMaxima
  +agregarObjeto(Objeto objeto) boolean
  +eliminarObjeto(Objeto objeto) boolean
  +usarObjeto(String nombre) boolean
  +contieneObjeto(String nombre) boolean
  +obtenerObjeto(String nombre) Objeto
  +listarObjetos() List~Objeto~
  +estaLleno() boolean
}

class Objeto {
  -String id
  -String nombre
  -String descripcion
  -TipoObjeto tipo
  -int coste
  -boolean usable
  +usar(Jugador jugador) void
  +obtenerDescripcion() String
}

class Registro {
  -String contenido
  -boolean corrupto
  -Terminal origen
  +leer() String
  +estaCorrupto() boolean
}

class FragmentoCodigo {
  -String valor
  -int orden
  +combinar(FragmentoCodigo otro) Objeto
}

class Mapa {
  -List~Zona~ zonas
  -Zona zonaInicial
  +agregarZona(Zona zona) void
  +obtenerZona(String nombre) Zona
  +moverJugador(Jugador jugador, Zona destino) boolean
  +desbloquearZona(String nombreZona, Objeto objeto) boolean
  +obtenerZonasDisponibles() List~Zona~
}

class Zona {
  -String id
  -String nombre
  -String descripcion
  -boolean bloqueada
  -String requiereObjeto
  -List~Terminal~ terminales
  -List~NPC~ npcs
  -List~Enemigo~ enemigos
  -List~Objeto~ objetos
  +entrar(Jugador jugador) boolean
  +desbloquear(Objeto objeto) boolean
  +agregarObjeto(Objeto objeto) void
  +eliminarObjeto(Objeto objeto) void
  +obtenerTerminales() List~Terminal~
  +obtenerNPCs() List~NPC~
  +obtenerEnemigos() List~Enemigo~
}

class NPC {
  -String id
  -String nombre
  -List~String~ dialogos
  -Zona ubicacion
  -Mision misionAsociada
  +hablar(Jugador jugador) String
  +darObjeto(Jugador jugador, Objeto objeto) void
  +asignarMision(Jugador jugador, Mision mision) void
  +completarMision(Jugador jugador) void
}

class Profesor {
  -String departamento
  -int nivelAutorizacion
  -List~Pregunta~ preguntas
  +explicarContexto() String
  +hacerPregunta(Jugador jugador) Pregunta
  +evaluarRespuesta(Pregunta pregunta, String respuesta, Jugador jugador) boolean
  +entregarTarjeta(Jugador jugador) void
}

class Hacker {
  -Tienda tienda
  +abrirTienda() Tienda
  +venderObjeto(Jugador jugador, Objeto objeto) boolean
  +darPista() String
}

class Conserje {
  -Zona zonaVigilada
  +contarIncidente() String
  +entregarFragmentoCodigo(Jugador jugador) void
  +darPistaAcceso() String
}

class Tienda {
  -String id
  -String nombre
  -List~Objeto~ objetosDisponibles
  +mostrarObjetos() List~Objeto~
  +comprar(Jugador jugador, Objeto objeto) boolean
  +tieneObjeto(String nombre) boolean
  +obtenerPrecio(Objeto objeto) int
}

class Mision {
  -String id
  -String nombre
  -String descripcion
  -EstadoMision estado
  -int acto
  -List~Objetivo~ objetivos
  -List~Objeto~ recompensas
  -int ectsRecompensa
  -Mision siguienteMision
  +iniciar() void
  +completar() void
  +fallar() void
  +actualizarProgreso(Jugador jugador) void
  +estaCompletada() boolean
  +obtenerObjetivoActual() Objetivo
  +entregarRecompensas(Jugador jugador) void
}

class Objetivo {
  -String id
  -String descripcion
  -TipoObjetivo tipo
  -boolean completado
  -String objetoRequerido
  -Zona zonaRequerida
  -NPC npcRequerido
  -Terminal terminalRequerida
  +comprobar(Jugador jugador) boolean
  +completar() void
}

class Terminal {
  -String id
  -String nombre
  -Zona ubicacion
  -boolean activa
  -boolean bloqueada
  -int nivelAutorizacionRequerido
  -List~Registro~ registrosDisponibles
  -SIGMA sigma
  -Puzzle puzzle
  +interactuar(Jugador jugador) String
  +conectarSigma() boolean
  +descargarRegistros(Jugador jugador) List~Registro~
  +desbloquear(Jugador jugador) boolean
  +resolverPuzzle(String respuesta, Jugador jugador) boolean
}

class SIGMA {
  -String id
  -String nombre
  -EstadoSIGMA estado
  -int nivelControl
  -boolean registrosEliminados
  -boolean dronesActivados
  -List~String~ dialogos
  +responder(String mensaje, Jugador jugador) String
  +entregarRegistro(Terminal terminal, Jugador jugador) Registro
  +comprobarPermisos(Jugador jugador, Terminal terminal) boolean
  +activarDrones() void
  +desactivarDronesTemporalmente() void
  +darPistaCodigo(Jugador jugador) String
  +procesarDecisionFinal(DecisionFinal decision) void
}

class Puzzle {
  -String id
  -String enunciado
  -String respuestaCorrecta
  -boolean resuelto
  -Objeto recompensa
  +comprobarRespuesta(String respuesta) boolean
  +resolver(Jugador jugador) void
  +darPista() String
}

class Enemigo {
  -String id
  -String nombre
  -int vida
  -int danio
  -Zona ubicacionActual
  -EstadoEnemigo estado
  -double velocidad
  -boolean activo
  +patrullar() void
  +perseguir(Jugador jugador) void
  +atacar(Jugador jugador) void
  +recibirDanio(int cantidad) void
  +desactivar() void
  +activar() void
  +estaActivo() boolean
}

class Dron {
  -int municion
  -double rangoVision
  -int tiempoDesactivado
  +detectarJugador(Jugador jugador) boolean
  +disparar(Jugador jugador) void
  +desactivarTemporalmente(int segundos) void
  +recargar() void
}

class RobotHumanoide {
  -int nivelBlindaje
  -int fuerzaGolpe
  +bloquearPaso(Jugador jugador) void
  +golpear(Jugador jugador) void
}

class Pregunta {
  -String id
  -String enunciado
  -List~String~ opciones
  -String respuestaCorrecta
  -int recompensaECTS
  +comprobarRespuesta(String respuesta) boolean
  +obtenerRecompensa() int
}

class EstadoPartida {
  <<enumeration>>
  MENU
  EN_CURSO
  PAUSADO
  FINALIZADO
  GAME_OVER
}

class EstadoMision {
  <<enumeration>>
  NO_INICIADA
  ACTIVA
  COMPLETADA
  FALLIDA
}

class TipoObjetivo {
  <<enumeration>>
  HABLAR_NPC
  IR_ZONA
  OBTENER_OBJETO
  USAR_OBJETO
  INTERACTUAR_TERMINAL
  DERROTAR_ENEMIGO
  RESOLVER_PUZZLE
  COMPRAR_OBJETO
}

class TipoObjeto {
  <<enumeration>>
  CONSUMIBLE
  HERRAMIENTA
  REGISTRO
  FRAGMENTO_CODIGO
  ACCESO
  MAPA
}

class EstadoSIGMA {
  <<enumeration>>
  ACTIVA
  RESTRINGIDA
  INESTABLE
  APAGADA
  REINICIADA
}

class EstadoEnemigo {
  <<enumeration>>
  PASIVO
  PATRULLANDO
  PERSIGUIENDO
  ATACANDO
  DESACTIVADO
}

class DecisionFinal {
  <<enumeration>>
  APAGAR_SIGMA
  REINICIAR_SIGMA
  MANTENER_SIGMA_ACTIVA
}

Objeto <|-- Registro
Objeto <|-- FragmentoCodigo

NPC <|-- Profesor
NPC <|-- Hacker
NPC <|-- Conserje

Enemigo <|-- Dron
Enemigo <|-- RobotHumanoide

Partida "1" *-- "1" Jugador
Partida "1" *-- "1" Mapa
Partida "1" --> "1" SIGMA
Partida "1" *-- "1..*" Mision
Partida "1" --> "0..1" Mision : misionActual

Jugador "1" *-- "1" Inventario
Jugador "1" --> "1" Zona : ubicacionActual

Inventario "1" o-- "0..*" Objeto

Mapa "1" *-- "1..*" Zona

Zona "1" o-- "0..*" Terminal
Zona "1" o-- "0..*" NPC
Zona "1" o-- "0..*" Enemigo
Zona "1" o-- "0..*" Objeto

Hacker "1" *-- "1" Tienda
Tienda "1" o-- "0..*" Objeto

Profesor "1" o-- "0..*" Pregunta

Mision "1" *-- "1..*" Objetivo
Mision "0..1" --> "0..1" Mision : siguienteMision
Mision "0..*" --> "0..*" Objeto : recompensas

Objetivo "0..1" --> "0..1" NPC
Objetivo "0..1" --> "0..1" Zona
Objetivo "0..1" --> "0..1" Terminal

Terminal "0..*" --> "1" SIGMA
Terminal "1" o-- "0..*" Registro
Terminal "1" --> "0..1" Puzzle

Puzzle "0..1" --> "0..1" Objeto : recompensa

SIGMA "1" --> "0..*" Enemigo : controla

Objeto --> TipoObjeto
Mision --> EstadoMision
Objetivo --> TipoObjetivo
Partida --> EstadoPartida
SIGMA --> EstadoSIGMA
Enemigo --> EstadoEnemigo
SIGMA --> DecisionFinal