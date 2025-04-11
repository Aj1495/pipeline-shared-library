def call(String serviceName, String branchName) {
  stage('Create Helm Manifests') {
    ansiColor('xterm') {
      try {
        sh """ 
          echo "Creating Helm charts for ${serviceName}..."
          rm -rf helm_charts
          mkdir helm_charts
          cd helm_charts
          helm create ${serviceName}
          #Additional commands for templating or customizing values.yaml
        """
      } catch (Exception e) {
        currentBuild.result = 'FAILURE'
        throw e
      }
    }
  }
}
