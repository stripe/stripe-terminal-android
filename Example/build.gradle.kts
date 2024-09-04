if (!project.hasProperty("EXAMPLE_BACKEND_URL")) {
    error("You must specify EXAMPLE_BACKEND_URL in gradle.properties")
}

ext {
    set("minSdkVersion", 26)
    set("latestSdkVersion", 35)
}

tasks.register<Delete>("clean") {
    delete(project.layout.buildDirectory)
}
