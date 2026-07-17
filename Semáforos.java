//Productor consumidor: split mutex
global Object buffer;
global Semaphore vacio = new Semaphore(1);
global Semaphore lleno = new Semaphore(0);

thread Productor :
  while(true) {
    vacio.acquire();
    buffer = producir();
    lleno.release();
  }

thread Consumidor :
  while (true) {
    lleno.acquire();
    consumir(buffer);
    vacio.release();
  }

//Productor consumidor: multiples productores y consumidores
global Object[] buffer = new Object[N];
global Semaphore hayvacio = new Semaphore(N, true);
global Semaphore haylleno = new Semaphore(0, true);
global int inicio = 0;
global int fin = 0;
global Semaphore mutexP = new Semaphore(1, true);
global Semaphore mutexC = new Semaphore(1, true);

thread Productor() {
  Object temp;
  while (true) {
    temp = producir();
    hayvacio.acquire();
    mutexP.acquire();
    buffer[inicio] = temp;
    inicio = (inicio+1) % N;
    mutexP.release();
    haylleno.release();
  }
}

thread Consumidor() {
  Object temp;
  while (true) {
    haylleno.acquire();
    mutexC.acquire();
    temp = buffer[fin];
    buffer[fin]=null;
    fin = (fin+1) % N;
    mutexC.release();
    hayvacio.release();
    consumir(temp);
  }
}

//Lectores-Escritores: Prioridad lectores
global Semaphore permisoE = new Semaphore(1);
global Semaphore mutexL = new Semaphore(1, true);
global Semaphore mutexP = new Semaphore(1, true);
global int lectores = 0;

thread Escritor() {
  mutexP.acquire();
  permisoE.acquire();
  escribir();
  permisoE.release();
  mutexP.release();
}

thread Lector() {
  mutexL.acquire();
  lectores++;
  if (lectores == 1)
    permisoE.acquire();
  mutexL.release();

  leer();

  mutexL.acquire();
  lectores--;
  if (lectores == 0)
    permisoE.release();
  mutexL.release();
}

//Lectores-Escritores: Prioridad escritores
global Semaphore permisoE = new Semaphore(1);
global Semaphore permisoL = new Semaphore(1);
global Semaphore mutexE = new Semaphore(1, true);
global Semaphore mutexL = new Semaphore(1, true);
global Semaphore mutexP = new Semaphore(1, true);
global int lectores = 0;
global int escritores = 0;

thread Escritor() {
  mutexE.acquire();
  escritores++;
  if (escritores == 1)
    permisoL.acquire();
  mutexE.release();

  permisoE.acquire();
  escribir();
  permisoE.release();

  mutexE.acquire();
  escritores--;
  if (escritores == 0)
    permisoL.release();
  mutexE.release();
}

thread Lector() {
  mutexP.acquire();
  permisoL.acquire();
  mutexL.acquire();
  lectores++;
  if (lectores == 1)
    permisoE.acquire();
  mutexL.release();
  permisoL.release();
  mutexP.release();

  leer();

  mutexL.acquire();
  lectores--;
  if (lectores == 0)
    permisoE.release();
  mutexL.release();
}

//Ejercicio 1.A Productor consumidor, Split mutex 
global int impar = N;
global int suma = 0;
global Semaphore permisoAcumular = new Semaphore(1);
global Semaphore permisoGenerar = new Semaphore(0);

thread Generador() {
  while (impar > 1) {
    permisoGenerar.acquire();
    impar = impar - 1; //genera el i-esimo numero impar
    permisoAcumular.release();
  }
  permisoGenerar.acquire();
  print(suma);
}

thread Acumulador() {
  while (true) {
    permisoAcumular.acquire();
    i = 2 * impar - 1;
    suma = suma + i;
    permisoGenerar.release();
  }
}

//Ejercicio 2.A
global Semaphore puedeAbordar = new Semaphore(0,true);
global Semaphore puedeNavegar = new Semaphore(0);
global Semaphore puedeBajar = new Semaphore(0);
global Semaphore puedeVolver = new Semaphore(0);

