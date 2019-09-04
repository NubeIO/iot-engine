library identifier: 'shared@master', retriever: modernSCM(
    [$class: 'GitSCMSource',
     remote: 'https://github.com/zero-88/jenkins-pipeline-shared.git'])

pipeline {
    agent {
        // docker "gradle:4.10.3-jdk8-alpine"
        docker "gradle:4.10.3-jdk8"
    }
    environment {
        BUILD_AGENT = "ci-jenkins:${JENKINS_VERSION}"
    }
    stages {

        stage("Prepare") {
            steps {
                script {
                    VERSION =
                        sh(script: "gradle properties | grep 'version:' | awk '{print \$2}'", returnStdout: true).trim()
                    BUILD_CMD = BRANCH_NAME ==~ /^master|v.+|PR-.+/ ? 'dist' : 'build'
                }
            }
        }

        stage("Build") {
            steps {
                sh "gradle clean jooq ${BUILD_CMD} -x test -PbuildBy=${BUILD_AGENT} -PbuildNumber=${BUILD_NUMBER} " +
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
//                 sh "gradle -PexcludeTests=**/Postgres*Test* test jacocoTestReport --stacktrace"
                sh "gradle -PexcludeTests=**/Postgres*Test* test --stacktrace"
            }
            post {
                always {
                    junit 'build/test-results/**/*.xml'
                    zip archive: true, dir: "build/reports", zipFile: "build/reports/test-reports.zip"
                }
            }
        }

        stage("Docker") {
            steps {
                script {
                    withCredentials([string(credentialsId: "gcr-nubeio-ci", variable: "GCR_JSON_PWD"),
                                     string(credentialsId: "gcr-nubeio-project", variable: "GCR_PROJECT")]) {
                        sh "set +x"
                        sh "gradle -x test dockerPublish -PvcsBranch=${BRANCH_NAME} -PbuildNumber=${BUILD_NUMBER} " +
                           "-PdockerHost=unix:///var/run/docker.sock -PdockerRegistryUrl=gcr.io " +
                           "-PdockerRegistryUser=_json_key -PdockerRegistryPwd='${GCR_JSON_PWD}' " +
                           "-PdockerRegistryProject=${GCR_PROJECT}"
                    }
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
            script {
                currentBuild.result = currentBuild.currentResult
            }
            githubHookStatus()
            emailNotifications(VERSION)
        }
    }
}
