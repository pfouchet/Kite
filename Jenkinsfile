node('java') {
    def isPRBuild
    try {
        stage('Checkout') {
            // configure old builds retention
            /* Only keep the 2 most recent builds on branches, and 5 last builds on master. */
            def numToKeepStr
            if (BRANCH_NAME == 'master') {
                numToKeepStr = '5'
            } else {
                numToKeepStr = '2'
            }
            properties([[$class  : 'BuildDiscarderProperty',
                         strategy: [$class: 'LogRotator', numToKeepStr: "${numToKeepStr}"]]])

            // Checkout code from repository
            checkout scm
        }

        stage('Prepare') {
            // Check if the current build concerns a PR or a branch
            try {
                def prNumber = CHANGE_ID
                // the CHANGE_ID environment variable exists: we are building a PR
                isPRBuild = true
            } catch (MissingPropertyException e) {
                // we are building a branch and not a PR
                isPRBuild = false
            }

        }

        stage('Build') {
            // Get the JDK7 to build
            env.JAVA_HOME = "${tool 'JDK-1.7u121'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            // choose maven goal using the branch : only snapshot from Master are deployed
            def mvnGoal
            if (BRANCH_NAME == 'master') {
                mvnGoal = "deploy"
            } else {
                mvnGoal = "install"
            }

            // Run the maven build, in a try/finally to ensure tests reports are published even if maven tests failed
            try {
                withMaven(
                        maven: 'Maven3',
                        mavenSettingsConfig: 'seb-nexus-aws-config') {

                    sh "mvn clean ${mvnGoal}"
                }
            } finally {
                // publish test results
                step([$class: 'JUnitResultArchiver', testResults: '**/target/*-reports/*.xml'])
            }
        }

        stage('Code analysis') {
            // use JDK8 to run Sonar, since JDK7 support has been dropped down
            env.JAVA_HOME = "${tool 'JDK-1.8u111'}"
            env.PATH = "${env.JAVA_HOME}/bin:${env.PATH}"

            def scannerHome = tool 'SonarQube Scanner AWS';
            def sonarQubePRArguments = ""
            if (isPRBuild) {
                echo "Building a change request : running SonarQube analysis in preview mode."
                withCredentials([string(credentialsId: 'github-sonar-api-token', variable: 'GITHUB_TOKEN')]) {
                    // The preview mode allows not to override the master analysis (results are separated from the main project)
                    // OAuth token is generated through GitHub: https://help.github.com/articles/creating-a-personal-access-token-for-the-command-line/
                    sonarQubePRArguments = " -Dsonar.analysis.mode=preview -Dsonar.github.oauth=${GITHUB_TOKEN} -Dsonar.github.repository=groupeseb/Kite -Dsonar.github.pullRequest=" + CHANGE_ID
                }
            }
            withSonarQubeEnv('SonarQube AWS') {
                sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=KITE -Dsonar.sources=. ${sonarQubePRArguments}"
            }
        }
    } finally {
        // the notify stage is in a finally block to be executed even if the build failed
        stage('Notify') {
            // if the build status is null, then the build is successfull
            def buildStatus = currentBuild.result ?: "SUCCESS"

            if (isPRBuild) {
                echo "Building a change request : not sending emails."
            } else {
                // we are not in a PR : sending emails if the build is failed or unstable
                if (buildStatus != "SUCCESS") {
                    emailext(
                            subject: "${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - ${buildStatus}!",
                            body: "${env.JOB_NAME} - Build # ${env.BUILD_NUMBER} - ${buildStatus}:\n\nCheck console output at ${env.BUILD_URL} to view the results.",
                            to: "FctIs-DCPBackTeam@groupeseb.com"
                    )
                }
            }
        }
    }

}