thread Transbordador() {
  while (true) {
    puedeAbordar.release();
    puedeNavegar.acquire();
    //navegarHacia(1)
    puedeBajar.release();
    puedeVolver.acquire();
    //navegarHacia(0)
  }
}

thread Persona() {
  puedeAbordar.acquire();
  //subir()
  puedeNavegar.release();
  puedeBajar.acquire();
  //bajar()
  puedeVolver.release();
}

//Ejercicio 2.B.I
global Semaphore puedeAbordar[] = new Semaphore[2]{0,0}; //FUERTES
global Semaphore puedeNavegar = new Semaphore(0);
global Semaphore puedeBajar = new Semaphore(0);
global Semaphore puedeVolver = new Semaphore(0);

thread Transbordador() {
  int costa = 0;
  while (true) {
    puedeAbordar[costa].release(N);
    puedeNavegar.acquire(N);
    //Espero que los N pasajeros se sienten.
    //Nota: Si usara el mismo repeat, estaria impidiendo que se sienten concurrentemente.
    costa = (costa + 1) % 2; //Cambio de costa
    //navegarHacia(costa)
    puedeBajar.release(N);
    puedeVolver.acquire(N);
    //Espera que los N pasajeros se hayan bajado
  }
}

thread Persona(costa) {
  puedeAbordar[costa].acquire();
  //subir()
  puedeNavegar.release();
  puedeBajar.acquire(); //Espera el cambio de costa
  //bajar()
  puedeVolver.release();
}

//Ejercicio 2.B.II
global Semaphore puedeAbordar[] = new Semaphore[2]{0,0}; //FUERTES
global Semaphore puedeNavegar = new Semaphore(0);
global Semaphore puedeBajar[] = new Semaphore[2]{0,0};
global Semaphore puedeSubir = new Semaphore(N);

thread Transbordador() {
  int costa = 0;
  while (true) {
    puedeAbordar[costa].release(N);
    puedeNavegar.acquire(N);
    costa = (costa + 1) % 2;
    //navegarHacia(costa)
    puedeBajar[costa].release(N);
  }
}

thread Persona(costa) {
  puedeAbordar[costa].acquire();
  puedeSubir.acquire();
  //subir()
  puedeNavegar.release();
  int costaDestino = (costa + 1) % 2;
  puedeBajar[costaDestino].acquire();
  //bajar()
  puedeSubir.release();
}

//Ejercicio 3.B
global Semaphore aparatos[] = new Semaphore[4]{1..1}; //FUERTES
global Semaphore discos = new Semaphore(20);
global Semaphore puedeCargar = new Semaphore(1,true);

thread Cliente(rutina) {
  int nroAparato;
  int cantDiscos;
  for (rutinaActual in rutina) {
    nroAparato = rutinaActual.fst();
    cantDiscos = rutinaActual.snd();
    aparatos[nroAparato].acquire();
    puedeCargar.acquire();
    discos.acquire(cantDiscos);
    puedeCargar.release();
    //trabajarGrupoMuscular();
    aparatos[nroAparato].release();
    discos.release(cantDiscos);
  }
}

//Ejercicio 4
global Semaphore puedePublicarId = new Semaphore(1,true);
global Semaphore puedeTomarId = new Semaphore(0,true);
global Semaphore puedeOperar[] = new Semaphore[K]{0..0};
global Semaphore tintoreriaTerminada[] = new Semaphore[K]{0..0};
global Semaphore ropaRetirada[] = new Semaphore[K]{0..0};
global int idMaquinaAUsar;

thread Maquina(idMaquina) {
  while (true) {
    puedePublicarId.acquire();
    idMaquinaAUsar = idMaquina;
    puedeTomarId.release();
    puedeOperar[idMaquina].acquire();
    //hacerTintoreriaSobreRopa()
    tintoreriaTerminada[idMaquina].release();
    ropaRetirada[idMaquina].acquire();
  }
}

thread Persona() {
  puedeTomarId.acquire();
  int miMaquina = idMaquinaAUsar;
  puedePublicarId.release();
  //cargarMaquina(miMaquina)
  puedeOperar[miMaquina].release();
  //hacerCompras()
  tintoreriaTerminada[miMaquina].acquire();
  //retirarRopa()
  ropaRetirada[miMaquina].release();
}

