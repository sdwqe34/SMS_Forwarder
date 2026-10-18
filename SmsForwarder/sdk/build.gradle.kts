plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("maven-publish")
}

group = "com.github.sdwqe34.SMS_Forwarder"
version = System.getenv("VERSION") ?: "1.0.0"

android {
    namespace = "io.github.sdwqe34.smsforwardersdk"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
        consumerProguardFiles("consumer-rules.pro")
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    testImplementation("junit:junit:4.13.2")
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                groupId = "com.github.sdwqe34.SMS_Forwarder"
                artifactId = "sdk"
                version = System.getenv("VERSION") ?: "1.0.0"

                pom {
                    name.set("SMS Forwarder SDK")
                    description.set("Rule matching and multi-channel routing primitives for Android applications.")
                    url.set("https://github.com/sdwqe34/SMS_Forwarder")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("https://opensource.org/licenses/MIT")
                        }
                    }
                    scm {
                        url.set("https://github.com/sdwqe34/SMS_Forwarder")
                        connection.set("scm:git:https://github.com/sdwqe34/SMS_Forwarder.git")
                    }
                }
            }
        }
    }
}
