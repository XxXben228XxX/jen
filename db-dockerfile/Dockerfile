FROM mcr.microsoft.com/mssql/server:2022-latest

# Перемикаємося на користувача root для встановлення пакетів
USER root

# Встановлення необхідних залежностей
RUN apt-get update && apt-get install -y --no-install-recommends \
    curl \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Додавання репозиторію Microsoft SQL Server Tools
RUN curl https://packages.microsoft.com/keys/microsoft.asc | apt-key add - && \
    curl https://packages.microsoft.com/config/ubuntu/22.04/prod.list | tee /etc/apt/sources.list.d/mssql-release.list

# Оновлення та встановлення mssql-tools
RUN apt-get update && \
    ACCEPT_EULA=Y apt-get install -y mssql-tools unixodbc-dev && \
    rm -rf /var/lib/apt/lists/*

# Додаємо шлях до mssql-tools у змінну PATH, щоб sqlcmd був доступний
ENV PATH="/opt/mssql-tools/bin:${PATH}"

# Після цього контейнер використовуватиме стандартний ENTRYPOINT образу mssql/server.
# Немає необхідності в USER mssql, оскільки базовий образ сам керує цим після свого ENTRYPOINT.