const express = require('express')
const mailer = require('./mailer.js')
const app = express()

const port = 8888
app.use(express.json());

app.listen(port, () => {
    console.log(`Servidor activo en puerto: ${port}`)
})

app.post('/enviar', (req, res) => {
    try {
        //Se captura el destinatario
        let sendTo = req.body.sendto;

        let htmlbody = 
        `<center>
            <h1>Notificacion de visita completada</h1>
            <hr>
            <h3>Se ha completado la visita del lugar con id: ${req.body.id}</h3>
            <p>
                <strong>Hora de Entrada: </strong> ${req.body.horaEntrada}
                <br>
                <strong>Hora de Entrada Real: </strong> ${req.body.horaEntradaReal}
                <hr>
                <strong>Hora de Salida: </strong> ${req.body.horaSalida}
                <br>
                <strong>Hora de Salida Real: </strong> ${req.body.horaSalidaReal}
            </p>
        </center>`;

        //Se manda a llamar el metodo para el envio del correo
        mailer.sendemail(sendTo, req.body.subject,htmlbody);

        //Si todo finaliza bien responde con 200
        res.status(200).send('Correo enviado con éxito');
    } catch (e) {

        //Imprimimos el error en consola
        console.log(e);

        //Si se produce una excepcion
        res.status(500).send('Ocurrio un error');
    }
});