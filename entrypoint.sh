#!/bin/bash
# Запускаємо скрипт для зміни дозволів docker.sock
/usr/local/bin/fix-docker-sock-perms.sh &

# Викликаємо оригінальний entrypoint Jenkins
/usr/local/bin/jenkins.sh "$@"