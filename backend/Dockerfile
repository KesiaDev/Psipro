# Backend PsiPro - usa Debian (não Alpine) para compatibilidade com Prisma
FROM node:20-slim AS builder

WORKDIR /app

# Instalar dependências
COPY package.json package-lock.json* ./
RUN npm ci

# Prisma + build
COPY . .
RUN npx prisma generate
RUN npm run build

# Produção
FROM node:20-slim

WORKDIR /app

ENV NODE_ENV=production

# OpenSSL para Prisma
RUN apt-get update && apt-get install -y --no-install-recommends openssl ca-certificates && rm -rf /var/lib/apt/lists/*

# Copiar apenas o necessário
COPY --from=builder /app/package.json ./
COPY --from=builder /app/package-lock.json* ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/dist ./dist
COPY --from=builder /app/prisma ./prisma

EXPOSE 8080

CMD ["sh", "-c", "npx prisma migrate deploy && node dist/src/main"]
