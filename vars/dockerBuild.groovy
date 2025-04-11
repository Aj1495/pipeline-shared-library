def call(String serviceName) {
  ansiColor('xterm') {
    try {
      echo "Building Docker image: ${serviceName}"
      sh "docker build -t ${serviceName} ."
    } catch (Exception e) {
      currentBuild.result = 'FAILURE'
      throw e
    }
  }
}
