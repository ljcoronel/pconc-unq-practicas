//Productor-Consumidor: Buffer de un solo elemento
monitor Buffer {

  boolean hayElemento = false;
  Object valor = null;

  void producir(Object nuevo) {
    while (hayElemento) wait();
    valor = nuevo;
    hayElemento = true;
    notifyAll();
  }

  Object consumir() {
    while (!hayElemento) wait();
    Object resultado = valor;
    valor = null;
    hayElemento = false;
    notifyAll();
    return resultado;
  }

}

//Productor-Consumidor: Buffer de tamaño N
monitor Buffer {

  Object[] datos = new Object[N+1];
  int inicio = 0;
  int fin = 0;

  void producir(Object nuevo) {
    while (lleno()) wait();
    datos[inicio] = nuevo;
    inicio = siguiente(incio);
    notifyAll();
  }

  Object consumir() {
    while (vacio()) wait();
    Object resultado = datos[fin];
    datos[fin] = null;
    fin = siguiente(fin);
    notifyAll();
    return resultado;
  }

  boolean vacio() {
    return inicio == fin;
  }

  boolean lleno() {
    return siguiente(inicio) == fin;
  }

  int siguiente(int i) {
    return (i+1)%(N+1);
  }

}

//Lectores-Escritores: Database prioridad lectores
monitor Database {

  int cantEscritores = 0;
  int cantLectores = 0;

  void inicioLectura() {
    while (!puedeLeer()) wait();
    cantLectores++;
  }

  void finLectura() {
    cantLectores--;
    if (cantLectores == 0) notify();
  }

  void inicioEscritura() {
    while (!puedeEscribir()) wait();
    cantEscritores = 1;
  }

  void finEscritura() {
    cantEscritores = 0;
    notifyAll();
  }

  boolean puedeLeer() {
    return cantEscritores == 0;
  }

  boolean puedeEscribir() {
    return cantEscritores == 0 && cantLectores == 0;
  }

}

//Lectores-Escritores: Database prioridad escritores
monitor Database {

  int cantEscritores = 0;
  int cantLectores = 0;
  int cantEscritoresEsperando = 0;

  void inicioLectura() {
    while (!puedeLeer()) wait();
    cantLectores++;
  }

  void finLectura() {
    cantLectores--;
    if (cantLectores == 0) notifyAll();
  }

  void inicioEscritura() {
    cantEscritoresEsperando++;
    while (!puedeEscribir()) wait();
    cantEscritoresEsperando--;
    cantEscritores = 1;
  }

  void finEscritura() {
    cantEscritores = 0;
    notifyAll();
  }

  boolean puedeLeer() {
    return cantEscritores == 0 && cantEscritoresEsperando == 0;
  }

  boolean puedeEscribir() {
    return cantEscritores == 0 && cantLectores == 0;
  }

}

//Ejercicio 1
monitor Contador {

  int valor = 0;

  void incrementar() {
    valor++;
  }

  void decrementar() {
    valor--;
  }

}

//Ejercicio 2
monitor Semaforo {

  int permisos = N;

  void acquire() {
    while (permisos==0) wait();
    permisos--;
  }

  void release() {
    permisos++;
    notify();
  }

} //Tiene starvation

//Ejercicio 3
monitor SecuenciadorTernario {

  int proximo = 0;

  void primero() {
    invocar(0);
  }

  void segundo() {
    invocar(1);
  }

  void tercero() {
    invocar(2);
  }

  void invocar(int valor) {
    while (proximo != valor) wait();
    proximo = (valor + 1) % 3;
    notifyAll();
  }

}

//Ejercicio 4
monitor Barrera {

  int restantes = N;

  void esperar() {
    if (restantes > 0) restantes--;
    while (restantes > 0) wait();
    notify();
  }

}

//Ejercicio 5
monitor Event {

  int ultimoPublish = 0; //contador de epoch o epocas

  void publish() {
    ultimoPublish++; //Incremento el reloj logico
    notifyAll();
  }

  void suscribe() {
    int miPublish = ultimoPublish + 1;
    while (ultimoPublish < miPublish) wait();
  }

}

