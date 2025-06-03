def call(String targeturl){
                sh """
                    zaproxy -cmd \
                    -port 8090 \
                    -quickurl ${targeturl} \
                    -quickout ${WORKSPACE}/zap-report.html \
                    -quickprogress
                    """
}
