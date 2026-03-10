# Ativar API de Transcrição de Voz (Whisper)

Para a transcrição de áudio na sessão rápida funcionar 100%, configure a variável **OPENAI_API_KEY** no ambiente de produção.

---

## 1. Obter chave da OpenAI

1. Acesse [platform.openai.com](https://platform.openai.com)
2. Faça login ou crie uma conta
3. Vá em **API keys** → **Create new secret key**
4. Copie a chave (começa com `sk-...`)

---

## 2. Configurar no Railway

1. Acesse o [Railway Dashboard](https://railway.app/dashboard)
2. Selecione o projeto **psipro-backend-production**
3. Clique no serviço do backend
4. Vá em **Variables** (ou **Settings** → **Variables**)
5. Adicione:
   - **Nome:** `OPENAI_API_KEY`
   - **Valor:** `sk-...` (sua chave)
6. Clique em **Add** e aguarde o **redeploy** automático

---

## 3. Testar localmente (opcional)

Crie um arquivo `.env` na pasta `backend/`:

```
OPENAI_API_KEY=sk-sua-chave-aqui
```

Depois rode o backend:

```bash
cd backend
npm run start:dev
```

---

## 4. Verificar se está funcionando

Após o deploy:

1. Abra o app, faça login no backend
2. Entre em um paciente → Sessão rápida
3. Grave um áudio e pare a gravação
4. A transcrição deve aparecer nas anotações e os insights (resumo, temas, emoções) devem ser gerados

Se aparecer erro 401: faça login no backend (BackendLoginActivity).  
Se aparecer erro 500: verifique se `OPENAI_API_KEY` está configurada no Railway.

---

## Custos (OpenAI Whisper)

- Whisper: ~US$ 0,006 por minuto de áudio
- GPT-4o-mini (insights): ~US$ 0,15 por 1M tokens de entrada

Para uso clínico moderado, o custo costuma ficar baixo.