//Ejercicio 5
global Semaphore puedeAbordar[] = new Semaphore[K]{0..0}; //FUERTES
global Semaphore puedeNavegar[] = new Semaphore[K]{0..0};
global Semaphore puedeBajar[] = new Semaphore[K]{0..0};
global Semaphore puedeVolver[] = new Semaphore[K]{0..0};
global Semaphore puedeAmarrar[] = new Semaphore[2]{1,1}; //FUERTES
global int idBarco[] = new int[2];
global Semaphore hayEspacio[] = new Semaphore[2]{N,N};
global Semaphore hayBarco[] = new Semaphore[2]{0,0}; //FUERTES

thread Transbordador(id) {
  int costa = 0;
  while (true) {
    puedeAmarrar[costa].acquire();
    hayEspacio[costa].acquire(N);
    idBarco[costa] = id;
    hayBarco[costa].release(N);
    puedeAmarrar[costa].release();
    puedeAbordar[id].release(N);
    puedeNavegar[id].acquire(N);
    costa = (costa + 1) % 2;
    //navegarHacia(costa)
    puedeBajar[id].release(N);
    puedeVolver[id].acquire(N);
  }
}

thread Persona(costa) {
  hayBarco[costa].acquire();
  int id = idBarco[costa];
  hayEspacio[costa].release();
  puedeAbordar[id].acquire();
  //subir()
  puedeNavegar[id].release();
  puedeBajar[id].acquire();
  //bajar()
  puedeVolver[id].release();
}

//Ejercicio 6.A
global Semaphore puedeEntrarBJ = new Semaphore(1,true);
global Semaphore puedeEntrarRP = new Semaphore(1,true);

thread HinchaBJ() {
  puedeEntrarBJ.acquire();
  //entrar()
  puedeEntrarRP.release();
}

thread HinchaRP() {
  puedeEntrarRP.acquire();
  //entrar()
  puedeEntrarBJ.release();
}

//Ejercicio 6.B
global Semaphore permisoBJ = new Semaphore(1,true);
global Semaphore permisoRP = new Semaphore(1,true);
global Semaphore mutex = new Semaphore(1);
global int capacidad = N;

thread HinchaBJ() {
  permisoBJ.acquire();
  mutex.acquire();
  bool pudeEntrar = capacidad > 0;
  bool ultimo = capacidad==1;
  capacidad--;
  mutex.release();
  if (pudeEntrar) {
    permisoRP.release();
    if (ultimo) {
      permisoBJ.release();
    }
  } else {
    permisoBJ.release();
    //retirarse()
  }
}

thread HinchaRP() {
  permisoRP.acquire();
  mutex.acquire();
  bool pudeEntrar = capacidad > 0;
  bool ultimo = capacidad==1;
  capacidad--;
  mutex.release();
  if (pudeEntrar) {
    permisoBJ.release();
    if (ultimo) {
      permisoRP.release();
    }
  } else {
    permisoRP.release();
    //retirarse()
  }
}

//Ejercicio 6.C
global Semaphore puedeRetirar = new Semaphore(1,true); 
global Semaphore puedeEntrar = new Semaphore(0);
global Semaphore puedeCerrar = new Semaphore(0);
global int cantEntradas = M;
global int cantEntradasRetiradas = 0;
global bool hayMostrador = true;

thread Persona() {
  bool pudeEntrar = false;
  puedeRetirar.acquire();
  if (hayMostrador && cantEntradas > 0) {
    pudeEntrar = cantEntradas > 0;
    cantEntradas--;
    cantEntradasRetiradas++;
  }
  puedeRetirar.release();
  if (pudeEntrar) {
    puedeEntrar.acquire();
    //acomodarEnButaca()
    puedeCerrar.release();
  } else {
    //retirarDelCine()
  }
}

thread PersonalCine() {
  puedeRetirar.acquire();
  hayMostrador = false;
  puedeRetirar.release();
  puedeEntrar.release(cantEntradasRetiradas);
  puedeCerrar.acquire(cantEntradasRetiradas);
  //comenzarProyeccion()
}

