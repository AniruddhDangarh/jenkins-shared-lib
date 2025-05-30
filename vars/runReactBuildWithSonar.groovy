def call(Map config = [:]) {
    def branch = config.get('branch', 'main')
    def repo = config.get('repo', '')
    def projectKey = config.get('projectKey', 'my-project')
    def sonarEnvName = config.get('sonarEnvName', 'SonarQube')
    def nodeTool = config.get('nodeTool', 'NodeJS')
    def scannerTool = config.get('scannerTool', 'SonarQube Scanner')

    pipeline {
        agent any

        tools {
            nodejs "${nodeTool}"
        }

        environment {
            SONARQUBE_SCANNER_HOME = tool name: "${scannerTool}", type: 'hudson.plugins.sonar.SonarRunnerInstallation'
        }

        stages {
            stage('Checkout Code') {
                steps {
                    git branch: branch, url: repo
                }
            }

            stage('Install Dependencies') {
                steps {
                    sh 'npm install'
                }
            }

            stage('Run SonarQube Analysis') {
                steps {
                    withSonarQubeEnv("${sonarEnvName}") {
                        sh """
                            ${SONARQUBE_SCANNER_HOME}/bin/sonar-scanner \\
                            -Dsonar.projectKey=${projectKey} \\
                            -Dsonar.sources=src
                        """
                    }
                }
            }

            stage('Quality Gate') {
                steps {
                    script {
                        def qg = waitForQualityGate()
                        if (qg.status != 'OK') {
                            error "Pipeline aborted due to quality gate failure: ${qg.status}"
                        }
                    }
                }
            }
        }

        post {
            always {
                echo 'Pipeline execution completed.'
            }
            success {
                echo 'Pipeline completed successfully.'
            }
            failure {
                echo 'Pipeline failed.'
            }
        }
    }
}
