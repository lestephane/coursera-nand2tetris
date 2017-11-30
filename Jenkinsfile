#! /usr/bin/env groovy
pipeline {

    // https://issues.jenkins-ci.org/browse/JENKINS-33510
    agent { docker 'gradle:alpine' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '7'))
    }
    stages {
        stage('Init') {
            steps {
                sh 'gradle clean'
                sh 'find -type f -print0 | xargs -0 md5sum'
            }
        }
        stage('Build') {
            steps {
                sh 'gradle build'
            }
        }
        stage('Test') {
            parallel {
                stage('Unit Tests') {
                    steps {
                        sh 'gradle check'
                    }
                    post {
                        always {
                            junit '**/build/test-results/**/*.xml'
                        }
                    }
                }
                stage('CPUEmulator Tests') {
                    steps {
                        sh "./run-cpuemulator-tests.sh > cpuemulator-tests.tap"
                    }
                    post {
                        always {
                            step([$class: "TapPublisher", testResults: "cpuemulator-tests.tap"])
                        }
                    }
                }
            }
        }
    }
}