//Ejercicio 8.A
global Semaphore toileteDisponible = new Semaphore(8,true);
global Semaphore permisoLimpiar = new Semaphore(1);
global Semaphore mutexPersona = new Semaphore(1,true);
global int cantPersonas = 0;

thread Persona() {
  mutexPersona.acquire();
  cantPersonas++;
  if (cantPersonas==1) {
    permisoLimpiar.acquire();
  }
  mutexPersona.release();
  //Ya esta dentro del baño

  toileteDisponible.acquire();
  //usarElBaño()
  toileteDisponible.release();

  mutexPersona.acquire();
  cantPersonas--;
  if (cantPersonas==0) {
    permisoLimpiar.release();
  }
  mutexPersona.release();
}

thread PersonalLimpieza() {
  while (true) {
    permisoLimpiar.acquire();
    //limpiar()
    permisoLimpiar.release();
  }
}

//Ejercicio 8.B
global Semaphore toileteDisponible = new Semaphore(8,true);
global Semaphore permisoLimpiar = new Semaphore(1);
global Semaphore permisoUso = new Semaphore(1);
global Semaphore mutexPersona = new Semaphore(1,true);
global semaphore mutexP = new Semaphore(1,true);
global int cantPersonas = 0;

thread Persona() {
  mutexP.acquire();
  permisoUso.acquire();
  mutexPersona.acquire();
  cantPersonas++;
  if (cantPersonas==1) {
    permisoLimpiar.acquire();
  }
  mutexPersona.release();
  permisoUso.release();
  mutexP.release();

  toileteDisponible.acquire();
  //usarElBaño()
  toileteDisponible.release();

  mutexPersona.acquire();
  cantPersonas--;
  if (cantPersonas==0) {
    permisoLimpiar.release();
  }
  mutexPersona.release();
}

thread PersonalLimpieza() {
  while (true) {
    permisoUso.acquire();
    permisoLimpiar.acquire();
    //limpiar()
    permisoLimpiar.release();
    permisoUso.release();
  }
}

//Ejercicio 9.B
global Semaphore puedeCargar = new Semaphore(6,true);
global Semaphore permisoAbastecer = new Semaphore(1);
global Semaphore permisoCargar = new Semaphore(1);
global Semaphore mutexP = new Semaphore(1,true);
global Semaphore mutexVehiculo = new Semaphore(1,true);
global int cantVehiculos = 0;

thread Vehiculo() {
  puedeCargar.acquire();
  mutexP.acquire();
  permisoCargar.acquire();
  mutexVehiculo.acquire();
  cantVehiculos++;
  if (cantVehiculos==1) {
    permisoAbastecer.acquire();
  }
  mutexVehiculo.release();
  permisoCargar.release();
  mutexP.release();

  //cargarCombustible()

  mutexVehiculo.acquire();
  cantVehiculos--;
  if (cantVehiculos==0) {
    permisoAbastecer.release();
  }
  mutexVehiculo.release();
  puedeCargar.release();
}

thread CamionCombustible() {
  permisoCargar.acquire();
  permisoAbastecer.acquire();
  //descargarCombustible()
  permisoAbastecer.release();
  permisoCargar.release();
}

//Ejercicio 10.A Lectores-Lectores
global Semaphore permisoPuente = new Semaphore(1);
global Semaphore mutexAuto[] = new Semaphore[2]{1,1}; //FUERTES
global int cantAutos[] = {0,0};

thread Auto(ciudad) {
  mutexAuto[ciudad].acquire();
  cantAutos[ciudad]++; //cantAutos[ciudad] = cantAutos[ciudad] + 1
  if (cantAutos[ciudad]==1) {
    permisoPuente[ciudad].acquire();
  }
  mutexAuto[ciudad].release();
  //Se roba el permiso y se forma en la cola de autos

  //cruzarPuente()

  mutexAuto[ciudad].acquire();
  cantAutos[ciudad]--;
  if (cantAutos[ciudad]==0) {
    permisoPuente[ciudad].release();
  }
  mutexAuto[ciudad].release();
}

