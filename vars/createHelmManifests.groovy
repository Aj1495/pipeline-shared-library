def call(String serviceName, String branchName) {
  stage('Create Helm Manifests') {
    ansiColor('xterm') {
      withCredentials([
        usernamePassword(
          credentialsId: 'dockerCreds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )
      ]) {
        try {
          sh """ 
            echo "Creating Helm charts for ${serviceName}..."
            rm -rf helm_charts
            mkdir helm_charts
            cd helm_charts
            helm create ${serviceName}
            sed -i "s|tag:.*|tag: \\"${env.BUILD_NUMBER}\\"|" ${serviceName}/values.yaml
            sed -i "s|repository:.*|repository: \\"${DOCKER_USER}/${serviceName}\\"|" ${serviceName}/values.yaml
            #Additional commands for templating or customizing values.yaml
          """
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
      
