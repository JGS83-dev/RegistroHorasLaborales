import express from 'express';
import cors from 'cors';
import MailerRouter from './src/router/MailerRouter.js';

const port = 8888
const app = express()

app.use(cors());
app.use(express.json());

//routes
app.use('/api/mailer', MailerRouter);

app.listen(port, () => {
  console.log(`listening on port ${port}`);
});