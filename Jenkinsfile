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

        stage('Debug Workspace') {
                    steps {
                        script {
                            echo "Checking specific file locations:"
                            sh 'pwd' // Показуємо поточну робочу директорію (має бути корінь репозиторію)

                            echo "--- Frontend files ---"
                            sh 'ls -l Dockerfile || echo "Dockerfile not found in root"' // Чи є Dockerfile у корені (для фронт-енду)?
                            sh 'ls -l package.json || echo "package.json not found in root"' // Чи є package.json у корені?
                            sh 'ls -d src || echo "src directory not found in root"' // Чи є папка src (з React кодом) у корені?

                            echo "--- Backend files ---"
                            sh 'ls -l pom.xml || echo "pom.xml not found in root"' // Чи є pom.xml у корені (для бек-енду)?
                            sh 'ls -d backend || echo "backend directory not found"' // Чи є папка backend (з Java кодом)?
                            sh 'ls -d db-dockerfile || echo "db-dockerfile directory not found"' // Чи є папка db-dockerfile?

                            echo "--- Kubernetes files ---"
                            sh 'ls -d k8s || echo "k8s directory not found"' // Чи є папка k8s?
                        }
                    }
                }

        stage('Build Backend') { // Змінив назву для ясності
            steps {
                script {
                    withEnv(["PATH+MAVEN=${tool 'Maven 3.9.6'}/bin"]) {
                        sh 'mvn clean install -DskipTests'
                        echo "Backend project built successfully."
                    }
                }
            }
        }

        stage('Test Backend') { // Змінив назву для ясності
            steps {
                script {
                    sh 'mvn test'
                    echo "Backend tests executed successfully."
                }
            }
        }

        stage('Package Backend') { // Змінив назву для ясності
            steps {
                script {
                    sh 'mvn package -DskipTests'
                    echo "Backend application packaged into JAR."
                    archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                }
            }
        }

        stage('Build Backend Docker Image') { // Змінив назву для ясності
            steps {
                script {
                    def backendImageName = "demo6:latest" // Зберегли ім'я образу бек-енду

                    withEnv(["PATH+DOCKER=/usr/bin"]) {
                        sh "docker build -t ${backendImageName} -f db-dockerfile/Dockerfile ."
                    }
                    echo "Backend Docker image ${backendImageName} built."
                }
            }
        }

        stage('Deploy Backend to Minikube') { // Змінив назву для ясності
            steps {
                script {
                    echo "Applying Backend Kubernetes manifests..."
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

        // --- НОВІ ЕТАПИ ДЛЯ ФРОНТ-ЕНДУ ---

        stage('Build Frontend Docker Image') { // НОВИЙ ЕТАП
            steps {
                script {
                    def frontendImageName = "frontend-demo:latest" // Ім'я образу для фронт-енду
                    // Використовуємо URL бек-енду з Minikube для змінної середовища фронт-енду
                    def reactAppApiUrl = "http://192.168.49.2:30000" // !!! ПЕРЕВІРТЕ ЦЕЙ URL !!!

                    withEnv(["PATH+DOCKER=/usr/bin"]) {
                        // Контекст збірки - папка 'dockerfile', де знаходиться package.json та вихідники фронт-енду
                        sh "docker build -t ${frontendImageName} -f dockerfile/Dockerfile --build-arg REACT_APP_API_URL=${reactAppApiUrl} dockerfile"
                    }
                    echo "Frontend Docker image ${frontendImageName} built."
                }
            }
        }

        stage('Deploy Frontend to Minikube') { // НОВИЙ ЕТАП
            steps {
                script {
                    echo "Applying Frontend Kubernetes manifests..."
                    sh 'kubectl apply -f k8s/frontend/deployment.yaml' // Шлях до файлів K8s для фронт-енду
                    sh 'kubectl apply -f k8s/frontend/service.yaml'   // Шлях до файлів K8s для фронт-енду

                    echo "Waiting for frontend deployment to roll out..."
                    timeout(time: 5, unit: 'MINUTES') {
                        sh 'kubectl rollout status deployment/frontend-deployment --watch=true' // Ім'я деплойменту фронт-енду
                    }
                    echo "Frontend deployed to Minikube successfully."

                    echo "Access your frontend application at: "
                    sh 'minikube service frontend-service --url' // Ім'я сервісу фронт-енду
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