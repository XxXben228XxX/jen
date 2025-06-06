// Jenkinsfile - Повний CI/CD Pipeline для Spring Boot та React з Minikube Deployment

pipeline {
    agent any

    options {
        // !!! НОВИЙ РЯДОК: Запобігає автоматичному клонуванню Jenkins
        skipDefaultCheckout()
    }

    tools {
        maven 'Maven 3.9.6'
        jdk 'Java 21'
    }

    stages {
        stage('Cleanup and Checkout') { // Об'єднуємо очистку та клонування
            steps {
                cleanWs() // Очищує робочий простір перед клонуванням
                git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
                echo "Repository checked out."
            }
        }

        // !!! ЦЕЙ ЕТАП ВИДАЛЕНО: Він дублювався з 'Cleanup and Checkout'
        // stage('Checkout') {
        //     steps {
        //         git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
        //         echo "Repository checked out."
        //     }
        // }

        stage('Debug Workspace') {
            steps {
                script {
                    echo "Checking specific file locations:"
                    sh 'pwd'

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
                        sh 'mvn clean install -DskipTests'
                        echo "Backend project built successfully."
                    }
                }
            }
        }

        stage('Test Backend') {
            steps {
                script {
                    sh 'mvn test'
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
                                    echo "Starting Docker daemon inside Jenkins container for debugging..."
                                    sh 'sleep 5'

                                    echo "Checking Docker socket permissions and Jenkins user info..."
                                    sh 'ls -l /var/run/docker.sock || echo "Docker socket not found or permission denied to list"'
                                    sh 'id'

                                    sh 'sudo chown root:996 /var/run/docker.sock || echo "Failed to change docker.sock group. Trying chmod..."'
                                    sh 'ls -l /var/run/docker.sock'
                                    sh 'sudo chmod 666 /var/run/docker.sock || echo "Failed to chmod docker.sock"'
                                    sh 'ls -l /var/run/docker.sock'

                                    // --- КРИТИЧНА ЗМІНА ТУТ (знову!) ---
                                    echo "Attempting docker info directly to check connectivity (capturing all output)..."
                                    // Виконуємо 'docker info', перенаправляємо stderr в stdout (2>&1),
                                    // і захоплюємо весь об'єднаний вивід та статус
                                    def dockerInfoResult = sh(script: 'docker info 2>&1', returnStdout: true, returnStatus: true)
                                    def fullDockerInfoOutput = dockerInfoResult.stdout.trim() // Тепер це міститиме stdout та stderr
                                    def dockerInfoExitCode = dockerInfoResult.status

                                    echo "Full Docker Info Output:\n${fullDockerInfoOutput}"
                                    echo "Docker Info Exit Code: ${dockerInfoExitCode}"

                                    // Перевіряємо, чи docker info успішно завершився (код виходу 0)
                                    if (dockerInfoExitCode == 0) {
                                        echo "Docker daemon is accessible and responsive."
                                        sh 'docker build -t demo6:latest -f db-dockerfile/Dockerfile .'
                                        echo "Backend Docker image built successfully."
                                    } else {
                                        // Якщо код виходу не 0, це проблема
                                        error("Docker daemon is NOT accessible or responsive. Docker info exited with code ${dockerInfoExitCode}. Full output: ${fullDockerInfoOutput}")
                                    }
                                    // --- КІНЕЦЬ КРИТИЧНОЇ ЗМІНИ ---
                                }
                            }
                        }

        stage('Deploy Backend to Minikube') {
                    steps {
                        script {
                            withEnv([
                                "PATH+KUBECTL=/usr/local/bin",
                                "KUBECONFIG=/var/jenkins_home/.kube/config" // Шлях до конфігураційного файлу всередині контейнера
                            ]) {
                                echo "Starting Minikube cluster..."
                                sh 'minikube start --driver=docker' // <--- ДОДАЙТЕ ЦЕЙ РЯДОК
                                sh 'sleep 10' // <--- ДОДАЙТЕ ЦЕЙ РЯДОК (даємо Minikube час на повний запуск)

                                sh 'minikube image load demo6:latest'
                                echo "Backend image loaded into Minikube."

                                echo "Correcting kubeconfig paths for Linux environment..."
                                sh 'sed -i "s|C:/Users/Den/.minikube|/var/jenkins_home/.minikube|g" ${KUBECONFIG}'
                                sh 'sed -i "s|C:\\\\Users\\\\Den\\\\.minikube|/var/jenkins_home/.minikube|g" ${KUBECONFIG}'
                                sh 'cat ${KUBECONFIG} | tr "\\\\" "/" > ${KUBECONFIG}.tmp && mv ${KUBECONFIG}.tmp ${KUBECONFIG}'
                                echo "kubeconfig paths corrected."

                                echo "--- KUBECONFIG CONTENT AFTER TR ---"
                                sh 'cat ${KUBECONFIG}'
                                echo "--- KUBECONFIG CONTENT END ---"


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
                }

        // --- ЕТАПИ ДЛЯ ФРОНТ-ЕНДУ ---

        stage('Build Frontend Docker Image') {
                            steps {
                                script {
                                    echo "Starting Docker daemon inside Jenkins container for debugging..."
                                    sh 'sleep 5'

                                    echo "Checking Docker socket permissions and Jenkins user info (Frontend)..."
                                    sh 'ls -l /var/run/docker.sock || echo "Docker socket not found or permission denied to list"'
                                    sh 'id'

                                    sh 'sudo chown root:996 /var/run/docker.sock || echo "Failed to change docker.sock group. Trying chmod..."'
                                    sh 'ls -l /var/run/docker.sock'
                                    sh 'sudo chmod 666 /var/run/docker.sock || echo "Failed to chmod docker.sock"'
                                    sh 'ls -l /var/run/docker.sock'

                                    // --- КРИТИЧНА ЗМІНА ТУТ ---
                                    echo "Attempting docker info directly to check connectivity (capturing all output)..."
                                    def dockerInfoResult = sh(script: 'docker info 2>&1', returnStdout: true, returnStatus: true)
                                    def fullDockerInfoOutput = dockerInfoResult.stdout.trim()
                                    def dockerInfoExitCode = dockerInfoResult.status

                                    echo "Full Docker Info Output:\n${fullDockerInfoOutput}"
                                    echo "Docker Info Exit Code: ${dockerInfoExitCode}"

                                    if (dockerInfoExitCode == 0) {
                                        echo "Docker daemon is accessible and responsive."
                                        sh 'docker build -t frontend-demo:latest -f frontend/Dockerfile .'
                                        echo "Frontend Docker image built successfully."
                                    } else {
                                        error("Docker daemon is NOT accessible or responsive. Docker info exited with code ${dockerInfoExitCode}. Full output: ${fullDockerInfoOutput}")
                                    }
                                    // --- КІНЕЦЬ КРИТИЧНОЇ ЗМІНИ ---
                                }
                            }
                        }

        stage('Deploy Frontend to Minikube') {
                            steps {
                                script {
                                    withEnv([
                                        "PATH+KUBECTL=/usr/local/bin",
                                        "KUBECONFIG=/var/jenkins_home/.kube/config"
                                    ]) {
                                        echo "Starting Minikube cluster..."
                                        sh 'minikube start --driver=docker' // <--- ДОДАЙТЕ ЦЕЙ РЯДОК
                                        sh 'sleep 10' // <--- ДОДАЙТЕ ЦЕЙ РЯДОК

                                        sh 'minikube image load frontend-demo:latest'
                                        echo "Frontend image loaded into Minikube."

                                        echo "Correcting kubeconfig paths for Linux environment..."
                                        sh 'sed -i "s|C:/Users/Den/.minikube|/var/jenkins_home/.minikube|g" ${KUBECONFIG}'
                                        sh 'sed -i "s|C:\\\\Users\\\\Den\\\\.minikube|/var/jenkins_home/.minikube|g" ${KUBECONFIG}'
                                        sh 'cat ${KUBECONFIG} | tr "\\\\" "/" > ${KUBECONFIG}.tmp && mv ${KUBECONFIG}.tmp ${KUBECONFIG}'
                                        echo "kubeconfig paths corrected."

                                        echo "--- KUBECONFIG CONTENT AFTER TR ---"
                                        sh 'cat ${KUBECONFIG}'
                                        echo "--- KUBECONFIG CONTENT END ---"

                                        echo "Applying Frontend Kubernetes manifests..."
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