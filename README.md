# bank-application

Aplicación bancaria compuesta por dos microservicios backend (`client`, `account`), una base de datos MySQL y un frontend Angular servido con Nginx.

## Estructura del proyecto

```
bank-application/
├── bank-app/           # Microservicios backend y base de datos
│   ├── account/        # Microservicio de cuentas y movimientos (puerto 8081)
│   ├── client/         # Microservicio de clientes (puerto 8080)
│   └── mysql-scripts/  # Script SQL de inicialización
├── bank-front/         # Frontend Angular (puerto 80)
└── docker-compose.yaml # Despliegue completo
```

## Prerequisitos

- [Docker](https://docs.docker.com/get-docker/) 24+
- [Docker Compose](https://docs.docker.com/compose/install/) v2+

## Variables de entorno

Crea un archivo `.env` en la raíz del proyecto con el siguiente contenido:

```env
DB_ROOT_PASSWORD=root
DB_USER=bankuser
DB_PASSWORD=bankpass
DB_NAME=bank-app
```

## Ejecución

### Construir e iniciar todos los servicios

```bash
docker compose up --build
```

### Iniciar servicios previamente construidos

```bash
docker compose up
```

### Detener todos los servicios

```bash
docker compose down
```

### Detener y eliminar volúmenes (borra los datos de la base de datos)

```bash
docker compose down -v
```

## Acceso a la aplicación

| Servicio         | URL                        |
|------------------|----------------------------|
| Frontend         | http://localhost            |
| API Clientes     | http://localhost:8080       |
| API Cuentas      | http://localhost:8081       |
| Base de datos    | localhost:3306              |

## Ejecución solo del backend

Para desplegar únicamente los servicios backend:

```bash
cd bank-app
docker compose up --build
```
