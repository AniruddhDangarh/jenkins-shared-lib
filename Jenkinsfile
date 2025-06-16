@Library('shared-library') _
import com.company.Javadast

pipeline {
    agent any
    environment {
        // ... your env vars
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
        // Similar updates for other stages
    }
    // Post actions the same
}
