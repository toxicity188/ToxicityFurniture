plugins {
    id("com.github.johnrengelman.shadow") version("8.1.1")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.20.2-R0.1-SNAPSHOT")
    compileOnly("com.github.toxicity188:QuestAdder:master-SNAPSHOT")
    compileOnly("com.github.toxicity188:ToxicityLibs:master-SNAPSHOT")
    compileOnly("com.ticxo.modelengine:api:R3.1.9")

    implementation(project(":api"))
}