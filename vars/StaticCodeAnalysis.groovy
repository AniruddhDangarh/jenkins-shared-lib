def call() {
    withCredentials([string(credentialsId: 'sonarcred', variable: 'SONARQUBE_TOKEN')]) {
        withSonarQubeEnv('sonarqube') {
            sh """
                mvn sonar:sonar \
                -Dsonar.projectKey=${env.SONAR_PROJECT_KEY} \
                -Dsonar.login=${SONARQUBE_TOKEN}
            """
        }
    }
}
