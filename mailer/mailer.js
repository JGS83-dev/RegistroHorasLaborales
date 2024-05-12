import nodemailer from 'nodemailer'

export const sendemail = async (sendto, subject, htmlbody) => {
    let emailEnv, passEnv;

    emailEnv = process.env.EMAIL;
    passEnv = process.env.PASSWORD;

    //Configuracion para envio con Office 365
    //mas info: https://nodemailer.com/smtp/
    let transporter = nodemailer.createTransport({
        host: "smtp.office365.com",
        port: 587,
        secure: false,
        auth: {
            user: emailEnv,
            pass: passEnv
        },
        tls: {
            rejectUnauthorized: false
        }
    })

    //Configuracion del correo a enviar
    let configMail = {
        from: `${emailEnv} <${emailEnv}>`,
        to: sendto,
        subject: subject,
        html: htmlbody
    };

    //Envio del correo configurado
    await transporter.sendMail(configMail, (err, info) => {
        if (err) {
            console.error(err.message)
        }
        console.log("Correo enviado:",info)
        return 'Correo enviado con éxito';
    })

};
