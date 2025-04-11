def call(String masterBuild = '') {
  def git_app_repo = scm.userRemoteConfigs[0].url
  def SERVICE_NAME = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0]
  def git_app_branch = "${env.BRANCH_NAME}"

  node('master') {
    properties([
      buildDiscarder(logRotator(numToKeepStr: '5')),
      disableConcurrentBuilds(),
    ])

    stage('CleanWorkspace') {
      cleanWs()
      sh 'whoami && pwd'
    }

    stage('Checkout Code') {
      checkout scm
    }

    stage('Build Docker Image') {
      dockerBuild(SERVICE_NAME)
    }

    stage('Push Docker Image') {
      dockerPush(SERVICE_NAME)
    }

    stage('Helm Deploy') {
      helmDeploy(SERVICE_NAME, git_app_branch)
    }
  }
}
