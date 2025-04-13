def call(String serviceName, String branchName) {
  stage('Docker Push') {
    ansiColor('xterm') {
      withCredentials([
        usernamePassword(
          credentialsId: 'dockerCreds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        )
      ]) {
        def imageTag = "${DOCKER_USER}/${serviceName}:${env.BUILD_NUMBER}"

        try {
          sh """ 
          echo "Logging into Docker Registry for push..."
          docker login -u \$DOCKER_USER -p \$DOCKER_PASS

          echo "Tagging image as ${imageTag}"
          docker tag ${serviceName} ${imageTag}

          echo "Pushing Docker image: ${imageTag}"
          docker push ${imageTag}
          """
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
