plugins {
	`java-library`
	id("net.civmc.civgradle.plugin")
	id("io.papermc.paperweight.userdev") version "1.3.1"
}

civGradle {
	paper {
		pluginName = "WorldBorder"
	}
}

dependencies {
    paperDevBundle("1.18.1-R0.1-SNAPSHOT")
    implementation("com.googlecode.json-simple:json-simple:1.1.1")
    compileOnly("us.dynmap:dynmap-api:2.5")
}