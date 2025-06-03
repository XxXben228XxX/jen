// Jenkinsfile - Повний CI/CD Pipeline для Spring Boot з Minikube Deployment

pipeline {
    agent any

    tools {
        maven 'Maven 3.9.6'
        jdk 'Java 21'
    }

    stages {
        // ВИДАЛЕНИЙ ЕТАП 'Manual Workspace Cleanup'

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/XxXben228XxX/jen.git'
                echo "Repository checked out."
            }
        }

        stage('Build') {
            steps {
                script {
                    withEnv(["PATH+MAVEN=${tool 'Maven 3.9.6'}/bin"]) {
                        dir('backend') {
                            sh 'mvn clean install -DskipTests'
                            echo "Project built successfully."
                        }
                    }
                }
            }
        }

        stage('Test') {
            steps {
                script {
                    dir('backend') {
                        sh 'mvn test'
                        echo "Tests executed successfully."
                    }
                }
            }
            // Якщо у вас є тести, які можуть падати, і ви хочете, щоб пайплайн продовжувався,
            // ви можете обернути sh 'mvn test' у try-catch або використовувати опції Maven.
        }

        stage('Package') {
            steps {
                script {
                    dir('backend') {
                        sh 'mvn package -DskipTests'
                        echo "Application packaged into JAR."
                        archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
                    }
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def imageName = "demo6:latest"

                    withEnv(["PATH+DOCKER=/usr/bin"]) {
                        dir('backend') {
                            sh "docker build -t ${imageName} ."
                        }
                    }
                    echo "Docker image ${imageName} built."
                }
            }
        }

        stage('Deploy to Minikube') {
            steps {
                script {
                    echo "Applying Kubernetes manifests..."
                    sh 'kubectl apply -f k8s/deployment.yaml'
                    sh 'kubectl apply -f k8s/service.yaml'

                    echo "Waiting for deployment to roll out..."
                    timeout(time: 5, unit: 'MINUTES') {
                        sh 'kubectl rollout status deployment/demo6-backend-deployment --watch=true'
                    }
                    echo "Application deployed to Minikube successfully."

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