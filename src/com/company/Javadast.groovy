// src/com/company/Javadast.groovy
package com.company

class Javadast implements Serializable {
    def steps

    Javadast(steps) {
        this.steps = steps
    }

    def clean() {
        steps.cleanWs()
    }

    def installzap(String zapVersion) {
        steps.sh "wget https://github.com/zaproxy/zaproxy/releases/download/v${zapVersion}/ZAP_${zapVersion}_Linux.tar.gz"
        steps.sh "tar -xvf ZAP_${zapVersion}_Linux.tar.gz"
    }

    def zapscan(String zapDir, String zapPort, String scanUrl, String reportPath) {
        steps.sh """
            ${zapDir}/zap.sh -cmd -port ${zapPort} \\
            -quickurl ${scanUrl} -quickprogress -quickout ${reportPath}
        """
    }

    def publishhtml() {
        steps.publishHTML(target: [
            allowMissing: false,
            alwaysLinkToLastBuild: true,
            keepAll: true,
            reportDir: '.',
            reportFiles: 'zap-report.html',
            reportName: 'ZAP Scan Report',
            reportTitles: 'ZAP Report'
        ])
    }

    def notification(String status, String priority, String slackChannel, String emailRecipients) {
        def env = steps.env
        def currentBuild = steps.currentBuild

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
*Job:* \`${env.JOB_NAME}\`
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

        steps.slackSend(channel: slackChannel, color: colors[status], message: slackMessage)
        steps.mail(to: emailRecipients, subject: subjects[status], body: emailBody, mimeType: 'text/html')
    }
}
