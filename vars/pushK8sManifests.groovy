def call(String serviceName, String branchName) {
  stage('Push K8s Manifests') {
    ansiColor('xterm') {
      withCredentials([
        usernamePassword(
          credentialsId: '1360ab06-c1b5-4bc8-bc4d-89977f8400cf',
          usernameVariable: 'GIT_USER',
          passwordVariable: 'GIT_PASS'
        )
      ]) {
        try {
          sh """ 
            echo "Preparing to push Helm/K8s Manifests to separate repo ... "
            git config user.email "skswami91@gmail.com"
            git config user.name "jenkins"

            #Example: clone your separate manifests repo
            rm -rf k8s-manifests-repo
            git clone https://\$GIT_USER:\$GIT_PASS@github.com/k8s-manifests.git k8s-manifests-repo

            #Copy or move Helm output to that repo
            cp -r helm_charts/${serviceName} k8s-manifests-repo/${serviceName}

            cd k8s-manifests-repo
            git add .
            git commit -m "Updating manifests for ${serviceName} - build #${env.BUILD_NUMBER}"
            git push
          """
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
