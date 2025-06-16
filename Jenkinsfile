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

wait for me, i'' share groovy file also


ChatGPT said:
Got it—take your time. Send over the Groovy files from your shared library whenever you’re ready, and we’ll walk through the conversion together.




You said:
this is my groovy file

def call() {
    cleanWs()
}

def installzap(String zapVersion) {
    sh "wget https://github.com/zaproxy/zaproxy/releases/download/v${zapVersion}/ZAP_${zapVersion}_Linux.tar.gz"
    sh "tar -xvf ZAP_${zapVersion}_Linux.tar.gz"
}

def zapscan(String zapDir, String zapPort, String scanUrl, String reportPath) {
    sh """
        ${zapDir}/zap.sh -cmd -port ${zapPort} \\
        -quickurl ${scanUrl} -quickprogress -quickout ${reportPath}
    """
}

def publishhtml() {
    publishHTML(
        target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '.',
            reportFiles: 'zap-report.html',
            reportName: 'ZAP Scan Report',
            reportTitles: 'ZAP Report'
        ]
    )
}
def notification(String status, String priority, String slackChannel, String emailRecipients) {
    def icons = [SUCCESS: '🟢', FAILURE: '🔴']
    def results = [
        P0: [SUCCESS: 'Urgent job completed successfully! ✅', FAILURE: 'Urgent job FAILED! 🚨'],
        P1: [SUCCESS: 'Important job completed successfully! ✅', FAILURE: 'Important job FAILED! ⚠️'],
        P2: [SUCCESS: 'Standard job completed! ✅', FAILURE: 'Standard job FAILED. ❗']
    ]
    def colors = [SUCCESS: 'good', FAILURE: 'danger']
    def subjects = [
        SUCCESS: "${priority} SUCCESS: Jenkins Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'",
        FAILURE: "${priority} FAILURE: Jenkins Job '${env.JOB_NAME} [${env.BUILD_NUMBER}]'"
    ]

    def buildTime = new Date().format("yyyy-MM-dd HH:mm:ss", TimeZone.getTimeZone('Asia/Kolkata'))
    def triggeredBy = currentBuild.getBuildCauses().find { it.userId }?.userName ?: "Automated/Unknown"
    def reportUrl = "${env.BUILD_URL}ZAP_Scan_Report/"

    def failureReason = ""
    if (status == "FAILURE") {
        def logLines = currentBuild.rawBuild.getLog(100)
        def errorLine = logLines.find { it.contains("Exception") || it.contains("ERROR") || it.contains("FAILURE") }
        failureReason = errorLine ? "<p><strong>Reason for Failure:</strong> ${errorLine.trim()}</p>" : "<p><strong>Reason for Failure:</strong> Not found in last 100 log lines.</p>"
    }

    def slackMessage = """
${icons[status]} *${priority} ${status}*
*Status:* ${results[priority][status]}
*Job:* ${env.JOB_NAME}
*Build Number:* #${env.BUILD_NUMBER}
*Triggered By:* ${triggeredBy}
*Time (IST):* ${buildTime}
🔗 *Build URL:* <${env.BUILD_URL}|View Build>
📊 *ZAP Report:* <${reportUrl}|View Report>${status == "FAILURE" ? "\n❌ *Failure Reason:* ${failureReason}" : ""}
"""

    def emailBody = """
<html>
  <body>
    <p><strong>${icons[status]} ${priority} ${status}</strong></p>
    <p><strong>Status:</strong> ${results[priority][status]}</p>
    <p><strong>Job:</strong> ${env.JOB_NAME}</p>
    <p><strong>Build Number:</strong> #${env.BUILD_NUMBER}</p>
    <p><strong>Triggered By:</strong> ${triggeredBy}</p>
    <p><strong>Time (IST):</strong> ${buildTime}</p>
    <p><strong>Build URL:</strong> <a href="${env.BUILD_URL}">${env.BUILD_URL}</a></p>
    <p><strong>ZAP Report:</strong> <a href="${reportUrl}">${reportUrl}</a></p>
    ${failureReason}
  </body>
</html>
"""

    slackSend(channel: slackChannel, color: colors[status], message: slackMessage)
    mail(to: emailRecipients, subject: subjects[status], body: emailBody, mimeType: 'text/html')
}
