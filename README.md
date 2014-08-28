In-Game Wiki Mod
=======

ModJam 3 mod, the In-Game Wiki Mod. This mod adds a Graphical User Interface (GUI) into the game. You can open this with the 'i' key (by default).

If you're hovering over a block in the world when you press the 'i' key you'll automatically be navigated to the page about this block. Likewise for entities.

The info on the pages is retrieved from text files stored in /assets/igwmod/wiki/<language>/. For blocks/items the file the mod searches for is named the same as the unlocalized name of the block/item, with either block/ or item/ prefixed. The currently included files about Vanilla are there as a proof of concept. However, if you install PneumaticCraft, you'll see a full implementation of IGW-Mod.

The info in the files can be plain text. However, there are some commands with which you can easily include crafting/furnace and even custom recipes, pictures and/or references to other block's/item's pages. A tutorial for this can be found in the mod itself. Make sure to enable 'debug mode' in the mod's config.

=======
Developping with In-Game Wiki Mod
=======
If you want to add In-Game Wiki Mod support to your mod, it's really easy to include the mod to your development environment, as the mod has a maven.

In your build.gradle, add:

	repositories {
		maven {
			name = "IGW"
			url = "http://maven.k-4u.nl/"
		}
	}

	dependencies{
		compile "igwmod:IGW-Mod-1.7.10:1.1.0-13:userdev"
	}

It should be clear that the version number used in the 'compile' is an example, to see which versions you can use, go to http://maven.k-4u.nl/igwmod/IGW-Mod-1.7.10/

=======
Contributing to In-Game Wiki Mod
=======
If you're planning to contribute to the In-Game Wiki mods source, the best thing you can do is clone this github repository, and run 'gradle setupDecompWorkspace idea/eclipse' on the build.gradle file in this repository.

After you've made changes, do a pull request :)
