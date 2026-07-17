//Ejercicio 1.a
Channel chServidor = new Channel();

process Servidor(chServidor): {
  int contador = 0;
  String mensaje;
  while (true) {
    mensaje = chServidor.receive();
    if (mensaje == "sigue") {
      contador++;
    } else if (mensaje == "cuenta") {
      print(contador);
      contador = 0;
    }
  }
}

//Ejercicio 1.b
Channel chServidor = new Channel();
Channel chCliente = new Channel();

process Servidor(chServidor, chCliente): {
  int contador = 0;
  String mensaje;
  while (true) {
    mensaje = chServidor.receive();
    if (mensaje == "sigue") {
      contador++;
    } else if (mensaje == "cuenta") {
      chCliente.send(contador);
      contador = 0;
    }
  }
}

process Cliente(chServidor, chCliente): {
  String accion = read();
  chServidor.send(accion);
  if (accion == "cuenta") {
    int valor = chCliente.receive();
    print(valor);
  }
}

//Ejercicio 2
Channel chClienteA = new Channel();
Channel chClienteB = new Channel();

process Servidor(chClienteA, chClienteB): {
  String mensajeA;
  String mensajeB;
  String concatenado;
  while (true) {
    mensajeA = chClienteA.receive();
    mensajeB = chClienteB.receive();
    concatenado = mensajeA + mensajeB;
    print(concatenado);
  }
}

process ClienteA(chClienteA): {
  String mensaje;
  while (true) {
    mensaje = read();
    chClienteA.send(mensaje);
    sleep(randint());
  }
}

process ClienteB(chClienteB): {
  String mensaje;
  while (true) {
    mensaje = read();
    chClienteB.send(mensaje);
    sleep(randint());
  }
}

//Ejercicio 3
Channel chServidor = new Channel();

process Servidor(chServidor): {
  Object valor;
  Request req;
  while (true) {
    req = chServidor.receive();
    if (req.accion == "set") {
      valor = req.nuevoValor;
    } else if (req.accion == "get") {
      req.chCliente.send(valor);
    }
  }
}

process Cliente(chServidor): {
  Request req = new Request();
  req.accion = read();
  if (req.accion == "set") {
    req.nuevoValor = ...; //Alguna manera de conseguir el valor
  } else if (req.accion == "get") {
    req.chCliente = new Channel();
  }
  chServidor.send(req);
}

class Request implements Serializable {
  String accion;
  Object nuevoValor;
  Channel chCliente;
}

//Ejercicio 4.a
Channel chRequestTrim = new Channel();
Channel chResponseTrim = new Channel();

process ServidorTrimming(chRequestTrim, chResponseTrim): {
  String mensaje;
  String resultado;
  while (true) {
    mensaje = chRequestTrim.receive();
    resultado = mensaje.trim();
    chResponseTrim.send(resultado);
  }
}

process Cliente(chRequestTrim, chResponseTrim): {
  String mensaje = read();
  chRequestTrim.send(mensaje);
  String resultado = chResponseTrim.receive();
}

//Ejercicio 4.b
Channel chTrimming = new Channel();

process ServidorTrimming(chTrimming): {
  Request req;
  while (true) {
    req = chTrimming.receive();
    thread Sesion(req): {
      String resultado = req.mensaje.trim();
      req.chCliente.send(resultado);
    }
  }
}

process Cliente(chTrimming): {
  Request req = new Request();
  req.mensaje = read();
  req.chCliente = new Channel();
  chTrimming.send(req);
  String resultado = req.chCliente.receive();
}

class Request implements Serializable {
  String mensaje;
  Channel chCliente;
}

//Ejercicio 5
Channel chServidor = new Channel();

process ServidorNumeros(chServidor): {
  Channel chCliente;
  while (true) {
    chCliente = chServidor.receive();
    thread Sesion(chCliente): {
      int numeroPseudoaleatorio = (int) (random() * 10);
      Channel chSesion = new Channel();
      chCliente.send(chSesion);
      boolean adivino = false;
      int intento;
      while (!adivino) {
        intento = chSesion.receive();
        adivino = (numeroPseudoaleatorio == intento);
        chCliente.send(adivino);
      }
    }
  }
}

process Cliente(chServidor): {
  Channel chCliente = new Channel();
  chServidor.send(chCliente);
  Channel chSesion = chCliente.receive();
  boolean adivine = false;
  int intento;
  while (!adivine) {
    intento = parseInt(read());
    chSesion.send(intento);
    adivine = chCliente.receive();
  }
}

