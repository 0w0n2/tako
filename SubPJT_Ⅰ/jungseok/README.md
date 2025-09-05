# 특화 프로젝트 - INFRA

## EC2

- 프리티어 인스턴스 생성 후 정식 EC2 발급 전 개발 환경 구성을 위하여 일요일까지 운영 예정

## GitLab

### 이슈, MR 템플릿 설정

- `.gitlab` 파일 생성 후 `.gitlab/issue_templates`에 이슈 템플릿 저장
- `.gitlab/merge_request_templates`에 MR 템플릿 저장

Issue Template
```Markdown
<!--
# 📢 New Issue (제목을 입력해주세요)

📌 사용 예시:
- 🐞 버그 관련 이슈 → [🐞 BE] 로그인 오류 수정
- ✨ 신규 기능 → [✨ FE] 다크 모드 지원 추가
- 🎨 UI/디자인 → [🎨 FE] 버튼 스타일 개선
- 🛠️ 리팩토링/개발 개선 → [🛠️ BE] 인증 로직 리팩토링
- ⚡ 성능 개선 → [⚡ BE] DB 조회 속도 최적화
- 📄 문서 관련 → [📄 DOCS] API 가이드 업데이트
- 🧪 테스트 → [🧪 TEST] 단위 테스트 추가

⚠️ (괄호) 항목은 모두 지우고 알맞게 작성해주세요.
-->

### 📅 Date

<!-- 작업 시작일을 YYYY.MM.DD 형식으로 입력해주세요 -->

(ex. YYYY.MM.DD)

### 📢 Description

<!-- 작업 내용을 명확하게 설명해주세요 -->

### ✅ Checklist
- [ ] 작업 1
- [ ] 작업 2
- [ ] 작업 3

### 💡 Note

<!-- 참고사항을 적어주세요 (선택사항) -->
```

MR Template
```Markdown
<!--
# 🔧 Develop MR (제목을 입력해주세요)

📌 사용 예시:
[🔧 FE] 로그인 페이지 UI 수정
[🔧 BE] 사용자 인증 로직 리팩토링

⚠️ (괄호) 항목은 모두 지우고 알맞게 작성해주세요.
-->

### 📅 Development Period

<!-- 작업 기간을 YYYY.MM.DD ~ YYYY.MM.DD 형식으로 입력해주세요 -->

(ex. YYYY.MM.DD ~ YYYY.MM.DD)

### 📢 Description

<!-- 작업 내용을 명확하게 설명해주세요 -->

### 🔗 Related Issue

<!-- 연관된 이슈의 링크를 걸어주세요 -->
- #(이슈 번호 입력 후 Enter)

### ✅ PR Checklist

MR이 아래 사항을 충족하는지 확인해주세요:

- [ ] Merge 방향 및 Branch 확인했습니다.
- [ ] 커밋 메시지 컨벤션에 맞게 작성했습니다.
- [ ] 불필요한 파일(로그, 주석, 로컬 파일)을 삭제했습니다.
- [ ] 코드가 정상적으로 동작하는지 테스트했습니다.

### 💡 Note

<!-- 참고사항을 적어주세요 -->

```

## Jenkins

- MR 컨벤션 필터링 구현
  - 로컬에서 ngrok을 통해 로컬 개발 환경에서 인터넷을 통해 웹 애플리케이션에 안전하게 접근하여 실험
- MR 완료 시에 MatterMost로 성공, 실패 여부 메세지 전송
  - Generic Webhook Plugin과 MatterMost Notification Plugin 활용하여 구현

