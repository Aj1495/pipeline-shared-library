def call(String serviceName, String branchName) {
  stage('Docker Build') {
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
            echo "Logging into Docker Registry..."
            docker login -u \$DOCKER_USER -p \$DOCKER_PASS
          """

          echo "Waiting for Docker daemon to be ready..."
          sh '''
            for i in {1..10}; do
              docker info && break
              echo "Docker daemon not ready yet... retrying in 3s"
              sleep 3
            done
          '''
 
          if (branchName in ["staging", "preprod", "production", "master"]) {
            echo "Building Docker image for production like environment ... "
            sh "docker build -t ${serviceName} ./app"
          } else {
            echo "Building Docker image for non-production environment... "
            sh "docker build -t ${serviceName} ./app"
          }
        
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
