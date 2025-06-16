@Library('shared-library') _
import com.company.Javadast

pipeline {
    agent any
    environment {
        ZAP_VERSION = '2.15.0'
        ZAP_DIR = "ZAP_${ZAP_VERSION}"
        ZAP_PORT = '8090'
        SCAN_URL = 'http://13.234.240.15:8082/swagger-ui/index.html'
        JAVA_HOME = '/usr/lib/jvm/java-17-openjdk-amd64'
        PATH = "${JAVA_HOME}/bin:${env.PATH}"
        ZAP_REPORT = "${env.WORKSPACE}/zap-report.html"
        PRIORITY = 'P0'
        SLACK_CHANNEL = '#jenkins-notification'
        EMAIL_RECIPIENTS = 'aniruddh.dangarh.snaatak@mygurukulam.co'
    }
    
    stages {
        stage('Clean Workspace') {
            steps {
                script {
                    def javadast = new Javadast(this)
                    javadast.clean()
                }
            }
        }
        stage('Install ZAP') {
            steps {
                script {
                    javadast.installzap(env.ZAP_VERSION)
                }
            }
        }

        stage('Run ZAP Scan') {
            steps {
                script {
                    javadast.zapscan(env.ZAP_DIR, env.ZAP_PORT, env.SCAN_URL, env.ZAP_REPORT)
                }
            }
        }

        stage('Publish ZAP Report') {
            steps {
                script {
                    javadast.publishhtml()
                }
            }
        }
    }
    post {
        success {
            script {
                javadast.notification('SUCCESS', env.PRIORITY, env.SLACK_CHANNEL, env.EMAIL_RECIPIENTS)
            }
        }
        failure {
            script {
                javadast.notification('FAILURE', env.PRIORITY, env.SLACK_CHANNEL, env.EMAIL_RECIPIENTS)
            }
        }
        always {
            archiveArtifacts artifacts: 'zap-report.html', onlyIfSuccessful: false, fingerprint: true
            echo "Build completed"
            deleteDir()
        }
    }
}
