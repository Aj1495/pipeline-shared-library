def call(String serviceName, String branch) {
  ansiColor('xterm') {
    try {
      echo "Deploying ${serviceName} using Helm for branch: ${branch}"
      sh """
        helm upgrade --install ${serviceName}-${branch} ./helm/${serviceName} \
          --namespace default \
          --set image.repository=${serviceName} \
          --set image.tag=latest \
          --create-namespace
      """
    } catch (Exception e) {
      currentBuild.result = 'FAILURE'
      throw e
    }
  }
}