//Ejercicio 6.a
monitor Grid {

  int cantProductores = 0;
  int cantConsumidores = 0;

  void inicioConsumo() {
    while (!puedeComportarseComoConsumidor()) wait();
    cantConsumidores++;
  }

  void finConsumo() {
    cantConsumidores--;
    notifyAll();
  }

  void inicioProduccion() {
    cantProductores++;
    notifyAll();
  }

  void finProduccion() {
    while (!puedeDejarDeProducir()) wait();
    cantProductores--;
  }

  boolean puedeComportarseComoConsumidor() {
    return cantProductores >= cantConsumidores;
  }

  boolean puedeDejarDeProducir() {
    return cantProductores > cantConsumidores;
  }

}

//Ejercicio 6.b
monitor Grid {

  int cantProductores = 0;
  int cantConsumidores = 0;
  int cantProductoresOciosos = N;

  void inicioConsumo() {
    while (!puedeComportarseComoConsumidor()) wait();
    cantConsumidores++;
    notifyAll();
  }

  void finConsumo() {
    cantConsumidores--;
    notifyAll();
  }

  void inicioProduccion() {
    while (!puedeComportarseComoProductor()) wait();
    cantProductores++;
    notify();
  }

  void finProduccion() {
    while (!puedeDejarDeProducir()) wait();
    cantProductores--;
    notifyAll();
  }

  boolean puedeComportarseComoConsumidor() {
    return cantProductores >= cantConsumidores;
  }

  boolean puedeDejarDeProducir() {
    return cantProductores > cantConsumidores;
  }

  boolean puedeComportarseComoProductor() {
    return cantProductoresMasQueConsumidores() < cantProductoresOciosos;
  }

  int cantProductoresMasQueConsumidores() {
    return cantProductores - cantConsumidores;
  }

}

//Ejercicio 6.c
monitor Grid {

  int cantProductores = 0;
  int cantConsumidores = 0;
  int cantProductoresOciosos = N;
  int cantProductoresEsperando = 0;

  void inicioConsumo() {
    while (!puedeComportarseComoConsumidor()) wait();
    cantConsumidores++;
    notifyAll();
  }

  void finConsumo() {
    cantConsumidores--;
    notifyAll();
  }

  void inicioProduccion() {
    while (!puedeComportarseComoProductor()) wait();
    cantProductores++;
    notifyAll();
  }

  void finProduccion() {
    cantProductoresEsperando++;
    while (!puedeDejarDeProducir()) wait();
    cantProductoresEsperando--;
    cantProductores--;
    notify();
  }

  boolean puedeComportarseComoConsumidor() {
    return hayCantidadIgualOMayorDeProductores() && cantProductoresEsperando == 0;
  }

  boolean puedeDejarDeProducir() {
    return cantProductores > cantConsumidores;
  }

  boolean puedeComportarseComoProductor() {
    return cantProductoresMasQueConsumidores() < cantProductoresOciosos;
  }

  boolean hayCantidadIgualOMayorDeProductores() {
    return cantProductores >= cantConsumidores;
  }

  int cantProductoresMasQueConsumidores() {
    return cantProductores - cantConsumidores;
  }

}

//Ejercicio 7.a
monitor Sala {

  int capacidad = N;
  int cantAsistentes = 0;
  boolean hayCharlaEnCurso = false;

  void entrarSala() {
    while (!puedeEntrar()) wait();
    cantAsistentes++;
    if (esElPrimerAsistente()) notify();
  }

  void salirSala() {
    while (!puedeRetirarse()) wait();
    cantAsistentes--;
    notify();
  }

  void iniciarCharla() {
    while (!puedeIniciarLaCharla()) wait();
    hayCharlaEnCurso = true;
  }

  void finalizarCharla() {
    hayCharlaEnCurso = false;
    notifyAll();
  }

  boolean puedeRetirarse() {
    return !hayCharlaEnCurso;
  }

  boolean puedeIniciarLaCharla() {
    return !hayCharlaEnCurso && hayAsistentes();
  }

  boolean puedeEntrar() {
    return capacidad > cantAsistentes;
  }

  boolean esElPrimerAsistente() {
    return cantAsistentes == 1;
  }

  boolean hayAsistentes() {
    return cantAsistentes > 0;
  }

}