//Ejercicio 10.B
global Semaphore permisoPuente = new Semaphore(1);
global Semaphore mutexAutos[] = new Semaphore[2]{1,1}; //FUERTES
global Semaphore permisoCruzar = new Semaphore(3,true);
global int cantAutos[] = {0,0};

thread Auto(ciudad) {
  mutexAutos[ciudad].acquire();
  cantAutos[ciudad]++;
  if (cantAutos[ciudad]==1) {
    permisoPuente[ciudad].acquire();
  }
  mutexAutos[ciudad].release();

  permisoCruzar.acquire();
  //cruzarPuente()
  permisoCruzar.release();

  mutexAutos[ciudad].acquire();
  cantAutos[ciudad]--;
  if (cantAutos[ciudad]==0) {
    permisoPuente[ciudad].release();
  }
  mutexAutos[ciudad].release();
}

//Simulacro Ejercicio 2.A
global Semaphore mutexMaquina[] = new Semaphore[6]{1..1}; //solo el primero es fuerte
global Semaphore permisoAuto[] = new Semaphore[5]{0..0};
global Semaphore maquinaLista[] = new Semaphore[5]{0..0};

thread Maquina(id) {
  while (true) {
    permisoAuto[id].acquire();
    //lavar(id)
    maquinaLista[id].release();
  }
}

thread Auto() {
  mutexMaquina[0].acquire();
  //mover a 0
  for i in range [0,4] {
    permisoAuto[i].release();
    maquinaLista[i].acquire();
    mutexMaquina[i+1].acquire();
    //mover a i+1
    mutexMaquina[i].release();
  }
  mutexMaquina[5].release();
}

//Simulacro Ejercicio 2.B
global Semaphore mutexMaquina[] = new Semaphore[7]{1..1}; //solo el primero es fuerte
global Semaphore permisoAuto[] = new Semaphore[7]{0..0};
global Semaphore maquinaLista[] = new Semaphore[7]{0..0};
global Semaphore permisoSubir[] = new Semaphore[6]{0..0};
global Semaphore permisoBajar[] = new Semaphore[6]{0..0};
global Semaphore permisoDesplazar[] = new Semaphore[6]{0..0};
global Semaphore vacio = new Semaphore(1,true); 
global Semaphore lleno = new Semaphore(1);
global int idRobot;

thread Maquina(int id) {
//PRECOND: solo estaciones de 1 a 5.
  while (true) {
    permisoAuto[id].acquire();
    //lavar(id)
    maquinaLista[id].release();
  }
}

thread Auto() {
  int id;
  mutexMaquina[0].acquire();
  //mover a 0
  lleno.acquire();
  id = idRobot;
  vacio.release();
  permisoSubir[id].release();
  permisoDesplazar[id].acquire();
  mutexMaquina[1].acquire();
  //mover a 1
  mutexMaquina[0].release();
  for i in range [1,5] {
    permisoAuto[i].release();
    maquinaLista[i].acquire();
    mutexMaquina[i+1].acquire();
    //mover a i+1
    mutexMaquina[i].release();
  }
  permisoBajar[id].release();
  permisoDesplazar[id].acquire();
  mutexMaquina[6].release();
  //retirarse
}

thread Robot(int id) {
  while (true) {
    vacio.acquire();
    idRobot = id;
    lleno.release();
    permisoSubir[id].acquire();
    //subir
    permisoDesplazar[id].release();
    //limpiar
    permisoBajar[id].acquire();
    //bajar
    permisoDesplazar[id].release();
  }
}

//Simulacro Ejercicio 4.A
global Semaphore puedeApostar = new Semaphore(0);
global Semaphore hayResultado = new Semaphore(0);
global Semaphore siguienteRonda = new Semaphore(0);
global bool hayGente = true;
global int numeroQueSalio;
global Semaphore mesa = new Semaphore(1);

thread Apostador(int montoAApostar) {
  int numeroApostado;
  while (montoAApostar > 0) {
    puedeApostar.acquire();
    mesa.acquire();
    //Apuesta
    numeroApostado = elegirNumero();
    montoAApostar--;
    mesa.release();
    hayResultado.acquire();
    if (numeroApostado == numeroQueSalio) {
      print(“Gané!”);
      montoAApostar += 36;
    } else {
      print(“Perdí!”);
    }
    siguienteRonda.release();
  }
  print(“No, mi casa!”);
  //Se va
}

