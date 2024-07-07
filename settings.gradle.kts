pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven(url = "https://repo.osgeo.org/repository/release/")
        google()
        mavenCentral()
        maven(url = "https://repositories.tomtom.com/artifactory/maven")
    }
}

rootProject.name = "VodaKm"
include(":app")
 