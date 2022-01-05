@Library('apim-jenkins-lib@master') _

pipeline {
    agent { label 'apim-macmini-colo-01.lvn.broadcom.net' }
    environment {
        ANDROID_HOME ='/Users/MacMiniAdmin/Library/Android/sdk'
        ADB = '${ANDROID_HOME}/platform-tools/adb'
        GATEWAY_VERSION = 'latest'
        JAVA_HOME = '/Library/Java/JavaVirtualMachines/jdk1.8.0_281.jdk/Contents/Home'
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
