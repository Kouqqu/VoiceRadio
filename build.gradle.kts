plugins {
    java
}

group = "com.myname"
version = "1.0"

repositories {
    mavenCentral()
    // Для ядра Paper и файлов Майнкрафта
    maven("https://repo.papermc.io/repository/maven-public/")

    // Для релизов Плазмы и Лаваплеера
    maven("https://repo.plasmoverse.com/releases")
    // SNAPSHOT-библиотеки Плазмы
    maven("https://repo.plasmoverse.com/snapshots")

    // Для Simple Voice Chat API
    maven("https://maven.maxhenkel.de/repository/public")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:26.1.2.build.60-stable")
    compileOnly("su.plo.voice.api:server:2.1.0")
    compileOnly("su.plo:pv-addon-lavaplayer-lib:1.2.1")
    compileOnly("de.maxhenkel.voicechat:voicechat-api:2.5.0")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}
