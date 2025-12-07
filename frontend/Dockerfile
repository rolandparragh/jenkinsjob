FROM node:22-alpine AS build
WORKDIR /app

COPY package*.json ./
COPY angular.json tsconfig*.json proxy.conf.json ./
COPY src ./src
COPY public ./public

RUN npm ci && npm run build

FROM nginx:1.27-alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /app/dist/frontend /usr/share/nginx/html

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
