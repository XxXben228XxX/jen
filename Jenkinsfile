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
                            // ЗМІНА ТУТ: ДОДАНО 'sudo'
                            sh 'sudo dockerd --debug > /tmp/dockerd.log 2>&1 &'
                            sh 'sleep 5'

                            def dockerDaemonReady = sh(script: 'timeout 60 bash -c "while ! docker info >/dev/null 2>&1; do sleep 1; done"', returnStatus: true)

                            echo "--- DOCKERD LOGS START ---"
                            sh 'cat /tmp/dockerd.log'
                            echo "--- DOCKERD LOGS END ---"

                            if (dockerDaemonReady == 0) {
                                echo "Docker daemon is running."
                                sh 'docker build -t demo6:latest -f db-dockerfile/Dockerfile .'
                                echo "Backend Docker image built successfully."
                            } else {
                                error("Docker daemon did not start in time. Check DOCKERD LOGS above for details.")
                            }
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
                            // ЗМІНА ТУТ: ДОДАНО 'sudo'
                            sh 'sudo dockerd --debug > /tmp/dockerd_frontend.log 2>&1 &'
                            sh 'sleep 5'

                            def dockerDaemonReady = sh(script: 'timeout 60 bash -c "while ! docker info >/dev/null 2>&1; do sleep 1; done"', returnStatus: true)

                            echo "--- DOCKERD LOGS START (Frontend) ---"
                            sh 'cat /tmp/dockerd_frontend.log'
                            echo "--- DOCKERD LOGS END (Frontend) ---"

                            if (dockerDaemonReady == 0) {
                                echo "Docker daemon is running."
                                sh 'docker build -t frontend-demo:latest -f frontend/Dockerfile .'
                                echo "Frontend Docker image built successfully."
                            } else {
                                error("Docker daemon did not start in time. Check DOCKERD LOGS above for details.")
                            }
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