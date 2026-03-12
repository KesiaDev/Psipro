# Backend PsiPro - evita Nixpacks (que falha no Railway)
FROM node:20-alpine AS builder

WORKDIR /app

# Instalar dependências
COPY package.json package-lock.json* ./
RUN npm ci

# Prisma + build
COPY . .
RUN npx prisma generate
RUN npm run build

# Produção
FROM node:20-alpine

WORKDIR /app

ENV NODE_ENV=production

# Copiar apenas o necessário
COPY --from=builder /app/package.json ./
COPY --from=builder /app/package-lock.json* ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma

EXPOSE 8080

CMD ["sh", "-c", "npx prisma migrate deploy && node dist/src/main"]
