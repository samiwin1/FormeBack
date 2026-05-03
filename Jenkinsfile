pipeline {
    agent any

    environment {
        IMAGE_NAME = 'pidev/mentor-service:latest'
        SONAR_PROJECT_KEY = 'mentor-service'
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                script {
                    def mvnHome = tool 'Maven'
                    bat "\"${mvnHome}\\bin\\mvn\" clean package -DskipTests -B"
                }
            }
        }

        stage('Tests & Coverage') {
            steps {
                script {
                    def mvnHome = tool 'Maven'
                    bat "\"${mvnHome}\\bin\\mvn\" test jacoco:report -B"
                }
            }
            post {
                always {
                    junit allowEmptyResults: true,
                          testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def mvnHome = tool 'Maven'
                    withSonarQubeEnv('SonarQube') {
                        withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                            bat "\"${mvnHome}\\bin\\mvn\" sonar:sonar -B -Dsonar.projectKey=%SONAR_PROJECT_KEY% -Dsonar.login=%SONAR_TOKEN%"
                        }
                    }
                }
            }
        }

        stage('Docker Build & Push') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhub-credentials',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    bat "docker build -t %IMAGE_NAME% ."
                    bat "echo %DOCKER_PASS% | docker login -u %DOCKER_USER% --password-stdin"
                    bat "docker push %IMAGE_NAME%"
                    bat "docker logout"
                }
            }
        }
    }

    post {
        success {
            echo 'mentor-service CI pipeline completed successfully.'
        }
        failure {
            echo 'mentor-service CI pipeline failed.'
        }
    }
}
