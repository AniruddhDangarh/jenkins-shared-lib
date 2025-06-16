// vars/javadast.groovy
def call() {
    new com.company.Javadast(this).clean()
}

def installzap(String version) {
    new com.company.Javadast(this).installzap(version)
}

def zapscan(String zapDir, String zapPort, String scanUrl, String reportPath) {
    new com.company.Javadast(this).zapscan(zapDir, zapPort, scanUrl, reportPath)
}

def publishhtml() {
    new com.company.Javadast(this).publishhtml()
}

def notification(String status, String priority, String slackChannel, String emailRecipients) {
    new com.company.Javadast(this).notification(status, priority, slackChannel, emailRecipients)
}
