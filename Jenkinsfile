// Syntax: https://www.jenkins.io/doc/book/pipeline/syntax/

pipeline {

  agent {
    label 'java8'
  }

  environment {
    JAVA_HOME = "${tool 'JDK-1.7u121'}"
    PATH = "${env.JAVA_HOME}/bin:${env.PATH}"
  }

  options {
    skipDefaultCheckout()
    buildDiscarder(
      logRotator(
        artifactDaysToKeepStr: '30',
        artifactNumToKeepStr: '2',
        daysToKeepStr: '',
        numToKeepStr: '5'
      )
    )
    parallelsAlwaysFailFast()
    timeout(120)
  }

  stages {
    stage('Checkout') {
      steps {
        cleanWs()
        checkout(scm)
      }
    }

    stage('Build') {
      when {
        changeRequest()
      }
      steps {
        withMaven(
          maven: 'Maven3',
          mavenSettingsConfig: 'seb-nexus-aws-config'
        ) {
          sh '''
            mvn install \
                -Dmaven.test.failure.ignore=true
          '''
        }
        junit '**/target/*-reports/*.xml'
      }
    }

    stage('Code Analysis') {
      when {
        changeRequest()
      }
      environment {
        // Key visible in SonarQube dashboard
        JAVA_HOME           = "${tool 'JDK-11'}"
        PATH                = "${env.JAVA_HOME}/bin:${env.PATH}"
        REPO_NAME           = 'Kite'
        PROJECT             = "KITE"
        EXCLUSIONS          = ""
        COVERAGE_EXCLUSIONS = ""
        SCANNER_HOME        = "${tool 'SonarQube Scanner AWS'}"
        VERSION             = "${readMavenPom().getVersion()}"
      }
      steps {
        withSonarQubeEnv('SonarQube AWS') {
          sh '''
            ${SCANNER_HOME}/bin/sonar-scanner \
              -Dsonar.scm.provider=git \
              -Dsonar.projectKey="${PROJECT}" \
              -Dsonar.projectVersion="${VERSION}" \
              -Dsonar.java.source=7 \
              -Dsonar.sources=src \
              -Dsonar.java.binaries=target/classes,target/test-classes \
              -Dsonar.java.libraries=target/dependency \
              -Dsonar.exclusions="${EXCLUSIONS}" \
              -Dsonar.coverage.exclusions="${COVERAGE_EXCLUSIONS}" \
              -Dsonar.dependencyCheck.reportPath=target/dependency-check-report.xml \
              -Dsonar.dependencyCheck.htmlReportPath=target/dependency-check-report.html \
              -Dsonar.pullrequest.key="${CHANGE_ID}" \
              -Dsonar.pullrequest.branch="${CHANGE_BRANCH}" \
              -Dsonar.pullrequest.base=$"{CHANGE_TARGET}"\
              -Dsonar.pullrequest.github.repository=groupeseb/"${REPO_NAME}" \
              -Dsonar.pullrequest.github.endpoint=https://api.github.com/ \
              -Dsonar.pullrequest.provider=GitHub
          '''
        }
      }
    }

    stage('Release') {
      when {
        buildingTag()
      }
      environment {
        GIT = credentials('25b87f15-0c13-4b33-91b5-b0bb3b33a721')
      }
      steps {
        sh "mkdir ${WORKSPACE}/.m2"
        withMaven(
          maven: 'Maven3',
          mavenSettingsConfig: 'seb-nexus-aws-config',
          mavenLocalRepo: "${WORKSPACE}/.m2"
        ) {
          sh '''
            mvn --batch-mode clean

            mvn --batch-mode dependency:purge-local-repository \
                -DreResolve=false \
                -DactTransitively=false

            mvn --batch-mode versions:set \
                -DnewVersion="${TAG_NAME:1}"

            mvn --batch-mode deploy \
                -Dall \
                -DskipTests \
                -Dskip.it.tests=true \
                -PspecialReleaseProfile \
                -PspecialRelease
            # splits version, add +1 to last element
            # and joins the array back
            IFS='.' read -ra ARRAY <<< "${TAG_NAME:1}"
            LAST_ELEM=$( expr ${#ARRAY[@]} - 1 )
            ARRAY[${LAST_ELEM}]=$( expr ${ARRAY[${LAST_ELEM}]} + 1 )
            NEW_VERSION=$( tr ' ' '.' <<< ${ARRAY[@]} )
            git config credential.helper \
              '!f() { sleep 1; echo "username=${GIT_USR}"; echo "password=${GIT_PSW}"; }; f'
            git config --replace-all remote.origin.fetch '+refs/heads/*:refs/remotes/origin/*'
            git config user.name "Jenkins"
            git config user.email "FCTIS-ITDCPIOT-DevOps@groupeseb.com"
            git config push.default simple
            git fetch --all
            git branch -r \
            | grep -v -- '->' \
            | while read remote; do \
                git branch --track "${remote#origin/}" "$remote"; \
              done
            PARENT_BRANCH=$(git branch --contains ${TAG_NAME} \
                  | tail -n 1 \
                  | sed -e 's/ //g')
            git checkout ${PARENT_BRANCH}
            mvn --batch-mode versions:set \
                -DnewVersion=${NEW_VERSION}-SNAPSHOT
            
            git status
            git commit -am "Set pom version to ${NEW_VERSION}-SNAPSHOT"
            git push

          '''
        }
      }
    }
  }
  post {
    cleanup {
      cleanWs()
    }
  }
}
