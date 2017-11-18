pipeline {
    agent { docker 'gradle:alpine' } 
    stages {
        stage('Gradle Test') {
            steps {
                dir ('vmtranslator') {
                    sh 'gradle test'
                }
            }
        }
    }
}
