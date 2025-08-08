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

3. **Сборка и запуск всех сервисов с 1 ЮД**
   ```bash
   docker-compose up --build -d
   ```
3.1 **Сборка и запуск всех сервисов с асинхронной репликацией(1 мастер, 2 слейва)**
   ```bash
   docker compose -f docker-compose-stream.yml up --build -d
   ```   
3.2 **Запуск с кворумной синхронной репликацией**
   ```bash
   docker-compose -f docker-compose-stream.yml -f docker-compose.sync.yml up -d
   ```
Между переключениями с асинхронной на синхронную репликаци вызывать:
   ```bash
docker-compose down -v
   ```
Для наполнения данными, несколько раз запустить скрипт(1 запуск 1млн записей), пароль otus_pass
   ```bash
docker-compose exec postgres-master psql -U otus -d social_network_rep -c "
CREATE TEMP TABLE tmp_people(
fullname    text,
birth_date  date,
city        text
);
COPY tmp_people
FROM PROGRAM 'unzip -p /data/people.v2.zip'
WITH (FORMAT csv, HEADER false);
INSERT INTO app_user(first_name, last_name, birth_date, city)
SELECT
split_part(fullname,' ',2) AS first_name,
split_part(fullname,' ',1) AS last_name,
birth_date,
city
FROM tmp_people;
"
   ```

3.3 **Проверка доступности**
    - **Приложение**: http://localhost:8080
    - **PostgreSQL Master**: localhost:5432
    - **PostgreSQL Slave 1**: localhost:5433
    - **PostgreSQL Slave 2**: localhost:5434
    - **Prometheus**: http://localhost:9090
    - **Grafana**: http://localhost:3000  (логин/пароль `admin`/`admin`)

4 **Default User**  
   Через Liquibase создаётся пользователь:
    - ID: `1`
    - First Name: `Admin`
    - Last Name: `User`
    - Password: `secret`

## Репликация

- В `docker-compose.yml` подняты 1 master и 2 slave с потоковой репликацией PostgreSQL.
- В `docker-compose.sync.yml` подняты 1 master и 2 slave с синхронной репликацией PostgreSQL.
- В Spring-конфигурации используется `ReplicationRoutingDataSource`, который при `@Transactional(readOnly = true)` направляет запрос на slave, иначе — на master.

## Шардирование
   ```bash
   docker-compose -f docker-compose-citus.yml up --build -d
   ```

Для обеспечения устойчивость к «эффекту Леди Гаги», шардируем по conversation_id.
Даже если один пользователь активно переписывается с множеством собеседников, 
каждый диалог будет иметь свой conversation_id и попадёт на разные шарды, что предотвращает концентрацию всех сообщений одного пользователя на одной ноде.
### Решардинг (Online Rebalancing)

Чтобы добавить воркер и перераспределить шарды без даунтайма:

#### Добавьте нового воркера в `docker-compose-citus.yml` (например, `worker3`):
   ```yaml

worker3:
  image: citusdata/citus:11.2
  environment:
    <<: *common-env
    COORDINATOR_HOST: coordinator
  volumes:
    - ./postgres/sharding/init-workers.sql:/docker-entrypoint-initdb.d/init-workers.sql
  ports:
    - "5435:5432"
  networks:
    - appnet
  command:
    - postgres
    - "-c"
    - "wal_level=logical"
    - "-c"
    - "max_wal_senders=32"
    - "-c"
    - "max_replication_slots=32"
    - "-c"
    - "shared_preload_libraries=citus"
  ```
#### Запустите нового воркера:
   ```bash
docker-compose -f docker-compose-citus.yml up -d worker3
   ```
#### Подключитесь к координатору и выполните:
   ```bash
psql -h localhost -p 5435 -U coord -d coord
SELECT master_add_node('worker3', 5432);
   ```
#### Запустите ребалансировку:
   ```bash
SELECT rebalance_table_shards('messages');
   ```
#### Проверить шарды:
   ```bash
SELECT s.shardid,
       p.nodename,
       p.nodeport,
       s.shardminvalue,         -- начало диапазона хеша
       s.shardmaxvalue          -- конец диапазона
FROM   pg_dist_shard            AS s
JOIN   pg_dist_shard_placement  AS p USING (shardid)
WHERE  s.logicalrelid = 'messages'::regclass
ORDER  BY shardid;
   ```

## Authentication

All protected endpoints require a JWT Bearer token. First, log in to receive a token:

```bash
POST /login?id=1&password=secret
```

Example Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUxMDU3NTY3LCJleHAiOjE3NTEwNTg0Njd9.kkdqsyB-pIRRGLDxhxM8mO4d-LaDgqUxdvOUu9qaYF4",
  "expiresIn": 900,
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxIiwiaWF0IjoxNzUxMDU3NTY3LCJleHAiOjE3NTE2NjIzNjd9.dnUfEgpIzlUr3dc9yNJzeOsXkN7-BMmsXP3RmferrHw",
  "tokenType": "Bearer"
}
```

## API Endpoints

- `POST /login?id={id}&password={password}` - User authentication
- `POST /user/register` - Register new user (JSON body)
- `GET /user/get/{id}` - Retrieve user profile by ID
- `POST /login?id={id}&password={password}`  
  Obtain an authentication token.

- `POST /user/register`  
  Register a new user.  
  **Headers**:
  ```
  Authorization: Bearer <accessToken>
  ```  
  **Body** (JSON):
  ```json
  {
    "firstName": "Jane",
    "lastName": "Doe",
    "birthDate": "1990-01-01",
    "gender": "Female",
    "interests": "Reading,Travel",
    "city": "Moscow",
    "passwordHash": "secure_password"
  }
  ```

- `GET /user/get/{id}`  
  Retrieve a user profile by ID.  
  **Headers**:
  ```
  Authorization: Bearer <accessToken>
  ```

## Postman Collection

See `postman_collection.json`
