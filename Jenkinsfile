pipeline {
    agent { docker 'gradle:alpine' } 
    stages {
        stage('Gradle Test') {
            steps {
                sh 'gradle test'
            }
        }
    }
}
