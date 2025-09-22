pipeline { 
  agent any
  options { timestamps(); disableConcurrentBuilds() }

  parameters {
    booleanParam(name: 'MANUAL_DEV_DEPLOY', defaultValue: false, description: 'Check to trigger manual dev deploy')
    booleanParam(name: 'MANUAL_PROD_DEPLOY', defaultValue: false, description: 'Check to trigger manual prod deploy')
    booleanParam(name: 'MANUAL_BACK', defaultValue: false, description: 'Backend only')
    booleanParam(name: 'MANUAL_FRONT', defaultValue: false, description: 'Frontend only')
    booleanParam(name: 'MANUAL_AI', defaultValue: false, description: 'AI Backend only')
    booleanParam(name: 'MANUAL_MYSQL', defaultValue: false, description: 'MySQL only')
  }

  triggers {
    GenericTrigger(
      genericVariables: [
        [key:'GL_EVENT',        value:'$.object_kind'],
        [key:'GL_MR_ACTION',    value:'$.object_attributes.action'],
        [key:'GL_MR_STATE',     value:'$.object_attributes.state'],
        [key:'GL_MR_SOURCE',    value:'$.object_attributes.source_branch'],
        [key:'GL_MR_TARGET',    value:'$.object_attributes.target_branch'],
      ],
      token: 'dev-test',
      printContributedVariables: true,
      printPostContent: true,
      regexpFilterText: '$GL_EVENT:$GL_MR_ACTION',
      regexpFilterExpression: '^merge_request:(mergetest)$'
    )
  }

  environment {
    GIT_URL_HTTPS   = 'https://lab.ssafy.com/s13-blochain-transaction-sub1/S13P21E104.git'
    GIT_CREDS_HTTPS = 'seok'

    GITLAB_BASE        = 'https://lab.ssafy.com'
    GITLAB_PROJECT_ENC = 's13-blochain-transaction-sub1%2FS13P21E104'
    RELEASE_BRANCH     = 'release'
    DEVELOP_BRANCH     = 'develop'

    DOCKER_BUILDKIT = '1'
    COMPOSE_DOCKER_CLI_BUILD = '1'
    COMPOSE_DEV_FILE = 'deploy/docker-compose.dev.yml'
    COMPOSE_PROD_FILE = 'deploy/docker-compose.prod.yml'
    COMPOSE_AI_FILE = 'deploy/docker-compose.ai.yml'
    DEV_CONTAINER = ""
  }

  stages {
    stage('Debug payload') {
      steps {
        sh 'env | sort | sed -n "1,140p"'
      }
    }

    stage('Prepare repo(develop)') {
      when {
        expression {
          params.MANUAL_DEV_DEPLOY || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        checkout([$class:'GitSCM',
          branches: [[name: "*/${env.DEVELOP_BRANCH}"]],
          userRemoteConfigs: [[
            url: env.GIT_URL_HTTPS,
            credentialsId: env.GIT_CREDS_HTTPS,
            refspec: '+refs/heads/develop:refs/remotes/origin/develop'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
      }
    }

    stage('Prepare repo(release)') {
      when {
        expression {
          params.MANUAL_PROD_DEPLOY || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.RELEASE_BRANCH)
          )
        }
      }
      steps {
        checkout([$class:'GitSCM',
          branches: [[name: "*/${env.RELEASE_BRANCH}"]],
          userRemoteConfigs: [[
            url: env.GIT_URL_HTTPS,
            credentialsId: env.GIT_CREDS_HTTPS,
            refspec: '+refs/heads/release:refs/remotes/origin/release'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
      }
    }

    stage('Prepare .env.dev') {
      when {
        expression {
          params.MANUAL_DEV_DEPLOY || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        withCredentials([file(credentialsId: 'ENV_DEV_FILE', variable: 'ENV_DEV_FILE')]) {
          sh '''
            set -eu
            install -m 600 "$ENV_DEV_FILE" deploy/.env.dev
          '''
        }
      }
    }

    stage('Prepare .env.prod') {
      when {
        expression {
          params.MANUAL_PROD_DEPLOY || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.RELEASE_BRANCH)
          )
        }
      }
      steps {
        withCredentials([file(credentialsId: 'ENV_PROD_FILE', variable: 'ENV_PROD_FILE')]) {
          sh '''
            set -eu
            install -m 600 "$ENV_PROD_FILE" deploy/.env.prod
          '''
        }
      }
    }

    stage('Back dev Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_DEV_DEPLOY && params.MANUAL_BACK) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        script {
          sh ''' 
            set -eux

            docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" pull || true
            docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" up -d --build tako_back_dev
          '''
        }
      }
    }

    stage('Front dev Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_DEV_DEPLOY && params.MANUAL_FRONT) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" pull || true
          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" up -d --build tako_front_dev
        '''
      }
    }

    stage('Back prod Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_PROD_DEPLOY && params.MANUAL_BACK) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.RELEASE_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" pull || true
          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" up -d --build tako_back
        '''
      }
    }

    stage('Front prod Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_PROD_DEPLOY && params.MANUAL_FRONT) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.RELEASE_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" pull || true
          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" up -d --build tako_front
        '''
      }
    }

    stage('AI Deploy (compose up)') {
      when {
        expression {
          params.MANUAL_AI || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose -f "$COMPOSE_AI_FILE" pull || true
          docker compose -f "$COMPOSE_AI_FILE" up -d --build tako_ai
        '''
      }
    }

    stage('MySQL dev (compose up)') {
      when {
        expression {
          params.MANUAL_DEV_DEPLOY && params.MANUAL_MYSQL
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.prod -f "$COMPOSE_DEV_FILE" pull || true
          docker compose --env-file deploy/.env.prod -f "$COMPOSE_DEV_FILE" up -d --build mysql_dev
        '''
      }
    }

    stage('MySQL prod (compose up)') {
      when {
        expression {
          params.MANUAL_PROD_DEPLOY && params.MANUAL_MYSQL
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" pull || true
          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" up -d --build mysql_prod
        '''
      }
    }
  }

  post {
    success {
      echo "✅ DEV deploy done (compose up --build)."
    }
    failure {
      echo "❌ DEV deploy failed."
    }
  }
}
