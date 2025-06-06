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

# Створюємо групу 'docker', якщо її не існує
RUN groupadd docker || true
# Додаємо Jenkins користувача до групи docker
RUN usermod -aG docker jenkins

# === ВИПРАВЛЕННЯ ДЛЯ ДОЗВОЛІВ DOCKER.SOCK (обхідний шлях) ===
# Цей скрипт буде виконуватися при старті контейнера.
# Встановлює дозволи 666 (читання/запис для всіх) на сокет Docker,
# а також намагається змінити власника на jenkins:docker.
# Це менш безпечно, але допомагає вирішити проблеми з дозволами в середовищах розробки.
RUN echo '#!/bin/bash' > /usr/local/bin/fix-docker-sock-perms.sh && \
    echo 'chmod 666 /var/run/docker.sock || true' >> /usr/local/bin/fix-docker-sock-perms.sh && \
    echo 'chown jenkins:docker /var/run/docker.sock || true' >> /usr/local/bin/fix-docker-sock-perms.sh && \
    chmod +x /usr/local/bin/fix-docker-sock-perms.sh

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

# === ПЕРЕЗАПИСУЄМО CMD ДЛЯ ВИКОНАННЯ НАШОГО СКРИПТА ПЕРЕД ЗАПУСКОМ JENKINS ===
# Базовий образ Jenkins вже має ENTRYPOINT, який використовує tini.
# Ми просто змінюємо CMD, щоб він спочатку виконав наш скрипт, а потім оригінальний jenkins.sh
CMD ["/bin/bash", "-c", "/usr/local/bin/fix-docker-sock-perms.sh && /usr/local/bin/jenkins.sh"]