//Ejercicio 6.a
Channel chServicioT = new Channel();
Channel chServicioR = new Channel();

process ServicioT(chServicioT, chServicioR): {
  String mensaje;
  String mensajeCodificado;
  while (true) {
    mensaje = chServicioT.receive();
    mensajeCodificado = codificar(mensaje);
    chServicioR.send(mensajeCodificado);
  }
}

//Ejercicio 6.b
Channel chServicioT = new Channel();

process ServicioT(chServicioT): {
  Request req;
  Channel chDestino;
  String mensajeCodificado;
  while (true) {
    req = chServicioT.receive();
    if (req.cambiarDestinatario) {
      chDestino = req.chServicioR;
    } else {
      mensajeCodificado = codificar(req.mensaje);
      chDestino.send(mensajeCodificado);
    }
  }
}

class Request implements Serializable {
  String mensaje;
  boolean cambiarDestinatario;
  Channel chServicioR;
}

//Ejercicio 6.c
Channel chServicioT = new Channel();
Channel chServicioK = new Channel();

process ServicioT(chServicioT, chServicioK): {
  Request req;
  Channel chDestino;
  while (true) {
    req = chServicioT.receive();
    if (req.cambiarDestinatario) {
      chDestino = req.chServicioR;
    } else {
      thread Clave(chServicioK, chDestino, req.mensaje): {
        Channel chClave = new Channel();
        chServicioK.send(chClave);
        String clave = chClave.receive();
        String mensajeCodificado = codificar(req.mensaje, clave);
        chDestino.send(mensajeCodificado);
      }
    }
  }
}

class Request implements Serializable {
  String mensaje;
  boolean cambiarDestinatario;
  Channel chServicioR;
}

//Ejercicio 7.a
Channel c1 = new Channel();
Channel c2 = new Channel();
Channel c3 = new Channel();
Channel c4 = new Channel();

process ServicioP(c1, c2, c3, c4): {
  Object mensaje;
  Object respuesta;
  while (true) {
    mensaje = c3.receive();
    c1.send(mensaje);
    respuesta = c2.receive();
    c4.send(respuesta);
  }
}

//Ejercicio 7.b
Channel c1 = new Channel();
Channel c2 = new Channel();
Channel c3 = new Channel();
Channel c4 = new Channel();

process ServicioP(c1, c2, c3, c4): {
  int cantidadTotal = N;
  Channel puedePedir = new Channel();
  repeat (cantidadTotal) puedePedir.send();
  thread Pedir(c1, c3, puedePedir): {
    Object mensaje;
    while (true) {
      mensaje = c3.receive();
      puedePedir.receive();
      c1.send(mensaje);
    }
  }
  thread Responder(c2, c4, puedePedir): {
    Object respuesta;
    while (true) {
      respuesta = c2.receive();
      puedePedir.send();
      c4.send(respuesta);
    }
  }
}

//Ejercicio 7.c
Channel c1 = new Channel();
Channel c2 = new Channel();
Channel c3 = new Channel();
Channel c4 = new Channel();
Channel c11 = new Channel();
Channel c22 = new Channel();

process ServicioP(c1, c2, c3, c4, c11, c22): {
  int cantidadTotal = N;
  Channel puedePedirS1 = new Channel();
  Channel puedePedirS2 = new Channel();
  repeat (cantidadTotal) {
    puedePedirS1.send();
    puedePedirS2.send();
  }
  thread Pedir(c1, c3, c11, puedePedirS1, puedePedirS2): {
    Object mensaje;
    while (true) {
      mensaje = c3.receive();
      puedePedir.receive();
      c1.send(mensaje);
    }
  }
  thread Responder(c2, c4, c22, puedePedirS1, puedePedirS2): {
    Object respuesta;
    while (true) {
      respuesta = c2.receive();
      puedePedir.send();
      c4.send(respuesta);
    }
  }
  thread Gestor(...): {...}
  //llevar registro de cuando los mensajes que llegan si se los mande al 1 o al 11
  //y cuando voy a devolver tengo que fijarme en ese registro si le toca devolver al 1 o le toca devolver al 11
  //y tengo que esperar cuando le toca devolver al otro
}

//Ejercicio 8.a
Channel chRecepcion = new Channel();
int frecuencia = N;
int cantMinima = M;

