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

            #Example: clone your separate manifests repo
            rm -rf k8s-manifests-repo
            git clone https://\$GIT_USER:\$GIT_PASS@github.com/skswami91/k8s-manifests-2025.git k8s-manifests-repo

            #Copy or move Helm output to that repo
            cp -r helm_charts/${serviceName} k8s-manifests-repo/${serviceName}

            cd k8s-manifests-repo
            git config --global user.email "skswami91@gmail.com"
            git config --global user.name "skswami91"
            git add .
            git commit -m "Updating manifests for ${serviceName} - build #${env.BUILD_NUMBER}"
            git push origin main
          """
        } catch (Exception e) {
          currentBuild.result = 'FAILURE'
          throw e
        }
      }
    }
  }
}
