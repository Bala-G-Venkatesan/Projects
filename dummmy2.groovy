pipeline {
  agent {
    docker {
      image 'abhishekf5/maven-abhishek-docker-agent:v1'
      args '--user root -v /var/run/docker.sock:/var/run/docker.sock'
    }
  }
  parameters {
        string(name: 'SONAR_URL', defaultValue: 'http://default-sonarqube-url:9000', description: 'SonarQube URL')
    }
  stages {
    stage ('CheckOut') {
      steps {
      sh 'echo Passed'
      //git branch 'main', url:'https://github.com/Bala-G-Venkatesan/My-Projects.git'
      }
    }
    stage ('Build and Test')
    { 
      steps {
      sh 'ls -ltr'
      sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn clean package'
      }
    }
    stage ('SonarQube'){
      steps {
        script{
          def SONAR_URL = params.SONAR_URL
          withCredentials([string(credentialID: 'sonarqube', variable: 'SONAR_AUTH_TOKEN')])
          sh 'cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && mvn sonar:sonar -Dsonar.login=${SONAR_AUTH_TOKEN} -Dsonar.host.url=${SONAR_URL}'
      }
    }
    }
    stage ('Build & Push Docker Image'){
      environment {
        DockerImage= "balajidevops1/myjavaspringbootapp:${BUILD_NUMBER}"
        DOCKER_CREDENTIALS= credentials('docker-cred')
      }
      steps {
        script {
        sh "cd java-maven-sonar-argocd-helm-k8s/spring-boot-app && docker build -t ${DockerImage} ."
        def dockerImage = docker.image("${DockerImage}")
        docker.withRegistry('https://index.docker.io/v1/','${DOCKER_CREDENTIALS}'){
          dockerImage.push()
        }
          }
      }

    }
    stage ('Update Deployment File') {
      environment {
        GITHUB_USERNAME = "Bala-G-Venkatesan"
        GITHUB_REPOSITORY = "My-Projects"
      }
      steps {
        withCredentials([string(credentialsId : 'git:hub', variable : 'github_token')])
        sh
          '''
          git config user.email "balamech769806@gmail.com"
          git config user.name "Bala-G-Venkatesan"
          sed -i "s/replaceImageTag/${BUILD_NUMBER}/g" java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
          git add java-maven-sonar-argocd-helm-k8s/spring-boot-app-manifests/deployment.yml
          git commit -m "Updating Manifest File for Kubernetes"
          git push https://${github_token}@github.com/${GITHUB_USERNAME}/${GITHUB_REPOSITORY} HEAD:main
          '''


      }
    }


  }
}