process Timer(chRecepcion, frecuencia, cantMinima): {
  Channel chClientes = new Channel();
  List<Channel> clientes = new ArrayList<>();
  chClientes.send(clientes);
  Channel puedeGenerar = new Channel();
  thread Recepcion(chRecepcion, chClientes, cantMinima, puedeGenerar): {
    Channel chCliente = new Channel();
    List<Channel> clientes;
    while (true) {
      chCliente = chRecepcion.receive();
      clientes = chClientes.receive();
      clientes.add(chCliente);
      chClientes.send(clientes);
      if (clientes.size() == cantMinima) puedeGenerar.send();
    }
  }
  thread Tick(chClientes, frecuencia, puedeGenerar): {
    List<Channel> clientes;
    puedeGenerar.receive();
    while (true) {
      clientes = chClientes.receive();
      chClientes.send(clientes);
      for (c in clientes) c.send("tick");
      sleep(frecuencia);
    }
  }
}

//Ejercicio 8.b
Channel chResponseEstado = new Channel();
Channel[] chRequestEstado = {new Channel(),...};
Channel chRecepcion = new Channel();
boolean estadoInicial = B;

process Cell(chResponseEstado, chRequestEstado, chRecepcion, estadoInicial): {
  Channel chCell = new Channel();
  chRecepcion.send(chCell);
  Channel chEstado = new Channel();
  chEstado.send(estadoInicial);
  while (true) {
    chCell.receive();
    thread Celda(chResponseEstado, chRequestEstado, chEstado): {
      boolean estado = chEstado.receive();
      for (i in range[0,7]) chRequestEstado[i].send(estado);
      boolean estadoVecina;
      int cantVivas = 0;
      repeat (8) {
        estadoVecina = chResponseEstado.receive();
        if (estadoVecina) cantVivas++;
      }
      if (estado) {
        estado = (cantVivas == 2 || cantVivas == 3);
      } else {
        estado = (cantVivas == 3);
      }
      chEstado.send(estado);
    }    
  }
}

//Simulacro Ejercicio 2.a
Channel vuelo = new Channel();
Channel hotel = new Channel();
Channel auto = new Channel();
Channel agencia = new Channel();

process AgenciaViajes(vuelo, hotel, auto, agencia): {
  Request req;
  Request reqAgencia = new Request();
  boolean disponibilidad;
  while (true) {
    req = agencia.receive();
    reqAgencia.fecha = req.fecha;
    reqAgencia.chRespuesta = new Channel();
    vuelo.send(reqAgencia);
    hotel.send(reqAgencia);
    auto.send(reqAgencia);
    disponibilidad = chRespuesta.receive() && chRespuesta.receive() && chRespuesta.receive();
    req.chCliente.send(disponibilidad);
  }
}

class Request implements Serializable {
  Date fecha;
  Channel chCliente;
} //Cliente

class Request implements Serializable {
  Date fecha;
  Channel chRespuesta;
} //Agencia

//Simulacro Ejercicio 2.b
Channel vuelo = new Channel();
Channel hotel = new Channel();
Channel auto = new Channel();
Channel agencia = new Channel();

process AgenciaViaje(vuelo, hotel, auto, agencia): {
  Request req;
  while (true) {
    req = agencia.receive();
    thread Sesion(vuelo, hotel, auto, req): {
      Request reqSesion = new Request();
      reqSesion.fecha = req.fecha;
      reqSesion.chSesion = new Channel();
      vuelo.send(reqSesion);
      hotel.send(reqSesion);
      auto.send(reqSesion);
      boolean disponibilidad = chSesion.receive() && chSesion.receive() && chSesion.receive();
      req.chCliente.send(disponibilidad);
    }
  }
}

class Request implements Serializable {
  Date fecha;
  Channel chCliente;
} //Cliente

class Request implements Serializable {
  Date fecha;
  Channel chSesion;
} //Sesion

//Simulacro Ejercicio 2.c
Channel[] vuelo = {new Channel(),...};
Channel[] hotel = {new Channel(),...};
Channel[] auto = {new Channel(),...};
Channel agencia = new Channel();

process AgenciaViajes(vuelo, hotel, auto, agencia): {
  Request req;
  while (true) {
    req = agencia.receive();
    thread Sesion(vuelo, hotel, auto, req): {
      Channel chRespuesta = new Channel();
      thread Vuelo(vuelo, chRespuesta, req.fecha): {
        consultar(vuelo, chRespuesta, req.fecha);
      }
      thread Hotel(hotel, chRespuesta, req.fecha): {
        consultar(hotel, chRespuesta, req.fecha);
      }
      thread Auto(auto, chRespuesta, req.fecha): {
        consultar(auto, chRespuesta, req.fecha);
      }
      boolean disponibilidad = chRespuesta.receive() && chRespuesta.receive() && chRespuesta.receive();
      req.chCliente.send(disponibilidad);
    }
  }
}

