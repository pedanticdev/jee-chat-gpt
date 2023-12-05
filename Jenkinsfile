final def MVN_OPTS = '-B -ntp'

pipeline {
    agent none
    environment {
        DOCKER_IMAGE = 'luqmanfish/jee-gpt-jenkins:0.1.1'
    }
    stages {
        stage('Maven Install') {
            agent {
                docker {
                    image 'maven:3.9.6'
                }
            }
            stage('Prepare') {
                steps {
                    sh "mvn ${MVN_OPTS} --version"
                }
            }
            stage('Build') {
                steps {
                    sh "mvn ${MVN_OPTS} compile"
                }
            }
            stage('Unit Test') {
                steps {
                    sh "mvn ${MVN_OPTS} test"
                }
            }
            stage('Integration Test') {
                steps {
                    sh "mvn ${MVN_OPTS} integration-test"
                }
            }
            stage('Package') {
                steps {
                    sh "mvn ${MVN_OPTS} clean package -Pproduction -DskipTests"
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                script {
                    def dockerImage = '$DOCKER_IMAGE'
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
                        docker push $DOCKER_IMAGE
                    '''
                    }
                }
            }
        }
    }
}