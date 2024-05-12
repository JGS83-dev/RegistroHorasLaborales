import {sendemail} from '../../mailer.js';
import 'dotenv/config'

export const EnviarCorreo = async (req, res, next) => {
    let response = {};
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
        let message = await sendemail(sendTo, req.body.subject,htmlbody);
        response.message = "Correo enviado con éxito";
        res.status(200).json(response);
    } catch (error) {
      console.log("Se produjo una excepcion al procesar la peticion:", error);
      response.message = "Ocurrió un error al procesar la petición";
      res.status(400).json(response);
    }
  };