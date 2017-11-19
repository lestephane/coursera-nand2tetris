#!groovy
pipeline {

    // https://issues.jenkins-ci.org/browse/JENKINS-33510
    agent { docker 'gradle:alpine' }

    //agent any

    stages {
        stage('Build') {
            steps {
                sh 'gradle build'
            }
        }
        stage('Test') {
            steps {
                sh 'gradle check'
            }
        }
    }
    post {
        always {
            junit '**/build/test-results/**/*.xml'
        }
    }
}
