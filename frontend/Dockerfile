# ---------- 1o stadio: build tou React ----------
FROM node:20-alpine AS build
WORKDIR /build

COPY package*.json ./
RUN npm install

COPY . .

# VITE_API_BASE_URL="/" -> to frontend kalei to backend sto IDIO domain (p.x. /api/faults),
# kai to nginx proothei ta /api sto backend container.
#
# Giati oxi "http://localhost:8080": to build trexei sto container, alla o kodikas
# trexei ston BROWSER tou xristi. Ekei to "localhost" einai to diko tou mihanima,
# oxi to container - kai an kapoios anevaze tin efarmogi se server, den tha douleve.
ENV VITE_API_BASE_URL=/
RUN npm run build

# ---------- 2o stadio: serving me nginx ----------
FROM nginx:1.27-alpine

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist /usr/share/nginx/html

EXPOSE 80
