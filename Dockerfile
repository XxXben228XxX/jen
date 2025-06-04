# Використовуємо офіційний LTS образ Jenkins з JDK 21 як базовий
FROM jenkins/jenkins:lts-jdk21

# Перемикаємося на користувача root для встановлення необхідних інструментів
USER root

# Встановлюємо sudo, curl, wget, lsb-release та dirmngr
RUN apt-get update && \
    apt-get install -y sudo curl wget apt-transport-https ca-certificates gnupg lsb-release dirmngr && \
    rm -rf /var/lib/apt/lists/*

# Додаємо GPG ключ Docker
RUN curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /usr/share/keyrings/docker-archive-keyring.gpg

# Додаємо репозиторій Docker
RUN echo "deb [arch=$(dpkg --print-architecture) signed-by=/usr/share/keyrings/docker-archive-keyring.gpg] https://download.docker.com/linux/debian $(lsb_release -cs) stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null

# Оновлюємо список пакетів з новим репозиторієм та встановлюємо Docker CLI
RUN apt-get update && \
    apt-get install -y docker-ce-cli && \
    rm -rf /var/lib/apt/lists/*

# === ПОЧАТОК ЗМІН, ЯКІ ПОТРІБНО ДОДАТИ ===

# Створюємо групу 'docker', якщо її не існує
RUN groupadd docker || true

# Додаємо Jenkins користувача до групи docker
# Це дасть користувачу jenkins права на доступ до docker.sock
RUN usermod -aG docker jenkins

# === КІНЕЦЬ ЗМІН, ЯКІ ПОТРІБНО ДОДАТИ ===


# Встановлюємо kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl

# Повертаємося до користувача jenkins
USER jenkins