import express from 'express';
import { EnviarCorreo} from '../controller/MailerController.js';
const router = express.Router();

router.post('/', EnviarCorreo);

export default router;