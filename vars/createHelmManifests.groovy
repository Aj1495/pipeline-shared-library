def call(String serviceName, String branchName) {
  stage('Create Helm Manifests') {
    ansiColor('xterm') {
      withCredentials([
        usernamePassword(
          credentialsId: 'dockerCreds',
          usernameVariable: 'DOCKER_USER',
          passwordVariable: 'DOCKER_PASS'
        ),
        usernamePassword(
          credentialsId: 'alb-dns-name',
          usernameVariable: 'ALB',
          passwordVariable: 'ALB_DNS'
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
            sed -i 's/ingress:\\n  enabled: false\\n  className: ""\\n  annotations: {}/ingress:\\n  enabled: true\\n  annotations:\\n    kubernetes.io\\/ingress.class: "alb"\\n    alb.ingress.kubernetes.io\\/scheme: internet-facing\\n    alb.ingress.kubernetes.io\\/healthcheck-path: "node-app"/' ${serviceName}/values.yaml
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
      
