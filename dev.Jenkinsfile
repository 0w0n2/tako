pipeline { 
  agent any
  options { timestamps(); disableConcurrentBuilds() }

  parameters {
    booleanParam(name: 'MANUAL_DEV_DEPLOY', defaultValue: false, description: 'Check to trigger manual dev deploy')
    booleanParam(name: 'MANUAL_PROD_DEPLOY', defaultValue: false, description: 'Check to trigger manual prod deploy')
    booleanParam(name: 'MANUAL_BACK', defaultValue: false, description: 'Backend only')
    booleanParam(name: 'MANUAL_FRONT', defaultValue: false, description: 'Frontend only')
    booleanParam(name: 'MANUAL_AI', defaultValue: false, description: 'AI Backend only')
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

    DOCKER_BUILDKIT = '1'
    COMPOSE_DOCKER_CLI_BUILD = '1'
    COMPOSE_DEV_FILE = 'deploy/docker-compose.dev.yml'
    COMPOSE_PROD_FILE = 'deploy/docker-compose.prod.yml'
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
        sh ''' 
          set -eux

          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" pull || true
          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" up -d --build tako_back_dev
        '''
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

    stage('AI dev Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_DEV_DEPLOY && params.MANUAL_AI) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.DEVELOP_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" pull || true
          docker compose --env-file deploy/.env.dev -f "$COMPOSE_DEV_FILE" up -d --build tako_ai_dev
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

    stage('AI prod Deploy (compose up)') {
      when {
        expression {
          (params.MANUAL_PROD_DEPLOY && params.MANUAL_AI) || (
            ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
            (env.GL_MR_TARGET == env.RELEASE_BRANCH)
          )
        }
      }
      steps {
        sh '''
          set -eux

          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" pull || true
          docker compose --env-file deploy/.env.prod -f "$COMPOSE_PROD_FILE" up -d --build tako_ai
        '''
      }
    }

    stage('Docker Image Push to DockerHub') {
      environment {
        // 커밋 해시(12자리) 기준 태깅, 없으면 manual
        IMG_SHA = "${(env.GL_MR_SHA ?: env.GIT_COMMIT ?: 'manual').take(12)}"
      }
      steps {
        withCredentials([
          usernamePassword(
            credentialsId: 'dockerhub-creds',
            usernameVariable: 'DOCKER_USER',
            passwordVariable: 'DOCKER_PASS'
          )
        ]) {
          script {
            // 서비스별 정의 (경로/도커파일/이미지명)
            def targets = [
              [name: 'backend',  ctx: 'backend',  df: 'S13P21E104/backend-spring/Dockerfile',  image: 'seok1419/takon-backend'],
              [name: 'frontend', ctx: 'frontend', df: 'S13P21E104/frontend-web/Dockerfile', image: 'seok1419/tako-frontend'],
              [name: 'ai',       ctx: 'AI',       df: 'S13P21E104/AI/Dockerfile',       image: 'seok1419/tako-ai']
            ]

            // Docker Hub 로그인(1회)
            sh '''
              set -eu
              echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            '''

            // 각 서비스 빌드 & 푸시
            targets.each { t ->
              echo ">>> Build & Push: ${t.name} -> ${t.image}:${IMG_SHA}"
              sh """
                set -eu
                export DOCKER_BUILDKIT=1

                # 최신 베이스 이미지 반영 시 --pull
                docker build --pull -t "${t.image}:${IMG_SHA}" -f "${t.df}" "${t.ctx}"

                # 롤백 용이하도록 latest 동시 태깅
                docker tag "${t.image}:${IMG_SHA}" "${t.image}:latest"

                # 푸시
                docker push "${t.image}:${IMG_SHA}"
                docker push "${t.image}:latest"
              """
            }

            // 선택: 정리
            sh '''
              docker logout || true
              docker image prune -f || true
            '''
          }
        }
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
