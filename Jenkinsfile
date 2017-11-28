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
                sh 'find -type f | xargs md5sum'
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
                        sh '''
                        set -euo pipefail
                        for test in 07/MemoryAccess/BasicTest/BasicTest; do
                            java -cp vmtranslator/build/classes/java/main VMTranslator ${test}.vm
                            tools/CPUEmulator.sh ${test}.tst
                        done
                            /* \
                            07/MemoryAccess/StaticTest/StaticTest.tst \
                            07/MemoryAccess/PointerTest/PointerTest.tst \
                            07/StackArithmetic/StackTest/StackTest.tst \
                            07/StackArithmetic/SimpleAdd/SimpleAdd.tst \
                            08/FunctionCalls/FibonacciElement/FibonacciElement.tst \
                            08/FunctionCalls/SimpleFunction/SimpleFunction.tst \
                            08/FunctionCalls/StaticsTest/StaticsTest.tst \
                            08/FunctionCalls/NestedCall/NestedCall.tst \
                            08/ProgramFlow/BasicLoop/BasicLoop.tst \
                            08/ProgramFlow/FibonacciSeries/FibonacciSeries.tst */
                        '''
                    }
                }
            }
        }
    }
}
