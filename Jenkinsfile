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
                        sh '''
                        set -euo pipefail
                        for test in \
                            07/StackArithmetic/SimpleAdd/SimpleAdd \
                            07/StackArithmetic/StackTest/StackTest \
                            07/MemoryAccess/BasicTest/BasicTest \
                            07/MemoryAccess/PointerTest/PointerTest \
                            07/MemoryAccess/StaticTest/StaticTest \
                            08/ProgramFlow/BasicLoop/BasicLoop \
                            08/ProgramFlow/FibonacciSeries/FibonacciSeries \
                            08/FunctionCalls/SimpleFunction/SimpleFunction; do
                                rm -f ${test}.asm
                                java -cp vmtranslator/build/classes/java/main VMTranslator ${test}.vm
                                tools/CPUEmulator.sh "${test}.tst"
                        done
                            # 08/FunctionCalls/FibonacciElement/FibonacciElement.tst \
                            # 08/FunctionCalls/StaticsTest/StaticsTest.tst \
                            # 08/FunctionCalls/NestedCall/NestedCall.tst \
                        '''
                    }
                }
            }
        }
    }
}
