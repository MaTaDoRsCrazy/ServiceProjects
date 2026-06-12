plugins {
    kotlin("jvm") version "1.9.24"
    application
}

kotlin {
    jvmToolchain(17)
}

application {
    mainClass.set("ru.practice.desktop.ServiceDeskDesktopAppKt")
}

