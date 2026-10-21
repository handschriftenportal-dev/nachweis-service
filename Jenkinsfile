@Library('Jenkins-Shared-Libraries') _

pipeline_maven {
    jdkInstallation          = 'temurin25'
    githubRepo               = 'github.com/handschriftenportal-dev/nachweis-service'
    githubUser               = 'github_handschriften-dev'
    dockerfileSubpath        = 'hsp-nachweis'
    sourceImageTag           = '1.0.0'
    enableScaCheck           = true
    enableSastCheck          = true
}
