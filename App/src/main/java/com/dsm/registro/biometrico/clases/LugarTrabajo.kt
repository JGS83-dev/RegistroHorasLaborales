package com.dsm.registro.biometrico.clases

class LugarTrabajo {
    var id: String = ""
    var nombre: String = ""
    var lugar: String = ""
    var horaInicio: String = ""
    var horaInicioReal: String = ""
    var horaFin: String = ""
    var horaFinReal: String = ""
    var estado: String = ""

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
        this.id = id
        this.nombre = nombre
        this.lugar = lugar
        this.horaInicio = horaInicio
        this.horaInicioReal = horaInicioReal
        this.horaFin = horaFin
        this.horaFinReal = horaFinReal
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
        this.id = id
        this.nombre = nombre
        this.lugar = lugar
        this.horaInicio = horaInicio
        this.horaFin = horaFin
        this.estado = estado
    }
}