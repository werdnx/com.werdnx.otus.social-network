FROM postgres:15

# Переменные окружения
#ENV POSTGRES_DB=socialdb
#ENV POSTGRES_USER=socialuser
#ENV POSTGRES_PASSWORD=socialpass


# COPY ./docker/initdb/*.sql /docker-entrypoint-initdb.d/
RUN apt-get update \
 && apt-get install -y --no-install-recommends unzip \
 && rm -rf /var/lib/apt/lists/*
# Открываем порт
EXPOSE 5432

