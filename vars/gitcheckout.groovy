def call(String branch, String repoUrl ) {
    script {
        echo "Checking out branch: ${branch} from repository: ${repoUrl}"
        checkout([
            $class: 'GitSCM',
            branches: [[name: branch]],
            userRemoteConfigs: [[url: repoUrl]]
        ])
    }
}
