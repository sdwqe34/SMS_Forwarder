import org.gradle.api.publish.maven.MavenPublication

plugins {
    id("com.android.application") version "8.5.2" apply false
    id("org.jetbrains.kotlin.android") version "1.9.24" apply false
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>("smsForwarderApk") {
            groupId = "io.github.sdwqe34"
            artifactId = "sms-forwarder"
            version = "1.4.0"

            artifact(file("app/build/outputs/apk/debug/app-debug.apk")) {
                extension = "apk"
                classifier = "debug"
            }

            pom {
                name.set("SMS Forwarder")
                description.set("Bilingual Android app for selective SMS forwarding with local send history")
                url.set("https://github.com/sdwqe34/SMS_Forwarder")
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/sdwqe34/SMS_Forwarder")
            credentials {
                username = System.getenv("GITHUB_ACTOR").orEmpty()
                password = System.getenv("GITHUB_TOKEN").orEmpty()
            }
        }
    }
}