//Ejercicio 7.b
monitor Sala {

  int capacidad = N;
  int cantAsistentes = 0;
  boolean hayCharlaEnCurso = false;
  int charlaActual = 0;

  void entrarSala(int numeroDeCharla) {
    while (!puedeEntrar(numeroDeCharla)) wait();
    cantAsistentes++;
    if (esElPrimerAsistente()) notifyAll();
  }

  void salirSala() {
    while (!puedeRetirarse()) wait();
    cantAsistentes--;
    notifyAll();
  }

  void iniciarCharla(int numeroDeCharla) {
    while (!puedeIniciarLaCharla(numeroDeCharla)) wait();
    hayCharlaEnCurso = true;
  }

  void finalizarCharla() {
    hayCharlaEnCurso = false;
    rotarCharla();
    notifyAll();
  }

  boolean puedeRetirarse() {
    return !hayCharlaEnCurso;
  }

  boolean puedeIniciarLaCharla(int numeroDeCharla) {
    return !hayCharlaEnCurso && hayAsistentes() && esCharlaCorrespondiente(numeroDeCharla);
  }

  boolean puedeEntrar(int numeroDeCharla) {
    return hayLugar() && esCharlaCorrespondiente(numeroDeCharla);
  }

  boolean esElPrimerAsistente() {
    return cantAsistentes == 1;
  }

  boolean hayAsistentes() {
    return cantAsistentes > 0;
  }

  boolean hayLugar() {
    return capacidad > cantAsistentes;
  }

  void rotarCharla() {
    charlaActual = (charlaActual + 1) % 3;
  }

  boolean esCharlaCorrespondiente(int numeroDeCharla) {
    return charlaActual == numeroDeCharla;
  }

}

//Simulacro Ejercicio 1.a
monitor Encoder {

  int limiteAlmacenamiento = M;
  int limitePaquete = P;
  List<Frame> cuadrosCrudos = new ArrayList<Frame>();

  void putRawFrame(Frame frame) {
    while (!puedeAlmacenar()) wait();
    cuadrosCrudos.add(frame);
    if (esLimiteParaEncodear()) notify();
  }

  List<Frame> getPack() {
    while (!puedeRealizarProcesoDeCompresion()) wait();
    List<Frame> paquete = new ArrayList<Frame>();
    for (int i = 0; i < limitePack; i++) paquete.add(cuadrosCrudos.pop());
    notifyAll();
    return paquete;
  }

  boolean puedeAlmacenar() {
    return cuadrosCrudos.size() < limiteAlmacenamiento;
  }

  boolean puedeRealizarProcesoDeCompresion() {
    return cuadrosCrudos.size() >= limitePaquete;
  }

  boolean esLimiteParaEncodear() {
    return cuadrosCrudos.size() == limitePaquete;
  }

}

//Simulacro Ejercicio 1.b
monitor Encoder {

  int limiteAlmacenamiento = M;
  List<Frame> cuadrosCrudos = new ArrayList<Frame>();

  void putRawFrame(Frame frame) {
    while (!puedeAlmacenar()) wait();
    cuadrosCrudos.add(frame);
    notifyAll();
  }

  List<Frame> getPack(int p) {
    while (!puedeRealizarProcesoDeCompresion(p)) wait();
    List<Frame> paquete = new ArrayList<Frame>();
    for (int i = 0; i < limitePack; i++) paquete.add(cuadrosCrudos.pop());
    notifyAll();
    return paquete;
  }

  boolean puedeAlmacenar() {
    return cuadrosCrudos.size() < limiteAlmacenamiento;
  }

  boolean puedeRealizarProcesoDeCompresion(int p) {
    return cuadrosCrudos.size() >= p;
  }

}

