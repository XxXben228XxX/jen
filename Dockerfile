# Використовуємо офіційний LTS образ Jenkins з JDK 21 як базовий
FROM jenkins/jenkins:lts-jdk21

# Перемикаємося на користувача root для встановлення необхідних інструментів
USER root

# Встановлюємо sudo, curl, wget, lsb-release та dirmngr
# А також всі компоненти Docker (демон, клієнт, containerd, buildx, compose)
RUN apt-get update && \
    apt-get install -yq ca-certificates curl gnupg lsb-release sudo wget apt-transport-https dirmngr && \
    rm -rf /var/lib/apt/lists/*

# Додаємо GPG ключ Docker
RUN install -m 0755 -d /etc/apt/keyrings && \
    curl -fsSL https://download.docker.com/linux/debian/gpg | gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# Додаємо репозиторій Docker до Apt sources
RUN echo \
    "deb [arch=\"$(dpkg --print-architecture)\" signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/debian \
    \"$(. /etc/os-release && echo \"$VERSION_CODENAME\")\" stable" | \
    tee /etc/apt/sources.list.d/docker.list > /dev/null

# Оновлюємо список пакетів та встановлюємо Docker-компоненти
# Зверніть увагу: тепер встановлюється docker-ce (демон), а не лише docker-ce-cli
RUN apt-get update && \
    apt-get install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin && \
    rm -rf /var/lib/apt/lists/*

# Додаємо користувача jenkins до групи docker.
# Це дозволить користувачу jenkins виконувати команди docker без sudo.
RUN groupadd docker || true && usermod -aG docker jenkins

# === ВИДАЛЕНО: Скрипт fix-docker-sock-perms.sh та його виклик у CMD ===
# Ці кроки більше не потрібні, оскільки Jenkins тепер запускає свій власний Docker демон всередині.

# Встановлюємо kubectl
RUN curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl" && \
    install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl && \
    rm kubectl

# Встановлюємо minikube
ARG MINIKUBE_VERSION="v1.36.0"
RUN curl -Lo minikube https://github.com/kubernetes/minikube/releases/download/${MINIKUBE_VERSION}/minikube-linux-amd64 && \
    chmod +x minikube && \
    mv minikube /usr/local/bin/

# Повертаємося до користувача jenkins
USER jenkins

# Базовий образ Jenkins вже має правильний ENTRYPOINT.
# Ми не змінюємо CMD, оскільки тепер Docker-демон запускатиметься як частина середовища.
# Jenkins запускається з його стандартним ENTRYPOINT.