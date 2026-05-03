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
                sh 'mvn clean package -DskipTests -B'
            }
        }

        stage('Tests & Coverage') {
            steps {
                sh 'mvn test jacoco:report -B'
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
                withSonarQubeEnv('SonarQube') {
                    withCredentials([string(credentialsId: 'sonar-token', variable: 'SONAR_TOKEN')]) {
                        sh """
                            mvn sonar:sonar -B \
                              -Dsonar.projectKey=${SONAR_PROJECT_KEY} \
                              -Dsonar.login=${SONAR_TOKEN}
                        """
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
                    sh """
                        docker build -t ${IMAGE_NAME} .
                        echo "${DOCKER_PASS}" | docker login -u "${DOCKER_USER}" --password-stdin
                        docker push ${IMAGE_NAME}
                        docker logout
                    """
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
