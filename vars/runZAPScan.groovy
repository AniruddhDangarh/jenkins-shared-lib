def call(Map config = [:]) {
    def zapVersion = config.get('version', '2.16.1')
    def targetUrl = config.get('targetUrl', 'http://localhost')
    def reportName = config.get('reportName', 'zap-report.html')
    def zapDir = "${env.WORKSPACE}/ZAP_${zapVersion}"
    def zapUrl = "https://github.com/zaproxy/zaproxy/releases/download/v${zapVersion}/ZAP_${zapVersion}_Linux.tar.gz"
    def reportPath = "${env.WORKSPACE}/${reportName}"

    echo "Installing OWASP ZAP ${zapVersion}..."
    sh """
        mkdir -p ${zapDir}
        wget -q ${zapUrl} -O zap.tar.gz
        tar -xzf zap.tar.gz -C ${env.WORKSPACE}
        rm zap.tar.gz
    """

    sh 'java -version'
    sh "${zapDir}/zap.sh -version"

    echo "Running ZAP DAST scan..."
    sh """
        ${zapDir}/zap.sh -cmd \\
            -port 9090 \\
            -quickurl ${targetUrl} \\
            -quickout ${reportPath} \\
            -quickprogress
    """

    archiveArtifacts artifacts: reportName, allowEmptyArchive: false
}
