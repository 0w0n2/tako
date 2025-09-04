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
      token: 'tcg-mr',
      printContributedVariables: true,
      printPostContent: true,
      regexpFilterText: '$GL_EVENT:$GL_MR_ACTION',
      regexpFilterExpression: '^merge_request:(open|reopen|merge)$'
    )
  }

  environment {
    GIT_URL_HTTPS   = 'https://lab.ssafy.com/s13-blochain-transaction-sub1/S13P21E104.git'
    GIT_CREDS_HTTPS = 'afe61e7f-6900-4334-863f-94560ac3b445'

    GITLAB_BASE        = 'https://lab.ssafy.com'                // 예: https://gitlab.com 과 동일 개념
    GITLAB_PROJECT_ENC = 's13-blochain-transaction-sub1%2FS13P21E104'               // <namespace>%2F<project>
    STATUS_CONTEXT     = 'jenkins:mr-title-check'               // 커밋 상태의 context 라벨
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
        sh '''
          set -eu

          TITLE="${GL_MR_TITLE:-}"
          URL="${GL_MR_URL:-${BUILD_URL:-}}"
          AUTHOR="@${GL_AUTHOR_USERNAME:-unknown}"
          TGT="${GL_MR_TARGET:-}"
          BUILDURL="${BUILD_URL:-}"

          esc() {
            s="${1//\\\\/\\\\\\\\}"
            s="${s//\"/\\\\\"}"
            s="${s//$'\\n'/\\\\n}"
            printf '%s' "$s"
          }

          TITLE_ESC="$(esc "$TITLE")"
          DESC_ESC="$(esc "$DESC")"
          AUTHOR_ESC="$(esc "$AUTHOR")"
          TGT_ESC="$(esc "$TGT")"
          URL_ESC="$(esc "$URL")"

          TEXT='---\\n## *Jenkins Pipeline Success*\\n'\
        '### Title: ['"$TITLE_ESC"']('"$URL_ESC"')\\n'\
        'Author: '"$AUTHOR_ESC"'\\n'\
        'Target: `'"$TGT_ESC"'`\\n'\
        '### MR create(merge) complete!\\n---'

          # ---------- DEBUG 출력 ----------
          if [ "${DEBUG_MM:-false}" = "true" ]; then
            echo "[MM] webhook configured? -> $([ -n "$MM_WEBHOOK" ] && echo yes || echo NO)"
            echo "[MM] channel host -> $(printf '%s\n' "$MM_WEBHOOK" | sed -E 's#https?://([^/]+)/.*#\\1#')"
            echo "[MM] payload preview:"
            printf '%s' "$TEXT" | cut -c1-300; echo
          fi

          # ---------- 호출 & 응답 로그 ----------
          printf '{ "username":"Jenkins", "icon_emoji":":white_check_mark:", "text":"%s" }' "$TEXT" > mm_payload.json

          HTTP_CODE="$(curl -sS -X POST -H "Content-Type: application/json" \
            --data @mm_payload.json \
            --write-out "%{http_code}" --output mm_response.txt "$MM_WEBHOOK" || echo "000")"

          if [ "${DEBUG_MM:-false}" = "true" ]; then
            echo "[MM] HTTP_CODE=$HTTP_CODE"
            echo "[MM] response body ↓"
            sed -n '1,120p' mm_response.txt || true
          fi

          # 2xx가 아니면 실패 로그만 남기고 파이프라인은 계속
          case "$HTTP_CODE" in
            2??) echo "[MM] sent successfully." ;;
            *)   echo "[MM] send failed (HTTP $HTTP_CODE)"; fi
        '''
      }
      echo "✅ MR success by seok."
    }

    failure {
      withCredentials([string(credentialsId: 'MM_WEBHOOK', variable: 'MM_WEBHOOK')]) {
        script {
          def tailLines = 80
          try {
            def tail = currentBuild.rawBuild.getLog(tailLines).join('\n')
            writeFile file: 'build_error_tail.txt', text: tail
          } catch (e) {
            writeFile file: 'build_error_tail.txt', text: "No tail available: ${e.toString()}"
          }
        }
        sh '''
          set -eu

          TITLE="${GL_MR_TITLE:-}"
          URL="${GL_MR_URL:-${BUILD_URL:-}}"
          AUTHOR="@${GL_AUTHOR_USERNAME:-unknown}"
          TGT="${GL_MR_TARGET:-}"
          ERR_TAIL="$(cat build_error_tail.txt || echo '')"

          esc() {
            s="${1//\\\\/\\\\\\\\}"
            s="${s//\"/\\\\\"}"
            s="${s//$'\\n'/\\\\n}"
            printf '%s' "$s"
          }

          TITLE_ESC="$(esc "$TITLE")"
          AUTHOR_ESC="$(esc "$AUTHOR")"
          TGT_ESC="$(esc "$TGT")"
          URL_ESC="$(esc "$URL")"
          ERR_ESC="$(esc "$ERR_TAIL")"

          TEXT='---\\n## :x: *MR Failed* :x:\\n'\
        '### ['"$TITLE_ESC"']('"$URL_ESC"')\\n'\
        'Author: '"$AUTHOR_ESC"'\\n'\
        'Target: `'"$TGT_ESC"'`\\n'\
        '**Error Tail**:\\n```'"$ERR_ESC"'```\\n'\
        '### MR Failed....\\n---'

          curl -sS -X POST -H "Content-Type: application/json" \
            -d "{ \\"username\\": \\"Jenkins\\", \\"icon_emoji\\": \\":x:\\", \\"text\\": \\"${TEXT}\\" }" \
            "$MM_WEBHOOK" || true
        '''
      }

      withCredentials([usernamePassword(credentialsId: env.GIT_CREDS_HTTPS,
                                        usernameVariable: 'GIT_USER',
                                        passwordVariable: 'GIT_PASS')]) {
        sh '''
          set -eu
          SHA="${GL_MR_SHA:-}"
          [ -n "$SHA" ] || exit 0

          CONTEXT="jenkins:mr-title-check"
          DESC="MR title check failed"
          TARGET="${BUILD_URL:-}"
          PROJECT_ENC="$(printf '%s' "${GL_PROJECT:-}" | sed 's#/#%2F#g')"

          curl -sS -X POST \
            -H "PRIVATE-TOKEN: ${GIT_PASS}" \
            --data-urlencode "state=failed" \
            --data-urlencode "context=${CONTEXT}" \
            --data-urlencode "description=${DESC}" \
            --data-urlencode "target_url=${TARGET}" \
            "${GITLAB_BASE}/api/v4/projects/${PROJECT_ENC}/statuses/${SHA}" \
            -w "\\nHTTP %{http_code}\\n" || true
        '''
      }
      echo "❌ MR failed."
    }
  }
}