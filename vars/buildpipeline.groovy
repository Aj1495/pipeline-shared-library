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
          envVar(key: 'DOCKER_TLS_CERTDIR', value: '')
        ] // <-- FIXED: closed the envVars list properly here
      ),
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
      emptyDirVolume(m
