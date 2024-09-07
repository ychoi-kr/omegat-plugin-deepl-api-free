plugins {
    java
    distribution
    maven
    id("org.omegat.gradle") version "1.5.11"
}

repositories {
    mavenCentral()
    mavenLocal()
}

version = "0.2.1"

omegat {
    version = "6.0.0"
    pluginClass = "kr.ychoi.otplugin.DeepLAPIFree"
}

dependencies {
    implementation("org.omegat:omegat:6.0.0")
    implementation("commons-io:commons-io:2.7")
    implementation("commons-lang:commons-lang:2.6")
    implementation("org.json:json:20240303")
}

distributions {
    main {
        contents {
            from(tasks["jar"], "README.md", "LICENSE.md")
        }
    }
}
