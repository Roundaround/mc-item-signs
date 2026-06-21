plugins {
  id("me.roundaround.allay")
}

allay {
  displayName.set("Item Signs")
  description.set("Place items into signs the same way you can with item frames!")
  authors.set(listOf("Roundaround"))
  license.set("MIT")
  homepage.set("https://modrinth.com/mod/item-signs")
  repository.set("https://github.com/Roundaround/mc-item-signs")
  issues.set("https://github.com/Roundaround/mc-item-signs/issues")
  logoFile.set("assets/itemsigns/banner.png")

  gametest {
    // Acknowledge the Minecraft EULA for the throwaway worlds the headless
    // server game test spins up.
    eula.set(true)
  }

  modrinth {
    projectId.set("item-signs")
  }

  curseforge {
    projectId.set(1501513)
  }

  release {
    versionType.set("release")
    minecraftVersions("26.2")
    changelogDir.set(file("changelogs"))
  }
}
