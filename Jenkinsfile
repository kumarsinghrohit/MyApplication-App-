node("application") {

    checkout scm

    def currentBranchName = env.BRANCH_NAME
    def commitId = sh(returnStdout: true, script: 'git rev-parse HEAD')

    // consider correct branch names in case of open pull requests
    if (env.BRANCH_NAME.startsWith('PR')) {
        currentBranchName = env.CHANGE_BRANCH
        // if there are local changes after a merged master we ned the original branch commitId
        def lastCommitByJenkins = sh(returnStdout: true, script: 'git log -1')
        if(lastCommitByJenkins.contains("Author: Jenkins <nobody@nowhere>")){
            commitId = sh(returnStdout: true, script: 'git rev-parse @~1')
        }
    }

    // retrieve maven settings
    sh "wget -O maven-setting.xml <mynexus>:8081/repository/settings/maven/settings-bremen.xml"

    stage("build") {
        docker.image("<docker-image-repo>/jdk11").inside("--mount type=bind,source=$HOME/.m2,target=/tmp/.m2") {
            echo 'build using maven'
            sh 'mvn -s ./maven-setting.xml clean verify'
        }
    }

    /*stage("SonarQube analysis") {
        docker.image("<docker-image-repo>/jdk11").inside("--mount type=bind,source=$HOME/.m2,target=/tmp/.m2") {
            withSonarQubeEnv() {
                sh "mvn -s ./maven-setting.xml org.sonarsource.scanner.maven:sonar-maven-plugin:3.7.0.1746:sonar -Dsonar.branch.name=${currentBranchName}"
            }
        }
    }*/

    def managerImage;
    def profileImage;

    stage("create docker image") {
        // user maven settings for Bremen
        managerImage = docker.build("MyApplication-App-/manager", "--build-arg MAVEN_SETTINGS=bremen manager/")
        profileImage = docker.build("MyApplication-App-/profile", "--build-arg MAVEN_SETTINGS=bremen profile/")
    }

    stage("deploy docker image to nexus") {
        if (currentBranchName == 'master') {
            withCredentials([string(credentialsId: 'DOCKER_REPOSITORY_URL', variable: 'DOCKER_REPOSITORY_URL')]) {
                docker.withRegistry("${DOCKER_REPOSITORY_URL}", 'dockerUser') {
                    echo "tagging docker my manager(command) image as master.${commitId}"
                    managerImage.push("master.${commitId}")
                    echo "tagging docker my profile(query) image as master.${commitId}"
                    profileImage.push("master.${commitId}")
                }
            }
        } else {
            // use md5 checksum of branchname as tagname
            def branchRef = sh ( script: "echo -n ${currentBranchName} | md5sum | cut -c 1-32", returnStdout: true).trim();

            withCredentials([string(credentialsId: 'DOCKER_REPOSITORY_STORE_TASKS_URL', variable: 'DOCKER_REPOSITORY_STORE_TASKS_URL')]) {
                docker.withRegistry("${DOCKER_REPOSITORY_STORE_TASKS_URL}", 'dockerUser') {
                    echo "tagging docker my manager(command) image as '${branchRef}.${commitId}'"
                    managerImage.push("${branchRef}.${commitId}")
                    echo "tagging docker my profile(query) image as '${branchRef}.${commitId}'"
                    profileImage.push("${branchRef}.${commitId}")
                }
            }
        }
    }
}