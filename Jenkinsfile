final def MVN_OPTS = '-B -ntp'

pipeline {
    agent any
    environment {
        DOCKER_IMAGE = 'luqmanfish/jee-gpt-jenkins:0.1.1'
    }
    stages {
        stage('Prepare') {
            steps {
                sh "./mvnw ${MVN_OPTS} --version"
            }
        }
        stage('Build') {
            steps {
                sh "./mvnw ${MVN_OPTS} compile"
            }
        }
        stage('Unit Test') {
            steps {
                sh "./mvnw ${MVN_OPTS} test"
            }
        }
        stage('Integration Test') {
            steps {
                sh "./mvnw ${MVN_OPTS} integration-test"
            }
        }
        stage('Package') {
            steps {
                sh "./mvnw ${MVN_OPTS} clean package -Pproduction -DskipTests"
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = "${env.DOCKER_IMAGE}"
                    sh "docker build -t ${dockerImage} ."
                }
            }
        }
        stage('Push to Docker Hub') {
            steps {
                script {
                    withCredentials([usernamePassword(credentialsId: 'DockerHub', usernameVariable: 'DOCKERHUB_USER', passwordVariable: 'DOCKERHUB_PASS')]) {
                        sh '''
                        echo $DOCKERHUB_PASS | docker login -u $DOCKERHUB_USER --password-stdin
                        docker push ${env.DOCKER_IMAGE}
                    '''
                    }
                }
            }
        }
    }
}