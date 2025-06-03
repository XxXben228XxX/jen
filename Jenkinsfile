// Jenkinsfile - Повний CI/CD Pipeline для Spring Boot з Minikube Deployment

pipeline {
    agent any // Використовуйте 'any' для запуску на будь-якому доступному агенті.
              // Для більш контрольованого середовища, можна вказати 'agent { label 'your-custom-agent-label' }'

    tools {
        // Конфігурація інструментів (залежить від того, як вони налаштовані у вашому Jenkins)
        // Переконайтеся, що ви налаштували 'Maven' та 'JDK' у 'Manage Jenkins -> Tools'
        maven 'Maven 3.9.6' // Використовуємо версію Maven, яка була у Dockerfile
        jdk 'Java 21'     // Відповідно до java.version з вашого pom.xml
    }

    stages {
        stage('Manual Workspace Cleanup') { // <<< ЦЕЙ НОВИЙ ЕТАП МИ ДОДАЄМО
            steps {
                script {
                    // Примусове видалення вмісту робочої директорії
                    // Це обхідний шлях, якщо "Wipe out workspace" у налаштуваннях завдання не ефективний.
                    sh 'rm -rf .' // Видаляє всі файли та директорії рекурсивно у поточному workspace
                    echo 'Workspace manually cleaned.'
                }
            }
        }
        stage('Checkout') {
            steps {
                // Витягування коду з вашого Git репозиторію
                git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
                echo "Repository checked out."
            }
        }

        stage('Build') {
            steps {
                script {
                    // Збірка проекту за допомогою Maven
                    dir('backend') { // Переходимо в директорію 'backend', якщо там знаходиться pom.xml
                        sh 'mvn clean install -DskipTests'
                        echo "Project built successfully."
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Виконання модульних та інтеграційних тестів
                    dir('backend') { // Переходимо в директорію 'backend'
                        sh 'mvn test'
                        echo "Tests executed successfully."
                    }
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    // Упаковка додатку у виконуваний JAR файл
                    dir('backend') { // Переходимо в директорію 'backend'
                        sh 'mvn package -DskipTests'
                        echo "Application packaged into JAR."
                        // Шлях до артефактів відносно директорії 'backend'
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageName = "demo6:latest" // Ім'я образу: 'demo6'

                    // Переходимо в директорію, де знаходиться Dockerfile для бекенду
                    dir('backend') {
                        sh "docker build -t ${imageName} ."
                    }
                    echo "Docker image ${imageName} built."
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                script {
                    echo "Applying Kubernetes manifests..."
                    // Шлях до маніфестів відносно кореня проекту (k8s/deployment.yaml та k8s/service.yaml)
                    sh 'kubectl apply -f k8s/deployment.yaml'
                    sh 'kubectl apply -f k8s/service.yaml'

                    echo "Waiting for deployment to roll out..."
                    // Очікування розгортання для стабільності
                    timeout(time: 5, unit: 'MINUTES') {
                        // Використовуємо ім'я Deployment, яке ми вказали в k8s/deployment.yaml
                        sh 'kubectl rollout status deployment/demo6-backend-deployment --watch=true'
                    }
                    echo "Application deployed to Minikube successfully."

                    // Отримання URL сервісу (опціонально, для логування в Jenkins Console Output)
                    // Використовуємо ім'я Service, яке ми вказали в k8s/service.yaml
                    echo "Access your application at: "
                    sh 'minikube service demo6-backend-service --url'
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
        }
        success {
            echo "Pipeline succeeded! Application is deployed."
        }
        failure {
            echo "Pipeline failed. Check console output for errors."
        }
    }
}