void consultar(Channel[] canalesServicio, Channel chRespuesta, Date fecha) {
  Request req = new Request();
  req.fecha = fecha;
  req.chSesion = new Channel();
  for (chServicio : canalesServicio) chServicio.send(req);
  int cantRespuestasRestantes = canalesServicio.length;
  boolean servicioDisponible = false;
  while (cantRespuestasRestantes > 0 && !servicioDisponible) {
    servicioDisponible = req.chSesion.receive();
    cantRespuestasRestantes--;
  }
  chRespuesta.send(servicioDisponible);
}

class Request implements Serializable {
  Date fecha;
  Channel chCliente;
} //Cliente

class Request implements Serializable {
  Date fecha;
  Channel chSesion;
} //Sesion

//Simulacro Ejercicio 4.a
Channel chProxy = new Channel();
Channel chServidor = new Channel();
int idAgente = ID;

process Agente(idAgente, chProxy): {
  int id = idAgente;
  Reporte reporte;
  while (true) {
    reporte = new Reporte(id);
    chProxy.send(reporte);
    sleep(60000);
  }
}

process Proxy(chProxy, chServidor): {
  Reporte reporte;
  while (true) {
    reporte = chProxy.receive();
    chServidor.send(reporte);
  }
}

process Servidor(chProxy): {
  Reporte reporte;
  while (true) {
    reporte = chProxy.receive();
    print(reporte);
  }
}

//Simulacro Ejercicio 4.b
Channel chReqProxy = new Channel();
Channel chServidor = new Channel();
Channel chRespProxy = new Channel();
int idAgente = ID;

process Agente(idAgente, chReqProxy): {
  int id = idAgente;
  Request req = new Request();
  int numero;
  while (true) {
    req.reporte = new Reporte(id);
    req.chAgente = new Channel();
    chReqProxy.send(req);
    sleep(60000);
    numero = req.chAgente.receive();
    id += numero;
  }
}

process Proxy(chReqProxy, chServidor, chRespProxy): {
  Request req;
  while (true) {
    req = chReqProxy.receive();
    chServidor.send(req.reporte);
    thread Sesion(req.chAgente, chRespProxy): {
      int numero = chRespProxy.receive();
      req.chAgente.send(numero);
    }
  }
}

process Servidor(chReqProxy, chRespProxy): {
  Reporte reporte;
  int numero;
  while (true) {
    reporte = chReqProxy.receive();
    print(reporte);
    numero = randint();
    chRespProxy.send(numero);
  }
}

class Request implements Serializable {
  Reporte reporte;
  Channel chAgente;
}

//Simulacro Ejercicio 4.c
Channel chReqProxy = new Channel();
Channel chServidor = new Channel();
Channel chRespProxy = new Channel();
int idAgente = ID;

process Agente(idAgente, chReqProxy): {
  int id = idAgente;
  Request req = new Request();
  int numero;
  while (true) {
    req.reporte = new Reporte(id);
    req.chAgente = new Channel();
    chReqProxy.send(req);
    sleep(60000);
    numero = req.chAgente.receive();
    id += numero;
  }
}

process Proxy(chReqProxy, chServidor, chRespProxy): {
  Request req;
  while (true) {
    req = chReqProxy.receive();
    chServidor.send(req.reporte);
    thread Sesion(req.chAgente, chRespProxy): {
      int numero = chRespProxy.receive();
      req.chAgente.send(numero);
    }
  }
}

process Servidor(chReqProxy, chRespProxy): {
  Channel chTimeout = new Channel();
  chTimeout.send("No llego nada");
  thread Timeout(chTimeout): {
    String mensaje;
    while (true) {
      sleep(120000);
      mensaje = chTimeout.receive();
      chTimeout.send("No llegó nada");
      if (mensaje == "No llegó nada") print(mensaje);
    }
  }
  thread Sesion(chReqProxy, chRespProxy, chTimeout): {
    Reporte reporte;
    int numero;
    while (true) {
      reporte = chReqProxy.receive();
      chTimeout.receive();
      chTimeout.send("Llegó algo");
      print(reporte);
      numero = randint();
      chRespProxy.send(numero);
    }
  }
}

class Request implements Serializable {
  Reporte reporte;
  Channel chAgente;
}
