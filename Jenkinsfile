pipeline {
    agent none
    options {
        gitLabConnection('icm-gitlab')
        gitlabBuilds(builds: ['Build', 'Publish'])
    }
    environment {
        ARTIFACTORY = credentials('ICM_ARTIFACTORY_JENKINSCI')
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
        stage('Publish') {
            agent {
                docker {
                    image "openjdk:11.0.11-jdk"
                }
            }
            when {
                    branch 'master'
            }
            steps {
                updateGitlabCommitStatus name: 'Publish', state: 'running'
                sh "./gradlew publish"
            }
            post {
                failure { updateGitlabCommitStatus name: 'Publish', state: 'failed' }
                success { updateGitlabCommitStatus name: 'Publish', state: 'success' }
            }
        }
    }
}
