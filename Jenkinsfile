@Library('apim-jenkins-lib@master') _

pipeline {
    agent { label 'ng2AgentStable' }
    parameters {
        choice(name: 'PUBLISH', choices: ['yes', 'no'], description: 'whether to publish artifact')
        string(name: 'PUBLISH_FILER_LOCATION', defaultValue: '/mnt/../../../../../../Volumes/wip/mag/MobileSDK-Release-Binaries/Android/', description: 'Filer location to publish the  artifacts')
		string(name: 'env.zip_name', defaultValue: 'MobileSDK', description: 'zip folder name')
		string(name: 'env.module', defaultValue: 'all', description: 'all')
		string(name: 'env.BUILD_PATH', defaultValue: '/Users/qa/Documents/iOSRepos/buildScript', description: 'path')
		string(name: 'env.ANDROID_HOME', defaultValue: '/Users/qa/Library/Android/sdk', description: 'path')
		string(name: 'sdk_version', defaultValue: '2.2.00', description: 'Configuration parameter')
    }
    environment {
        JAVA_HOME = '/usr/java/jdk1.8.0_275'
        PUBLISH_FILER_LOCATION = "${PUBLISH_FILER_LOCATION}"
    }
    stages {
        //Clean Filer3 Folder and the local foler
        stage('FolderClean') {
            steps {
                script {
                        withCredentials([usernamePassword(credentialsId: 'ARTIFACTORY_GW_APIKEY', passwordVariable: 'APIKEY', usernameVariable: 'USERNAME')]) {
							//Delete and Re-create local SDK folder
							sh "rm -rf ${env.zip_name}"
                            sh "sudo mkdir ${env.zip_name}"

                            //Clear MobileSDK-Release-Binaries folder on filer4
                            sh "cd ../../../../../../Volumes/wip/mag/MobileSDK-Release-Binaries/Android/[ -d ${sdk_version} ] && echo "Directory Exists" || mkdir ${sdk_version}"
                            sh "cd ${sdk_version}/"
                            sh "rm -rf *"
                    }
                }
            }
        }
        stages {
        //Build Android SDK - Gradle and copy to MobileSDK folder
        stage('Build Android SDK - Gradle') {
            steps {
                script {
                        withCredentials([usernamePassword(credentialsId: 'ARTIFACTORY_GW_APIKEY', passwordVariable: 'APIKEY', usernameVariable: 'USERNAME')]) {
                            sh "cd Android/Android-MAS-SDK"
                            sh "cat /dev/null > settings.gradle"
                            sh "printf \"include ':mas', ':masui', ':mas-connecta', ':mas-storage',  ':mas-foundation', ':mas-test'\" > settings.gradle"
                            sh "./gradlew build"
                            //# Copy binaries to local folder MobileSDK
                            sh "cp mas-foundation/build/outputs/aar/mas-foundation-release.aar ../../${env.zip_name}/mas-foundation-${sdk_version}.aar"
                            sh "cp mas-connecta/build/outputs/aar/mas-connecta-release.aar ../../${env.zip_name}/mas-connecta-${sdk_version}.aar"
                            sh "cp mas-storage/build/outputs/aar/mas-storage-release.aar ../../${env.zip_name}/mas-storage-${sdk_version}.aar"
                            sh "cp masui/build/outputs/aar/masui-release.aar ../../${env.zip_name}/masui-${sdk_version}.aar"
                            //Zip and Copy javadoc to folder MobileSDK
                            sh "cd docs"
                            sh "zip -r mas_foundation_javadoc.zip mas_foundation_javadoc/"
                            sh "zip -r mas_storage_javadoc.zip mas_storage_javadoc/"
                            sh "zip -r mas_connecta_javadoc.zip mas_connecta_javadoc/"

                            sh "sudo cp *.zip ../../../${env.zip_name}/"
							
                    }
                }
            }
        }
        stage('Publish') {
            when {
                expression { params.PUBLISH == 'yes' }
            }
            steps {
                    dir("build/libs") {
                        script {
                            //Copy all the SDK binaries to filer4
                            sh "sudo cp -R ${env.zip_name}/* ../../../../../../Volumes/wip/mag/MobileSDK-Release-Binaries/Android/${sdk_version}/"                       
                        }
                    }
                archiveArtifacts 'build/libs/*.*'
                archiveArtifacts 'build/test-results/test/*.*'
            }
        }
    }
    post {
        success {
            script {
                // 15. send commit status to repo when the build is a pull request
                if (env.CHANGE_ID) {
                    pullRequest.createStatus(status: 'success',
                            context: 'continuous-integration/jenkins/pr-merge',
                            description: 'Build Success',
                            targetUrl: "${env.JOB_URL}/testResults")
                }
            }
        }
        failure {
            script {
                if (env.CHANGE_ID) {
                    pullRequest.createStatus(status: 'failure',
                            context: 'continuous-integration/jenkins/pr-merge',
                            description: 'Build Failed',
                            targetUrl: "${env.JOB_URL}/testResults")
                }
            }
        }
    }
}
