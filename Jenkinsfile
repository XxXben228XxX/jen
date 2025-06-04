// Jenkinsfile - Повний CI/CD Pipeline для Spring Boot та React з Minikube Deployment

pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
        jdk 'Java 21'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
                echo "Repository checked out."
            }
        }

        stage('Debug Workspace') { // Оновлений етап для відладки шляхів
            steps {
                script {
                    echo "Checking specific file locations:"
                    sh 'pwd' // Показуємо поточну робочу директорію (має бути корінь репозиторію)

                    echo "--- Frontend files ---"
                    sh 'ls -l Dockerfile || echo "Dockerfile not found in root"'
                    sh 'ls -l package.json || echo "package.json not found in root"'
                    sh 'ls -d src || echo "src directory not found in root"'

                    echo "--- Backend files ---"
                    sh 'ls -l pom.xml || echo "pom.xml not found in root"'
                    sh 'ls -d backend || echo "backend directory not found"'
                    sh 'ls -d db-dockerfile || echo "db-dockerfile directory not found"'

                    echo "--- Kubernetes files ---"
                    sh 'ls -d k8s || echo "k8s directory not found"'
                }
            }
        }

        stage('Build Backend') {
            steps {
                script {
                    withEnv(["PATH+MAVEN=${tool 'Maven 3.9.6'}/bin"]) {
                        // Maven працює з pom.xml у корені
                        sh 'mvn clean install -DskipTests'
                        echo "Backend project built successfully."
                    }
                }
            }
        }

        stage('Test Backend') {
            steps {
                script {
                    sh 'mvn test' // Все ще буде "No tests to run"
                    echo "Backend tests executed successfully."
                }
            }
        }

        stage('Package Backend') {
            steps {
                script {
                    sh 'mvn package -DskipTests'
                    echo "Backend application packaged into JAR."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Backend Docker Image') {
            steps {
                script {
                    def backendImageName = "demo6:latest"

                    // ОНОВЛЕНО: Використання DOCKER_HOST для зв'язку з Docker Desktop через TCP
                    withEnv(["PATH+DOCKER=/usr/bin", "DOCKER_HOST=tcp://host.docker.internal:2375"]) {
                        // Контекст збірки - корінь ('.')
                        // Dockerfile для бек-енду знаходиться в db-dockerfile/Dockerfile
                        sh "docker build -t ${backendImageName} -f db-dockerfile/Dockerfile ."
                    }
                    echo "Backend Docker image ${backendImageName} built."
                }
            }
        }

        stage('Deploy Backend to Minikube') {
            steps {
                script {
                    echo "Applying Backend Kubernetes manifests..."
                    // Оновлений шлях до kubectl, якщо він не знайдений
                    withEnv(["PATH+KUBECTL=/usr/local/bin"]) { // Переконайтеся, що kubectl встановлений тут, або у системному PATH
                        sh 'kubectl apply -f k8s/deployment.yaml'
                        sh 'kubectl apply -f k8s/service.yaml'

                        echo "Waiting for backend deployment to roll out..."
                        timeout(time: 5, unit: 'MINUTES') {
                            sh 'kubectl rollout status deployment/demo6-backend-deployment --watch=true'
                        }
                        echo "Backend deployed to Minikube successfully."

                        echo "Access your backend application at: "
                        sh 'minikube service demo6-backend-service --url'
                    }
                }
            }
        }

        // --- НОВІ ЕТАПИ ДЛЯ ФРОНТ-ЕНДУ ---

        stage('Build Frontend Docker Image') {
            steps {
                script {
                    def frontendImageName = "frontend-demo:latest"
                    // URL бек-енду з Minikube для змінної середовища фронт-енду
                    def reactAppApiUrl = "http://192.168.49.2:30000" // !!! ПЕРЕВІРТЕ ЦЕЙ URL !!!

                    // ОНОВЛЕНО: Використання DOCKER_HOST для зв'язку з Docker Desktop через TCP
                    withEnv(["PATH+DOCKER=/usr/bin", "DOCKER_HOST=tcp://host.docker.internal:2375"]) {
                        // Контекст збірки - корінь ('.'), оскільки Dockerfile та вихідники фронт-енду там
                        // Dockerfile для фронт-енду знаходиться в корені репозиторію
                        sh "docker build -t ${frontendImageName} -f Dockerfile --build-arg REACT_APP_API_URL=${reactAppApiUrl} ."
                    }
                    echo "Frontend Docker image ${frontendImageName} built."
                }
            }
        }

        stage('Deploy Frontend to Minikube') {
            steps {
                script {
                    echo "Applying Frontend Kubernetes manifests..."
                    // Оновлений шлях до kubectl, якщо він не знайдений
                    withEnv(["PATH+KUBECTL=/usr/local/bin"]) { // Переконайтеся, що kubectl встановлений тут
                        sh 'kubectl apply -f k8s/frontend/deployment.yaml'
                        sh 'kubectl apply -f k8s/frontend/service.yaml'

                        echo "Waiting for frontend deployment to roll out..."
                        timeout(time: 5, unit: 'MINUTES') {
                            sh 'kubectl rollout status deployment/frontend-deployment --watch=true'
                        }
                        echo "Frontend deployed to Minikube successfully."

                        echo "Access your frontend application at: "
                        sh 'minikube service frontend-service --url'
                    }
                }
            }
        }
    }

    post {
        always {
            echo "Pipeline finished."
        }
        success {
            echo "Pipeline succeeded! Applications are deployed."
        }
        failure {
            echo "Pipeline failed. Check console output for errors."
        }
    }
}