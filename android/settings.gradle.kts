pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Quick Chat"
include(":app")
include(":core:model")
include(":core:crypto")
include(":core:database")
include(":core:network")
include(":feature:auth")
include(":feature:chat")
include(":feature:status")
