def call(String masterBuild) {
  def git_app_repo = scm.userRemoteConfigs[0].url
  def SERVICE_NAME = scm.getUserRemoteConfigs()[0].getUrl().tokenize('/').last().split("\\.")[0]
  def git_app_branch = "main"

  podTemplate(
    label: 'jenkins-agent',
    containers: [
      containerTemplate(
        name: 'docker',
        image: 'docker:20.10.8',
        command: 'cat',
        ttyEnabled: true,
        envVars: [
          envVar(key: 'DOCKER_HOST', value: 'tcp://localhost:2375'),
          envVar(key: 'DOCKER_TLS_CERTDIR', value: ''),
      containerTemplate(
        name: 'helm',
        image: 'alpine/helm:3.13.0',
        command: 'cat',
        ttyEnabled: true,
      ),
      containerTemplate(
        name: 'dind-daemon',
        image: 'docker:20.10.8-dind',
        privileged: true,
        args: '--host tcp://0.0.0.0:2375 --host unix:///var/run/docker.sock',
        envVars: [
          envVar(key: 'DOCKER_TLS_CERTDIR', value: '')
        ]
      )
    ],
    volumes: [
      emptyDirVolume(mountPath: '/var/lib/docker', memory: false)
    ]
  ) {
    node('jenkins-agent') {
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
        container('docker') {
          dockerBuild(SERVICE_NAME, git_app_branch)
        }
      }

      stage('Push Docker Image') {
        container('docker') {
          dockerPush(SERVICE_NAME, git_app_branch)
        }
      }

      stage('Helm Create Manifests') {
        container('helm') {
          createHelmManifests(SERVICE_NAME, git_app_branch)
        }
      }

      stage('Push Kubernetes Manifests') {
        pushK8sManifests(SERVICE_NAME, git_app_branch)
      }
    }
  }
}
