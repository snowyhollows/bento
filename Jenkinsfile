pipeline {
    agent none
    options {
        gitLabConnection('icm-gitlab')
        gitlabBuilds(builds: ['Build'])
    }
    stages {
        stage('Build') {
            agent {
                docker {
                    image "openjdk:11.0.11-jdk"
                }
            }
            steps {
                updateGitlabCommitStatus name: 'Build', state: 'running'
                sh "./gradlew clean build"
            }
            post {
                failure { updateGitlabCommitStatus name: 'Build', state: 'failed' }
                success { updateGitlabCommitStatus name: 'Build', state: 'success' }
            }
        }
    }
}
