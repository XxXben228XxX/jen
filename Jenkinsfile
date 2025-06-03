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
        stage('Checkout') {
            steps {
                // Витягування коду з вашого Git репозиторію
                // ЗАМІНІТЬ: на URL вашого репозиторію та гілку
                git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
                echo "Repository checked out."
            }
        }

        stage('Build') {
            steps {
                script {
                    // Збірка проекту за допомогою Maven
                    sh 'mvn clean install -DskipTests'
                    echo "Project built successfully."
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    // Виконання модульних та інтеграційних тестів
                    sh 'mvn test'
                    echo "Tests executed successfully."
                }
            }
        }

        stage('Package') {
            steps {
                script {
                    // Упаковка додатку у виконуваний JAR файл
                    sh 'mvn package -DskipTests'
                    echo "Application packaged into JAR."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageName = "demo6:latest" // Ім'я образу: 'demo6'

                    // Переходимо в директорію, де знаходиться Dockerfile для бекенду
                    // Якщо ваш Dockerfile знаходиться в корені проекту, залиште dir('.')
                    // Якщо Dockerfile знаходиться в піддиректорії 'backend', використовуйте dir('backend')
                    dir('backend') { // Припускаємо, що Dockerfile знаходиться в директорії 'backend'
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