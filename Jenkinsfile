library identifier: 'shared@master', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/zero-88/jenkins-pipeline-shared.git'])

pipeline {
    agent {
        docker "gradle:4.10.2-jdk8-alpine"
    }
    environment {
        BUILD_AGENT = "jenkins:${JENKINS_VERSION}-gradle:4.10.2-jdk8-alpine"
    }
    stages {

        stage("Build") {
            steps {
                sh "gradle clean dist -x test -PbuildBy=${BUILD_AGENT} -PbuildNumber=${BUILD_NUMBER} " +
                        "-PbuildHash=${GIT_COMMIT}"
                script {
                    VERSION = sh(script: "gradle properties | grep 'version:' | awk '{print \$2}'", returnStdout: true).trim()
                }
            }
            post {
                success {
                    archiveArtifacts artifacts: "build/distributions/*", fingerprint: true
                    archiveArtifacts artifacts: "build/docs/*", fingerprint: true
                }
            }
        }

        stage("Test") {
            steps {
                sh "gradle test jacocoTestReport"
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'
                    zip archive: true, dir: "build/reports", zipFile: "build/reports/test-reports.zip"
                }
            }
        }

//        stage("Analysis") {
//            steps {
//                script {
//                    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
//                        sh "set +x"
//                        sh "gradle sonarqube -Dsonar.organization=zero-88-github -Dsonar.branch.name=${GIT_BRANCH} " +
//                                "-Dsonar.host.url=https://sonarcloud.io -Dsonar.login=${SONAR_TOKEN}"
//                    }
//                }
//            }
//        }

        stage("Publish") {
            when { tag "v*" }
            steps {
                // TODO: Update later
                echo "Publish"
            }
        }

    }

    post {
        always {
            sh "apk add git"
            githubHookStatus()
            emailNotifications VERSION
        }
    }
}
