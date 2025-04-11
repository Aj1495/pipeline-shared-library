def call(String serviceName) {
  ansiColor('xterm') {
    try {
      echo "Pushing Docker image: ${serviceName}"
      withCredentials([usernamePassword(credentialsId: 'dockerhub-creds', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASS')]) {
        sh """
          echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
          docker tag ${serviceName} ${DOCKER_USER}/${serviceName}:latest
          docker push ${DOCKER_USER}/${serviceName}:latest
        """
      }
    } catch (Exception e) {
      currentBuild.result = 'FAILURE'
      throw e
    }
  }
}
