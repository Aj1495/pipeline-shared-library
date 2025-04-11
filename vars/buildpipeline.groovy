def call(String masterBuild) {
  def git_app_repo = scm.userRemoteConfigs[0].url
  def SERVICE_NAME = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0]
  def git_app_branch = "main"

  node() {
    properties([
      buildDiscarder(logRotator(numToKeepStr: '5')),
      disableConcurrentBuilds(),
    ])

    stage('CleanWorkspace') {
      sh 'whoami && pwd'
    }

    stage('Checkout Code') {
      checkout([
        $class: 'GitSCM',
        branches: [[name: "*/${git_app_branch}"]],
        doGenerateSubmoduleConfigurations: false,
        extensions: [
          [$class: 'LocalBranch', localBranch: "${git_app_branch}"]
        ],
        submoduleCfg: [],
        userRemoteConfigs: [
          [credentialsId: '1360ab06-c1b5-4bc8-bc4d-89977f8400cf', url: "${git_app_repo}"]
        ]
      ])
    }

    /* 
    * Call your shared library functions, each in its own stage
    */

    stage('Build Docker Image') {
      dockerBuild(SERVICE_NAME, git_app_branch)
    }

    stage('Push Docker Image') {
      dockerPush(SERVICE_NAME, git_app_branch)
    }

    stage('Helm Create Manifests') {
      createHelmManifests(SERVICE_NAME, git_app_branch)
    }
    stage('Push Kubernetes Manifests') {
      pushK8sManifests(SERVICE_NAME, git_app_branch)
    }
  }
}
