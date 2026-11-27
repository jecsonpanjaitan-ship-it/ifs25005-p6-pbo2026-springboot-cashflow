// Pipeline CI/CD: Build & Test -> Jacoco 100% -> Trivy Scan -> Package -> Publish -> Deploy
pipeline {
    agent any

    options {
        timestamps()
        disableConcurrentBuilds()
    }

    environment {
        APP_NAME = 'ifs25005-p6-pbo2026-springboot-cashflow'
        JAR_FILE = "target/${APP_NAME}-0.0.1-SNAPSHOT.jar"
    }

    stages {
        stage('Build & Test') {
            steps {
                // 'verify' menjalankan unit test + JDBC (H2) lalu aturan Jacoco.
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Jacoco 100%') {
            steps {
                // Gagal bila coverage counter terukur < 100% (view & auth dikecualikan di pom.xml).
                sh 'mvn -B jacoco:check@check'
                archiveArtifacts artifacts: 'target/site/jacoco/**', allowEmptyArchive: true
            }
        }

        stage('Trivy Scan') {
            steps {
                sh 'trivy fs --exit-code 1 --severity HIGH,CRITICAL --no-progress .'
            }
        }

        stage('Package') {
            steps {
                // Profil production wajib agar frontend Vaadin ter-bundle.
                sh 'mvn -B -DskipTests -Pproduction clean package'
            }
        }

        stage('Publish') {
            steps {
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                // Isi DEPLOY_HOST/DEPLOY_USER/DEPLOY_PATH sebagai kredensial/parameter Jenkins.
                sshagent(credentials: ['deploy-ssh']) {
                    sh '''
                        scp -o StrictHostKeyChecking=no "$JAR_FILE" "$DEPLOY_USER@$DEPLOY_HOST:$DEPLOY_PATH/app.jar"
                        ssh -o StrictHostKeyChecking=no "$DEPLOY_USER@$DEPLOY_HOST" "sudo systemctl restart cashflow"
                    '''
                }
            }
        }
    }

    post {
        failure {
            echo 'Pipeline gagal - periksa log stage di atas.'
        }
    }
}
