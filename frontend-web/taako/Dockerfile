# ---- Build stage --------------------------------------------------------
FROM node:20-alpine AS builder
WORKDIR /app

# Install dependencies
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund

# Copy source code and build
COPY . .
RUN npm run build

# ---- Production stage --------------------------------------------------------
FROM node:20-alpine
WORKDIR /app

# Copy necessary files from builder
COPY --from=builder /app/package.json ./
COPY --from=builder /app/package-lock.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public

# Install only production dependencies
RUN npm ci --only=production --no-audit --no-fund

# Expose port 3000
EXPOSE 3000

# Start production server
CMD ["npm", "start"]

