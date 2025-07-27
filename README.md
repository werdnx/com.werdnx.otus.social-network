# Social Network Application

## Prerequisites

- Docker & Docker Compose
- Java 18 (для локальной сборки)
- Maven 3.8+
- (опционально) Docker-образ Prometheus и Grafana подтягиваются автоматически через `docker-compose.yml`

## Local Run

1. **Клонируем репозиторий**
   ```bash
   git clone https://github.com/werdnx/com.werdnx.otus.social-network.git
   cd com.werdnx.otus-social-network
   ```

2. **Каталоги и конфиги**  
   В проекте созданы и заполнены следующие директории/файлы (см. примеры в `docker-compose.yml`):
    - `postgres/master/{data,postgresql.conf,pg_hba.conf}`
    - `postgres/slave1/{data,recovery.conf}`
    - `postgres/slave2/{data,recovery.conf}`
    - `monitoring/prometheus.yml`
    - `monitoring/grafana/provisioning/datasources/datasource.yml`
    - `monitoring/grafana/provisioning/dashboards/dashboard.yml`
    - `monitoring/grafana/dashboards/postgres-overview.json`

3. **Сборка и запуск всех сервисов**
   ```bash
   docker-compose up --build -d
   ```

4. **Проверка доступности**
    - **Приложение**: http://localhost:8080
    - **PostgreSQL Master**: localhost:5432
    - **PostgreSQL Slave 1**: localhost:5433
    - **PostgreSQL Slave 2**: localhost:5434
    - **Prometheus**: http://localhost:9090
    - **Grafana**: http://localhost:3000  (логин/пароль `admin`/`admin`)

5. **Default User**  
   Через Liquibase создаётся пользователь:
    - ID: `1`
    - First Name: `Admin`
    - Last Name: `User`
    - Password: `secret`

## Репликация

- В `docker-compose.yml` подняты 1 master и 2 slave с потоковой репликацией PostgreSQL.
- В Spring-конфигурации используется `ReplicationRoutingDataSource`, который при `@Transactional(readOnly = true)` направляет запрос на slave, иначе — на master.

## Authentication & API

Остальные разделы (`/login`, `/user/register`, `/user/get/{id}`) остаются без изменений — их см. в оригинале README.