```Jenkinsfile
pipeline { 
  agent any
  options { timestamps(); disableConcurrentBuilds() }

  triggers {
    GenericTrigger(
      genericVariables: [
        [key:'GL_EVENT',        value:'$.object_kind'],
        [key:'GL_MR_ACTION',    value:'$.object_attributes.action'],
        [key:'GL_MR_STATE',     value:'$.object_attributes.state'],
        [key:'GL_MR_TITLE',     value:'$.object_attributes.title'],
        [key:'GL_MR_SOURCE',    value:'$.object_attributes.source_branch'],
        [key:'GL_MR_TARGET',    value:'$.object_attributes.target_branch'],
        [key:'GL_MR_IID',       value:'$.object_attributes.iid'],
        [key:'GL_PROJECT',      value:'$.project.path_with_namespace'],
        [key:'GL_MR_SHA',       value:'$.object_attributes.last_commit.id'],
        [key:'GL_MR_DESC',        value:'$.object_attributes.description'],
        [key:'GL_MR_URL',         value:'$.object_attributes.url'],
        [key:'GL_AUTHOR_USERNAME',value:'$.user.username'],
        [key:'GL_AUTHOR_NAME',    value:'$.user.name']
      ],
      token: 'gitlab-mr',
      printContributedVariables: true,
      printPostContent: true,
      regexpFilterText: '$GL_EVENT:$GL_MR_ACTION',
      regexpFilterExpression: '^merge_request:(open|reopen|merge)$'
    )
  }

  environment {
    GIT_URL_HTTPS   = 'https://lab.ssafy.com/9526yu/cicd_practice.git'
    GIT_CREDS_HTTPS = 'seok'

    // GitLab 설정
    GITLAB_BASE        = 'https://lab.ssafy.com'                // 예: https://gitlab.com 과 동일 개념
    GITLAB_PROJECT_ENC = '9526yu%2Fcicd_practice'               // <namespace>%2F<project>
    STATUS_CONTEXT     = 'jenkins:mr-title-check'               // 커밋 상태의 context 라벨
    AUTO_MERGE         = 'false'                                  // 자동 병합 사용 (끄려면 'false')
    MERGE_ONLY_TO      = 'main'                                  // 이 타깃 브랜치로 들어오는 MR만 자동 병합 (빈 문자열이면 제한 없음)
    RELEASE_BRANCH     = 'release'                                // 릴리스 브랜치 이름 (태그 푸시용)
    DEBUG_MM = 'true'
  }

  stages {
    stage('Debug payload') {
      steps {
        sh 'env | sort | sed -n "1,140p"'
      }
    }

    stage('Checkout source branch (HTTPS)') {
      steps {
        checkout([$class: 'GitSCM',
          branches: [[name: "*/${GL_MR_SOURCE}"]],
          userRemoteConfigs: [[
            url: env.GIT_URL_HTTPS,
            credentialsId: env.GIT_CREDS_HTTPS,
            refspec: '+refs/heads/*:refs/remotes/origin/* +refs/tags/*:refs/tags/*'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
        // 타깃 브랜치도 로컬로 받아두기 (커밋 범위 검사/추후 단계 대비)
        sh '''
          set -eu
          git fetch origin "${GL_MR_TARGET}:${GL_MR_TARGET}" || true
        '''
      }
    }

    stage('Load conventions') {
      steps {
        sh '''
          set -eu

          MR_TITLE_COMMON='^\\[(🔧|☀️|✈️)\\s+(FE|BE|INFRA)\\]\\s+.+$'
          MR_TITLE_DEV='^\\[🔧\\s+(FE|BE|INFRA)\\]\\s+.+$'
          MR_TITLE_HOT='^\\[☀️\\s+(FE|BE|INFRA)\\]\\s+.+$'
          MR_TITLE_REL='^\\[✈️\\s+(FE|BE|INFRA)\\]\\s+.+$'

          RELEASE_REGEX='v[0-9]+\\.[0-9]+\\.[0-9]+(-\\S+)?$'

          {
            echo "MR_TITLE_COMMON='${MR_TITLE_COMMON}'"
            echo "MR_TITLE_DEV='${MR_TITLE_DEV}'"
            echo "MR_TITLE_HOT='${MR_TITLE_HOT}'"
            echo "MR_TITLE_REL='${MR_TITLE_REL}'"
            echo "RELEASE_REGEX='${RELEASE_REGEX}'"
          } > .conventions.env

          echo "[Loaded title conventions]"
          cat .conventions.env
        '''
      }
    }

    stage('Validate MR title') {
      steps {
        withCredentials([usernamePassword(
          credentialsId: env.GIT_CREDS_HTTPS,
          usernameVariable: 'GIT_USER',
          passwordVariable: 'GIT_PASS'
        )]) {
          sh '''
            set -eu
            . ./.conventions.env

            TITLE="${GL_MR_TITLE:-}"
            IID="${GL_MR_IID:-}"
            SHA="${GL_MR_SHA:-}"
            PROJECT_ENC="$(printf '%s' "${GL_PROJECT:-}" | sed 's#/#%2F#g')"

            # 규칙 매칭
            if   echo "$TITLE" | grep -Pq "$MR_TITLE_DEV"; then KIND="develop"
            elif echo "$TITLE" | grep -Pq "$MR_TITLE_HOT"; then KIND="hotfix"
            elif echo "$TITLE" | grep -Pq "$MR_TITLE_REL"; then KIND="release"
            else
              MSG="❌ MR title invalid. Must match one of:\\n- ${MR_TITLE_DEV}\\n- ${MR_TITLE_HOT}\\n- ${MR_TITLE_REL}\\nTitle: ${TITLE}"

              if [ -n "$SHA" ]; then
                curl -sS -X POST -H "PRIVATE-TOKEN: ${GIT_PASS}" \
                  --data-urlencode "state=failed" \
                  --data-urlencode "context=${STATUS_CONTEXT}" \
                  --data-urlencode "description=MR title check failed" \
                  --data-urlencode "target_url=${BUILD_URL:-}" \
                  "${GITLAB_BASE}/api/v4/projects/${PROJECT_ENC}/statuses/${SHA}" || true
              fi

              curl -sS -X POST -H "PRIVATE-TOKEN: ${GIT_PASS}" \
                --data-urlencode "body=${MSG}" \
                "${GITLAB_BASE}/api/v4/projects/${PROJECT_ENC}/merge_requests/${IID}/notes" || true

              curl -sS -X PUT -H "PRIVATE-TOKEN: ${GIT_PASS}" \
                --data-urlencode "state_event=close" \
                "${GITLAB_BASE}/api/v4/projects/${PROJECT_ENC}/merge_requests/${IID}" || true

              exit 3
            fi

            echo "$TITLE" | grep -Pq "$MR_TITLE_COMMON" || { echo "❌ Not matching common pattern"; exit 4; }
            echo "✅ MR title OK ($KIND)"
          '''
        }
      }  
    }


    stage('Report status: success to GitLab') {
      steps {
        withCredentials([usernamePassword(credentialsId: env.GIT_CREDS_HTTPS,
                                          usernameVariable: 'GIT_USER',
                                          passwordVariable: 'GIT_PASS')]) {
          sh '''
            set -eu
            SHA="${GL_MR_SHA:-}"
            [ -n "$SHA" ] || { echo "No MR last_commit SHA to report"; exit 0; }

            CONTEXT="jenkins:mr-title-check"
            DESC="MR title check passed"
            TARGET="${BUILD_URL:-}"

            PROJECT_ENC="$(printf '%s' "${GL_PROJECT:-}" | sed 's#/#%2F#g')"

            curl -sS -X POST \
              -H "PRIVATE-TOKEN: ${GIT_PASS}" \
              --data-urlencode "state=success" \
              --data-urlencode "context=${CONTEXT}" \
              --data-urlencode "description=${DESC}" \
              --data-urlencode "target_url=${TARGET}" \
              "${GITLAB_BASE}/api/v4/projects/${PROJECT_ENC}/statuses/${SHA}" \
              -w "\\nHTTP %{http_code}\\n"
          '''
        }
      }
    }

    // 병합 시에만 레포 준비(체크아웃) → 태그 푸시용
    stage('Prepare repo (only on merge)') {
      when {
        expression {
          ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
          (env.GL_MR_TARGET == env.RELEASE_BRANCH)
        }
      }
      steps {
        checkout([$class:'GitSCM',
          branches: [[name: "*/${GL_MR_TARGET}"]],
          userRemoteConfigs: [[
            url: env.GIT_URL_HTTPS,
            credentialsId: env.GIT_CREDS_HTTPS,
            refspec: '+refs/heads/*:refs/remotes/origin/* +refs/tags/*:refs/tags/*'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
      }
    }

    stage('Tag on merge (title-based version)') {
      when {
        expression {
          ((env.GL_MR_ACTION ?: "") == "merge" || (env.GL_MR_STATE ?: "") == "merged") &&
          (env.GL_MR_TARGET == env.RELEASE_BRANCH)
        }
      }
      steps {
        withCredentials([usernamePassword(credentialsId: env.GIT_CREDS_HTTPS,
                                          usernameVariable: 'GIT_USER',
                                          passwordVariable: 'GIT_PASS')]) {
          sh '''
            set -eu
            . ./.conventions.env
            TITLE="${GL_MR_TITLE:-}"
            TGT="${GL_MR_TARGET:-}"
            IID="${GL_MR_IID:-}"

            echo "$TITLE" | grep -Pq "$MR_TITLE_REL" && IS_REL=1 || IS_REL=0
            VERSION="$( echo "$TITLE" | grep -Po "$RELEASE_REGEX" | head -n1 || true )"
            if [ "$IS_REL" -eq 1 ] && [ -z "${VERSION:-}" ]; then
              echo "ℹ️ release MR but no semantic version in title; using fallback"
            fi
            [ -n "${VERSION:-}" ] || VERSION="release-$(date +%Y%m%d)-!${IID}"
            echo "📦 Tagging: ${VERSION}"

            git config user.email "9526yu@naver.com"
            git config user.name  "jungseokyu"

            BASE_URL="${GIT_URL_HTTPS}"
            case "$BASE_URL" in
              https://*) : ;;
              https//*)   BASE_URL="https://${BASE_URL#https//}";;
              *)          BASE_URL="https://${BASE_URL#https://}";;
            esac
            AUTH_URL="https://${GIT_USER}:${GIT_PASS}@${BASE_URL#https://}"

            if [ ! -d .git ]; then
              git init
              git remote add origin "$AUTH_URL"
            else
              git remote set-url origin "$AUTH_URL"
            fi
            echo "🔗 origin -> $(git remote get-url origin)"

            if ! git ls-remote --heads origin "${TGT}" >/dev/null 2>&1; then
              echo "❌ Remote branch 'origin/${TGT}' does not exist."
              exit 7
            fi

            if git rev-parse --is-shallow-repository >/dev/null 2>&1 && \
              [ "$(git rev-parse --is-shallow-repository)" = "true" ]; then
              git fetch --unshallow origin || true
            fi

            git fetch --prune origin \
              "+refs/heads/${TGT}:refs/remotes/origin/${TGT}" \
              "+refs/tags/*:refs/tags/*"

            git checkout -B "${TGT}" "origin/${TGT}"

            if git show-ref --tags --verify --quiet "refs/tags/${VERSION}"; then
              echo "⚠️ Tag ${VERSION} already exists; skip"
              exit 0
            fi

            git tag -a "${VERSION}" -m "MR !${IID} merged to ${TGT} (${VERSION})"
            git push origin "refs/tags/${VERSION}"
            echo "✅ Tag pushed: ${VERSION}"
          '''
        }
      }
    }
  }

  post {
    success {
      withCredentials([string(credentialsId: 'MM_WEBHOOK', variable: 'MM_WEBHOOK')]) {
        script{
          mattermostSend(
              endpoint: MM_WEBHOOK,                 // ← 플러그인에 웹훅 직접 전달
              color: 'good',
              message: """
---
## :green_frog: **Jenkins Pipeline Success** :green_frog:

### [${env.GL_MR_TITLE ?: 'No title'}](${env.GL_MR_URL ?: env.BUILD_URL})
:pencil2: *Author*: @${env.GL_AUTHOR_USERNAME ?: 'unknown'}
:gun_cat: *Target*: `${env.GL_MR_TARGET ?: ''}`

**배포 URL**: 추후 구현

### Pipeline Success!
---
"""
          )
        }
      }
      echo "✅ MR success by seok."
    }

    failure {
      // 실패 원인 tail은 우리가 쌓아둔 로그 파일이 있으면 그걸 활용
      // (없어도 동작하도록 안전 처리)
      withCredentials([string(credentialsId: 'MM_WEBHOOK', variable: 'MM_WEBHOOK')]) {
        script {
          mattermostSend(
              endpoint: MM_WEBHOOK,
              color: 'danger',  
              message: """
---
## :x: Jenkins Pipeline Failed :x:

### [${env.GL_MR_TITLE ?: 'No title'}](${env.GL_MR_URL ?: env.BUILD_URL})
:pencil2: *Author*: @${env.GL_AUTHOR_USERNAME ?: 'unknown'}
:gun_cat: *Target*: ${env.GL_MR_TARGET ?: ''}

### Emergency! Pipeline Failed!
---
"""
            )
        }
      }
      echo "❌ MR failed."
    }
  }
}
```