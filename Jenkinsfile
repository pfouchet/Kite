node('java') {
    stage('Checkout') {
        // configure old builds retention
        /* Only keep the 2 most recent builds on branches, and 5 last builds on master. */
        def numToKeepStr
        if (BRANCH_NAME == 'master') {
            numToKeepStr = '5'
        } else {
            numToKeepStr = '2'
        }
        properties([[$class: 'BuildDiscarderProperty',
                     strategy: [$class: 'LogRotator', numToKeepStr: "${numToKeepStr}"]]])

        // Checkout code from repository
        checkout scm
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

        // specific exclusions
        def sonarExclusions = "/src/main/java/com/groupeseb/datastore/controllers/synchronisation/SynchronizationDefaultValues.java," +
                "/src/main/java/com/groupeseb/datastore/controllers/synchronisation/SynchronizationParameter.java," +
                "/src/main/java/com/groupeseb/datastore/flat/**"

        def scannerHome = tool 'SonarQube Scanner AWS';
        withSonarQubeEnv('SonarQube AWS') {
            sh "${scannerHome}/bin/sonar-scanner -Dsonar.projectKey=KITE -Dsonar.sources=. -Dsonar.exclusions=${sonarExclusions}"
        }
    }

}
