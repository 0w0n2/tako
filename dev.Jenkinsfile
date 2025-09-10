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
        [key:'GL_MR_URL',       value:'$.object_attributes.url'],
        [key:'GL_ASSIGNEE',     value:'$.assignees[0].username'],
        [key:'GL_REVIEWER',     value:'$.reviewers[0].username'],
        [key:'GL_USER_NAME',    value:'$.user.name']
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
    DEVELOP_BRANCH    = 'develop'
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
            refspec: '+refs/heads/*:refs/remotes/origin/* +refs/tags/*:refs/tags/*'
          ]],
          extensions: [[$class:'CloneOption', shallow:true, depth:1, timeout:10]]
        ])
      }
    }

    stage('test server build') {
      when {
      }
      steps {
      }
    }
  }

  post {
    success {
    }

    failure {
    }
  }
}