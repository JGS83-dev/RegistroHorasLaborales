package com.dsm.registro.biometrico.clases

class LugarTrabajo {
    var uid: String = ""
    var dia: String = ""
    var nombre: String = ""
    var lugar: String = ""
    var entrada: String = ""
    var entrada_real: String = ""
    var salida: String = ""
    var salida_real: String = ""
    var estado: String = ""
    var latitud:String =""
    var longitud:String =""
    var imagen:String=""

    constructor(
        id: String,
        nombre: String,
        lugar: String,
        horaInicio: String,
        horaInicioReal: String,
        horaFin: String,
        horaFinReal: String,
        estado: String
    ) {
        this.uid = id
        this.nombre = nombre
        this.lugar = lugar
        this.entrada = horaInicio
        this.entrada_real = horaInicioReal
        this.salida = horaFin
        this.salida_real = horaFinReal
        this.estado = estado
    }

    constructor(
        id: String,
        nombre: String,
        lugar: String,
        horaInicio: String,
        horaFin: String,
        estado: String
    ) {
        this.uid = id
        this.nombre = nombre
        this.lugar = lugar
        this.entrada = horaInicio
        this.salida = horaFin
        this.estado = estado
    }

    constructor()
}