plugins {
    java
    `maven-publish`
    `java-library`
    id("io.freefair.lombok") version "6.5.1"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

repositories {
    mavenCentral()
    maven { url = uri("https://sonatype.projecteden.gg/repository/maven-public/") }
    maven { url = uri("https://hub.spigotmc.org/nexus/content/repositories/snapshots/") }
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://oss.sonatype.org/content/groups/public/") }
    maven { url = uri("https://maven.enginehub.org/repo/") }
    maven { url = uri("https://papermc.io/repo/repository/maven-public/") }
    maven { url = uri("https://maven.citizensnpcs.co/repo") }
    maven { url = uri("https://repo.codemc.org/repository/maven-public/") }
    maven { url = uri("https://repo.maven.apache.org/maven2/") }
    maven { url = uri("https://repo.onarandombox.com/content/groups/public/") }
    maven { url = uri("https://repo.dmulloy2.net/nexus/repository/public/") }
    maven { url = uri("https://mvnrepository.com/artifact/org.apache.commons/commons-collections4") }
    maven { url = uri("https://jitpack.io") }
    maven { url = uri("https://repo.codemc.io/repository/maven-public/") }
    maven { url = uri("https://sonatype.projecteden.gg/repository/maven-public/") }
    maven { url = uri("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/") }
    maven {
        url = uri("https://repo.inventivetalent.org/content/groups/public/")
        content { includeGroup("org.inventivetalent") }
    }
}

dependencies {
    paperweightDevBundle("gg.projecteden.parchment", "1.19.4-R0.1-SNAPSHOT")
    implementation("net.kyori:adventure-platform-bukkit:4.3.0")
    implementation("net.kyori:adventure-api:4.13.0")
    implementation("org.projectlombok:lombok:1.18.26")
    implementation("org.mongodb:mongo-java-driver:3.12.12")
    implementation("org.json:json:20230227")
    implementation("org.objenesis:objenesis:3.2")
    implementation("io.github.classgraph:classgraph:4.8.157")
    implementation("dev.morphia.morphia:core:1.6.1")
    implementation("org.apache.commons:commons-collections4:4.4")
    implementation("it.sauronsoftware.cron4j:cron4j:2.2.5")
    implementation("com.squareup.okhttp3:okhttp:3.14.6")
    implementation("dev.dbassett:skullcreator:3.0.1")
    implementation("fr.mrmicky:fastboard:1.2.1")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.7")
    compileOnly("net.citizensnpcs:citizens-main:2.0.30-SNAPSHOT") {
        exclude("*", "*")
    }
    compileOnly("de.tr7zw:item-nbt-api-plugin:2.10.0")
    compileOnly("com.comphenix.protocol:ProtocolLib:4.7.0")
    compileOnly("net.luckperms:api:5.4")
    compileOnly("com.fastasyncworldedit:FastAsyncWorldEdit-Core:2.5.2")
    compileOnly("com.onarandombox.multiversecore:Multiverse-Core:4.3.1")
    compileOnly("com.onarandombox.multiverseinventories:Multiverse-Inventories:4.2.3")
    compileOnly("me.lucko:helper:5.6.13")
    compileOnly("me.lucko:spark-api:0.1-SNAPSHOT")
    compileOnly("com.github.mcMMO-Dev:mcMMO:dc94fedee1")
    compileOnly("org.apache.commons:commons-configuration2:2.8.0")
    compileOnly("com.arcaniax:HeadDatabase-API:1.3.1")
    compileOnly(files("libs/SuperVanish-6.2.6.jar"))
}

group = "co.AegisRPG"
version = "1.0" // keep in sync with plugin.yml

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

tasks {
    assemble {
        dependsOn(reobfJar)
    }

    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.release.set(17)
        options.compilerArgs.add("-parameters")
        options.compilerArgs.add("-Xmaxerrs")
        options.compilerArgs.add("1000")
    }

    javadoc { options.encoding = Charsets.UTF_8.name() }

    processResources {
        filteringCharset = Charsets.UTF_8.name()

        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
