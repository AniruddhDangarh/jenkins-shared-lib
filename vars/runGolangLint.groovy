def call(Map config = [:]) {
    def repo = config.get('repo', '')
    def branch = config.get('branch', 'main')
    def email = config.get('email', '')
    def golangciVersion = config.get('golangciVersion', 'v1.52.2')
    def reportDir = 'reports'

    pipeline {
        agent any

        environment {
            PATH = "/usr/local/go/bin:${env.WORKSPACE}/bin:${env.PATH}"
            REPORT_DIR = "${reportDir}"
        }

        stages {
            stage('Install golangci-lint') {
                steps {
                    sh """
                        mkdir -p \${WORKSPACE}/bin
                        curl -sSfL https://raw.githubusercontent.com/golangci/golangci-lint/master/install.sh | sh -s -- -b \${WORKSPACE}/bin ${golangciVersion}
                    """
                }
            }

            stage('Checkout') {
                steps {
                    git url: repo, branch: branch
                }
            }

            stage('Install Dependencies') {
                steps {
                    sh 'go mod tidy'
                }
            }

            stage('Create golangci-lint config') {
                steps {
                    writeFile file: '.golangci.yml', text: '''
linters:
  enable:
    - govet
    - errcheck
    - staticcheck

run:
  timeout: 5m

issues:
  exclude-rules:
    - path: _test\\.go
      linters:
        - errcheck
'''
                }
            }

            stage('Run golangci-lint') {
                steps {
                    sh """
                        mkdir -p \${REPORT_DIR}
                        \${WORKSPACE}/bin/golangci-lint run --out-format checkstyle > \${REPORT_DIR}/golangci-report.xml || true

                        echo "===== golangci-lint checkstyle Output ====="
                        cat \${REPORT_DIR}/golangci-report.xml
                        echo "=========================================="
                    """
                }
            }

            stage('Publish Warnings') {
                steps {
                    recordIssues tools: [checkStyle(pattern: "\${REPORT_DIR}/golangci-report.xml")]
                }
            }
        }

        post {
            success {
                echo "Pipeline completed successfully, sending success notification."
                emailext(
                    to: email,
                    subject: "Jenkins Build #\${env.BUILD_NUMBER} - SUCCESS",
                    body: """\
Hello,

The Jenkins build #\${env.BUILD_NUMBER} for job '\${env.JOB_NAME}' has completed successfully.

You can view the build details here:
\${env.BUILD_URL}

Best regards,
Jenkins
"""
                )
            }

            failure {
                echo "Pipeline failed, sending failure notification."
                emailext(
                    to: email,
                    subject: "Jenkins Build #\${env.BUILD_NUMBER} - FAILURE",
                    body: """\
Hello,

The Jenkins build #\${env.BUILD_NUMBER} for job '\${env.JOB_NAME}' has failed.

Please investigate the issue:
\${env.BUILD_URL}

Best regards,
Jenkins
"""
                )
            }

            unstable {
                echo "Pipeline is unstable, sending unstable notification."
                emailext(
                    to: email,
                    subject: "Jenkins Build #\${env.BUILD_NUMBER} - UNSTABLE",
                    body: """\
*WARNING*

The Jenkins build #\${env.BUILD_NUMBER} for job '\${env.JOB_NAME}' has been marked as *UNSTABLE*. This indicates potential issues that need attention.

You can review the golangci-lint report here:
\${env.BUILD_URL}artifact/\${env.REPORT_DIR}/golangci-report.xml

Please address any warnings or errors in the report to ensure stability in future builds.

Best regards,
Jenkins
"""
                )
            }
        }
    }
}
