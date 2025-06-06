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
# Це дасть користувачу jenkins права на доступ до docker.sock
RUN groupadd docker || true
# Додаємо Jenkins користувача до групи docker
RUN usermod -aG docker jenkins

# === НОВИЙ КРОК: Змінюємо дозволи на docker.sock при запуску контейнера (обхідний шлях) ===
# Цей скрипт запускатиметься щоразу при старті контейнера.
# Встановлює дозволи 666 (читання/запис для всіх) на сокет Docker.
# Це менш безпечно, але допомагає вирішити проблеми з дозволами в середовищах розробки.
RUN echo '#!/bin/bash' > /usr/local/bin/fix-docker-sock-perms.sh && \
    echo 'chmod 666 /var/run/docker.sock' >> /usr/local/bin/fix-docker-sock-perms.sh && \
    chmod +x /usr/local/bin/fix-docker-sock-perms.sh

# Додаємо виклик цього скрипта до entrypoint Jenkins, щоб він запускався при старті
# Використовуємо Jenkins `entrypoint.sh` за замовчуванням
ENTRYPOINT ["/usr/bin/tini", "--", "/usr/local/bin/jenkins.sh"]
CMD ["/usr/local/bin/fix-docker-sock-perms.sh", "&&", "/usr/local/bin/jenkins.sh"] # Передаємо наш скрипт перед оригінальним CMD.
# Змінив CMD, щоб додати виконання fix-docker-sock-perms.sh. Це не зовсім стандартно.
# Краще було б створити custom entrypoint, який викликає наш скрипт, а потім оригінальний entrypoint/cmd.
# Давайте зробимо це більш надійним способом.

# Зробимо краще: створимо кастомний entrypoint, який запустить наш скрипт, а потім викличе оригінальний jenkins.sh
COPY entrypoint.sh /usr/local/bin/custom-jenkins-entrypoint.sh
RUN chmod +x /usr/local/bin/custom-jenkins-entrypoint.sh
ENTRYPOINT ["/usr/local/bin/tini", "--", "/usr/local/bin/custom-jenkins-entrypoint.sh"]


# === КІНЕЦЬ НОВОГО КРОКУ ===

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