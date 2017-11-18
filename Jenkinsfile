#!groovy
pipeline {

    // https://issues.jenkins-ci.org/browse/JENKINS-33510
    agent { docker 'gradle:alpine' }

    //agent any

    stages {
        stage('Gradle Test') {
            steps {
                sh 'gradle test'
            }
        }
    }
}