//Simulacro Ejercicio 1.c
monitor Encoder {

  int limiteAlmacenamiento = M;
  int limiteEncoding = K;
  int cantTareasEncoding = 0;
  List<Frame> cuadrosCrudos = new ArrayList<Frame>();
  List<Frame> cuadrosEncodeados = new ArrayList<Frame>();

  void putRawFrame(Frame frame) {
    while (!puedeAlmacenar()) wait();
    cuadrosCrudos.add(frame);
    notifyAll();
  }

  List<Frame> getPack(int p) {
    while (!puedeRealizarProcesoDeCompresion(p)) wait();
    cantTareasEncoding++;
    List<Frame> paquete = new ArrayList<Frame>();
    for (int i = 0; i < limitePack; i++) paquete.add(cuadrosCrudos.pop());
    notifyAll();
    return paquete;
  }

  void putEncodedPack(List<Frame> encodedPack) {
    //PRECOND: Sólo puede llamarse luego de una invocación a getPack.
    cuadrosEncodeados.addAll(encodedPack);
    cantTareasEncoding--;
    notifyAll();
  }

  boolean puedeAlmacenar() {
    return cuadrosCrudos.size() < limiteAlmacenamiento;
  }

  boolean puedeRealizarProcesoDeCompresion(int p) {
    return hayCuadrosDisponibles(p) && puedeRealizarTareaDeEncoding();
  }

  boolean hayCuadrosDisponibles(int p) {
    return cuadrosCrudos.size() >= p;
  }

  boolean puedeRealizarTareaDeEncoding() {
    return cantTareasEncoding < limiteEncoding;
  }

}

//Simulacro Ejercicio 3.a
monitor Telescopio {

  int cantObservadores = 0;
  boolean calibracionEnCurso = false;

  void iniciarObservacion() {
    while (!puedeObservar()) wait();
    cantObservadores++;
  }

  void finalizarObservacion() {
    cantObservadores--;
    if (cantObservadores == 0) notify();
  }

  void iniciarCalibracion() {
    while (!puedeCalibrar()) wait();
    calibracionEnCurso = true;
  }

  void finalizarCalibracion() {
    calibracionEnCurso = false;
    notifyAll();
  }

  boolean puedeObservar() {
    return !calibracionEnCurso;
  }

  boolean puedeCalibrar() {
    return !calibracionEnCurso && cantObservadores == 0;
  }

}

//Simulacro Ejercicio 3.b
monitor Telescopio {

  int cantObservadores = 0;
  boolean calibracionEnCurso = false;
  int direccionActual;

  void iniciarObservacion(int posicion) {
    //PRECOND: Las posiciones posibles son 1, 2, 3 y 4.
    while (!puedeObservar(posicion)) wait();
    cantObservadores++;
    if (cantObservadores == 1) direccionActual = posicion;
  }

  void finalizarObservacion() {
    cantObservadores--;
    if (cantObservadores == 0) notifyAll();
  }

  void iniciarCalibracion() {
    while (!puedeCalibrar()) wait();
    calibracionEnCurso = true;
  }

  void finalizarCalibracion() {
    calibracionEnCurso = false;
    notifyAll();
  }

  boolean puedeObservar(int posicion) {
    return !calibracionEnCurso && sinUtilizarseODireccionEn(posicion);
  }

  boolean puedeCalibrar() {
    return !calibracionEnCurso && cantObservadores == 0;
  }

  boolean sinUtilizarseODireccionEn(int posicion) {
    return cantObservadores == 0 || direccionActual == posicion;
  }

}

//Simulacro Ejercicio 3.c
monitor Telescopio {

  int cantObservadores = 0;
  boolean calibracionEnCurso = false;
  int direccionActual;
  int pedidosDeCalibracion = 0;

  void iniciarObservacion(int posicion) {
    while (!puedeObservar(posicion)) wait();
    cantObservadores++;
    if (cantObservadores == 1) direccionActual = posicion;
  }

  void finalizarObservacion() {
    cantObservadores--;
    if (cantObservadores == 0) notifyAll();
  }

  void iniciarCalibracion() {
    pedidosDeCalibracion++;
    while (!puedeCalibrar()) wait();
    pedidosDeCalibracion--;
    calibracionEnCurso = true;
  }

  void finalizarCalibracion() {
    calibracionEnCurso = false;
    notifyAll();
  }

  boolean puedeObservar(int posicion) {
    return !calibracionEnCurso && sinUtilizarseODireccionEn(posicion) && sinPedidosODireccionEn(posicion);
  }

  boolean puedeCalibrar() {
    return !calibracionEnCurso && cantObservadores == 0;
  }

  boolean sinUtilizarseODireccionEn(int posicion) {
    return cantObservadores == 0 || direccionActual == posicion;
  }

  boolean sinPedidosODireccionEn(int posicion) {
    return pedidosDeCalibracion == 0 || direccionActual == posicion;
  }

}
