plugins {
  id("me.roundaround.allay")
}

allay {
  displayName.set("Item Signs")
  description.set("Place items into signs the same way you can with item frames!")
  authors.set(listOf("Roundaround"))
  license.set("MIT")
  homepage.set("https://modrinth.com/mod/item-signs")
  repository.set("https://github.com/Roundaround/mc-fabric-item-signs")
  issues.set("https://github.com/Roundaround/mc-fabric-item-signs/issues")

  modrinth {
    projectId.set("item-signs")
  }

  curseforge {
    // CurseForge numeric project id taken from the README download-tracker badge
    // (img.shields.io/curseforge/dt/1501513). Verify against the CurseForge project page.
    projectId.set(1501513)
  }

  release {
    versionType.set("release")
    sourcesJar.set(true)
  }
}