thread Crupier() {
  while (hayGente) {
    puedeApostar.release();
    //Espero el tiempo que quiero
    print(“No va más”);
    mesa.acquire();
    //girar la ruleta
    numeroQueSalio = girarRuleta();
    hayResultado.release();
    mesa.release();
    siguienteRonda.acquire();
  }
  print(“Fin.”);
}

//Simulacro Ejercicio 4.B
global Semaphore hayResultado = new Semaphore(0);
global Semaphore siguienteRonda = new Semaphore(0);
global Semaphore permisoMesa = new Semaphore(1);
global Semaphore mutexApostador = new Semaphore(1,true);
global Semaphore mutex = new Semaphore(1,true);
global int numeroQueSalio;
global int cantApostadores = 0;
global int cantApostando = 0;

thread Apostador(int montoAApostar) {
  int numeroApostado;
  while (montoAApostar > 0) {
    mutexApostador.acquire();
    cantApostadores++;
    if (cantApostadores==1) {
      permisoMesa.acquire();
    }
    mutexApostador.release();

    numeroApostado = elegirNumero();
    montoAApostar--;
    mutex.acquire();
    cantApostando++;
    mutex.release();

    mutexApostador.acquire();
    cantApostadores--;
    if (cantApostadores==0) {
      permisoMesa.release();
    }
    mutexApostador.release();
    hayResultado.acquire();
    if (numeroApostado == numeroQueSalio) {
      print(“Gané!”);
      montoAApostar += 36;
    } else {
      print(“Perdí!”);
    }
    siguienteRonda.release();
  }
  print(“No, mi casa!”);
}

thread Crupier() {
  while (true) {
    print(“No va más”);
    permisoMesa.acquire();
    numeroQueSalio = girarRuleta();
    hayResultado.release(cantApostando);
    siguienteRonda.acquire(cantApostando);
    mutex.acquire();
    cantApostando = 0;
    mutex.release();
    permisoMesa.release();
  }
}

//Simulacro Ejercicio 4.C
global Semaphore hayResultado = new Semaphore(0);
global Semaphore siguienteRonda = new Semaphore(0);
global Semaphore permisoMesa = new Semaphore(1);
global Semaphore mutexApostador = new Semaphore(1);
global Semaphore mutex = new Semaphore(1,true);
global Semaphore permisoApostar = new Semaphore(1);
global Semaphore mutexP = new Semaphore(1,true);
global int numeroQueSalio;
global int cantApostadores = 0;
global int cantApostando = 0;

thread Apostador(int montoAApostar) {
  int numeroApostado;
  while (montoAApostar > 0) {
    mutexP.acquire();
    permisoApostar.acquire();
    mutexApostador.acquire();
    cantApostadores++;
    if (cantApostadores==1) {
      permisoMesa.acquire();
    }
    mutexApostador.release();
    permisoApostar.release();
    mutexP.release();

    numeroApostado = elegirNumero();
    montoAApostar--;
    mutex.acquire();
    cantApostando++;
    mutex.release();

    mutexApostador.acquire();
    cantApostadores--;
    if (cantApostadores==0) {
      permisoMesa.release();
    }
    mutexApostador.release();
    hayResultado.acquire();
    if (numeroApostado == numeroQueSalio) {
      print(“Gané!”);
      montoAApostar += 36;
    } else {
      print(“Perdí!”);
    }
    siguienteRonda.release();
  }
  print(“No, mi casa!”);
}

thread Crupier() {
  while (true) {
    permisoApostar.acquire();
    print(“No va más”);
    permisoMesa.acquire();
    numeroQueSalio = girarRuleta();
    hayResultado.release(cantApostando);
    siguienteRonda.acquire(cantApostando);
    mutex.acquire();
    cantApostando = 0;
    mutex.release();
    permisoMesa.release();
    permisoApostar.release();
  }
}
