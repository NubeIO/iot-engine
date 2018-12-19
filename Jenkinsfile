library identifier: 'shared@master', retriever: modernSCM(
        [$class: 'GitSCMSource',
         remote: 'https://github.com/zero-88/jenkins-pipeline-shared.git'])

pipeline {
    agent {
        docker "gradle:4.10.3-jdk8-alpine"
    }
    environment {
        BUILD_AGENT = "jenkins:${JENKINS_VERSION}-gradle:4.10.2-jdk8-alpine"
    }
    stages {

        stage("Prepare") {
            steps {
                script {
                    VERSION = sh(script: "gradle properties | grep 'version:' | awk '{print \$2}'", returnStdout: true).trim()
                    BUILD_CMD = BRANCH_NAME ==~ /^master|v.+|PR-.+/ ? 'dist' : 'build'
                }
            }
        }

        stage("Build") {
            steps {
                sh "gradle clean ${BUILD_CMD} -x test -PbuildBy=${BUILD_AGENT} -PbuildNumber=${BUILD_NUMBER} " +
                        "-PbuildHash=${GIT_COMMIT}"
            }
            post {
                success {
                    archiveArtifacts artifacts: "build/distributions/*", fingerprint: true
                    archiveArtifacts artifacts: "build/docs/*", fingerprint: true, allowEmptyArchive: true
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

        stage("Analysis") {
            steps {
                echo "Update Sonar Server"
//                script {
//                    withCredentials([string(credentialsId: 'SONAR_TOKEN', variable: 'SONAR_TOKEN')]) {
//                        sh "set +x"
//                        sh "gradle sonarqube -Dsonar.organization=zero-88-github -Dsonar.branch.name=${GIT_BRANCH} " +
//                                "-Dsonar.host.url=https://sonarcloud.io -Dsonar.login=${SONAR_TOKEN}"
//                    }
//                }
            }
        }

        stage("Publish") {
            when {
                expression { BRANCH_NAME ==~ /^master|v.+/ }
            }
            steps {
                // TODO: Update later
                echo "Publish"
            }
        }

    }

    post {
        always {
            sh "apk add git"
            script {
                currentBuild.result = currentBuild.currentResult
            }
            githubHookStatus()
            emailNotifications(VERSION)
        }
    }
}
