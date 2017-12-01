#! /usr/bin/env groovy
pipeline {

    agent { dockerfile true }

    options {
        buildDiscarder(logRotator(numToKeepStr: '7'))
    }
    stages {
        stage('Init') {
            steps {
                sh 'gradle clean'
                sh 'git clean -f'
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

                        if [ -f "vmtranslator/out/production/classes/VMTranslator.class" ]; then
                            vmclasspath="vmtranslator/out/production/classes"
                        else
                            vmclasspath="vmtranslator/build/classes/java/main"
                        fi

                        runtest() {
                            local input="${1}"
                            if [ -d "${test}" ]; then
                                testoutput="${input}/$(basename "${test}")"
                            else
                                testoutput="${input/.vm/}"
                            fi
                            rm -f "${testoutput}.asm" "${testoutput}.out"
                            java -cp "${vmclasspath}" VMTranslator "${input}"
                            tools/CPUEmulator.sh "${testoutput}.tst" || {
                                diff -w "${testoutput}.out" "${testoutput}.cmp"
                                return 1
                            }
                        }

                        testlist() {
                            echo 07/StackArithmetic/SimpleAdd/SimpleAdd.vm
                            echo 07/StackArithmetic/StackTest/StackTest.vm
                            echo 07/MemoryAccess/BasicTest/BasicTest.vm
                            echo 07/MemoryAccess/PointerTest/PointerTest.vm
                            echo 07/MemoryAccess/StaticTest/StaticTest.vm
                            echo 08/ProgramFlow/BasicLoop/BasicLoop.vm
                            echo 08/ProgramFlow/FibonacciSeries/FibonacciSeries.vm
                            echo 08/FunctionCalls/SimpleFunction/SimpleFunction.vm
                            echo 08/FunctionCalls/NestedCall
                        }

                        # 08/FunctionCalls/FibonacciElement/FibonacciElement \
                        # 08/FunctionCalls/StaticsTest/StaticsTest \

                        errorcount=0
                        testsuite() {
                            echo "1..$(testlist | wc -l)"
                            testnumber=1
                            for test in $(testlist); do
                                testoutput=$(runtest "${test}" 2>&1) && {
                                    echo -n "ok "
                                } || {
                                    echo -n "not ok "
                                    errorcount=$((errorcount + 1))
                                }
                                echo "$((testnumber++)) ${test}"
                                echo "${testoutput}" | while read line; do echo "# ${line}"; done
                            done
                            return $errorcount
                        }

                        testsuite > cpuemulator-tests.tap
                        '''
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
