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
            echo "Installing docker command"
            sudo apt install docker.io
            echo "Logging into Docker Registry..."
            docker login -u \$DOCKER_USER -p \$DOCKER_PASS
          """
          if (branchName in ["staging", "preprod", "production", "master"]) {
            echo "Building Docker image for production like environment ... "
            sh "docker build -t ${serviceName} ."
            // if there's an 'app' subdirectory or second Dockerfile
            sh "cd app && docker build -t ${serviceName} ."
          } else {
            echo "Building Docker image for non-production environment... "
            sh "docker build -t ${serviceName} ."
          }
        
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
