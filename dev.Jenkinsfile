pipeline { 
  agent any
  options { timestamps(); disableConcurrentBuilds() }

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
      regexpFilterExpression: '^merge_request:(merge)$'
    )
  }

  environment {
    GIT_URL_HTTPS   = 'https://lab.ssafy.com/s13-blochain-transaction-sub1/S13P21E104.git'
    GIT_CREDS_HTTPS = 'seok'

    GITLAB_BASE        = 'https://lab.ssafy.com'
    GITLAB_PROJECT_ENC = 's13-blochain-transaction-sub1%2FS13P21E104'
    RELEASE_BRANCH     = 'release'
    DEVELOP_BRANCH     = 'develop'

    // --- Compose & BuildKit ---
    DOCKER_BUILDKIT = '1'
    COMPOSE_DOCKER_CLI_BUILD = '1'
    COMPOSE_DEV_FILE = 'deploy/docker-compose.dev.yml'
  }

  stages {
    stage('Debug payload') {
      steps {
        sh 'env | sort | sed -n "1,140p"'
      }
    }

    stage('Prepare repo') {
      when {
        expression {
          ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
          (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
        }
      }
      steps {
        checkout([$class:'GitSCM',
          branches: [[name: "*/${GL_MR_TARGET}"]],
          userRemoteConfigs: [[
            url: env.GIT_URL_HTTPS,
            credentialsId: env.GIT_CREDS_HTTPS,
            refspec: '+refs/heads/develop:refs/remotes/origin/develop'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
      }
    }

    stage('Prepare .env.dev') {
      when {
        expression {
          ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
          (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
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

    stage('Build & Deploy (compose up)') {
      when {
        expression {
          ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
          (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
        }
      }
      steps {
        sh '''
          set -eux

          # (옵션) 이미지 참조가 있으면 먼저 pull (build와 혼용 가능)
          docker compose --env-file .env.dev -f "$COMPOSE_DEV_FILE" pull || true

          docker compose --env-file .env.dev -f "$COMPOSE_DEV_FILE" up -d --build tako_backend_dev

          # 상태 출력
          docker compose -f "$COMPOSE_DEV_FILE" ps
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
