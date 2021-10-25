@Library('apim-jenkins-lib@master') _

pipeline {
    agent { label 'ng2AgentStable' }
    environment {
        ANDROID_HOME ='/Users/qa/Library/Android/sdk'
        ADB = '${ANDROID_HOME}/platform-tools/adb'
        GATEWAY_VERSION = 'latest'
        JAVA_HOME = '/usr/java/jdk1.8.0_275'
    }
    stages {
        stage('Build') {
            steps {
                script {
                    sh './gradlew assembleAndroidTest build'
                }
            }
        }
    }
}