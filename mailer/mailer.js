import nodemailer from 'nodemailer'
import { MailerSend, EmailParams, Sender, Recipient } from "mailersend";

const mailerSend = new MailerSend({
  apiKey: "mlsn.ea5fc0bfbaf7f6a15d8b0953f5a207f2ad0b06e25ac65be09dff8b34f5b46c63",
});

const sentFrom = new Sender("registro-app@trial-ynrw7gynmpj42k8e.mlsender.net", "Registro App");

export const sendemail = async (sendto, subject, htmlbody) => {
    // let emailEnv, passEnv;

    const recipients = [
        new Recipient(sendto, "Recipiente")
      ];

    // emailEnv = "";
    // passEnv = "";

    // //Configuracion para envio con Office 365
    // //mas info: https://nodemailer.com/smtp/
    // let transporter = nodemailer.createTransport({
    //     host: "smtp25.elasticemail.com",
    //     port: 587,
    //     secure: false,
    //     auth: {
    //         user: emailEnv,
    //         pass: passEnv
    //     },
    //     tls: {
    //         rejectUnauthorized: false
    //     }
    // })

    // //Configuracion del correo a enviar
    // let configMail = {
    //     from: `${emailEnv} <${emailEnv}>`,
    //     to: sendto,
    //     subject: subject,
    //     html: htmlbody
    // };

    // //Envio del correo configurado
    // await transporter.sendMail(configMail, (err, info) => {
    //     if (err) {
    //         console.error(err.message)
    //     }
    //     console.log("Correo enviado:",info)
    //     return 'Correo enviado con éxito';
    // })

    const emailParams = new EmailParams()
        .setFrom(sentFrom)
        .setTo(recipients)
        .setReplyTo(sentFrom)
        .setSubject(subject)
        .setHtml(htmlbody)
        .setText("");

    await mailerSend.email.send(emailParams);
};
