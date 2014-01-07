In-Game Wiki Mod
=======

I decided to make this mod because I was feeling that documentation for mods in-game is often bad. I searched for mods that already did what I was planning to make to no avail. However, recently (after ModJam) I've been pointed towards the WikiLink mod. It's a mod that opens a GUI when you press 'i' when hovereing over a block or over an item in a GUI. This then gives you a screen with all the links available to wiki's you can go to. This is a quick, cool and easy way to handle the above described problem. Many thanks to DrEinsteinium for making this mod! A link to the WikiLink mod: http://www.minecraftforum.net/topic/1926671-164-wikilink-30105d-ftb-tooltips-and-more/ . I wish I saw this earlier. 

However, mods (like my own) still need a In-Game Wiki, so I'll develop the mod as an IGW API. Pages will not be automatically updated from the interwebs, modders are responsible for this themselves.




ModJam 3 mod, the In-Game Wiki Mod. This mod adds a Graphical User Interface (GUI) into the game. You can open this with the 'i' key. For now (ModJam) this is hard-coded. In here you can browse and search through info about (currently):
-Blocks & Items
-Entities

If you're hovering over a block in the world when you press the 'i' key you'll automatically be navigated to the page about this block. Likewise for entities.

The info on the pages is retrieved from text files stored in /assets/igwmod/wiki/. For blocks/items the file the mod searches for is named the same as the unlocalized name of the block/item, with either block/ or item/ prefixed. The amount of actual info files currently is not much as of when this mod is submitted to ModJam. The currently included files are there as a proof of concept. The idea is that people (without knowledge of programming) will be able to submit wikipages.

The info in the files can be plain text. However, there are some commands with which you can easily include crafting/furnace recipes, pictures and/or references to other block's/item's pages. All commands must be encased in a pair of '[' and ']'. When '<' and '>' are used in the following examples, this means that the word encased in it (and the '<' and '>') need to be replaced by a right word. 'block/' and 'item/' can be used interchangably. These are the current implemented commands:
-block/<blockName>, item/<itemName> Will create a clickable icon which refers to the item or block's wikipage.
-crafting{aaa,bbb,ccc,a=block/<blockName>,b=item/<itemName>,c=block/<blockName>}block/<result> will create a picture with a crafting recipe, of which the crafting components are clickable. The way of specifying which items go where is similair to how it's done in code. 
 For the people that don't know an example, the crafting recipe of a Piston:
 www
 cic
 crc w=block/wood,c=block/stonebrick,i=item/ingotIron,r=item/redstone  .The real notation should be [crafting{www,cic,crc,w=block/wood,c=block/stonebrick,i=item/ingotIron,r=item/redstone}block/pistonBase]
For items that have multiple items as output do: [crafting{nnn,ncn,nsn,s=item/stick,c=item/coal}item/torch#4]
 The 'www', 'cic', and 'crc' specify the pattern. Then each individual letter gets replaced by the block/item specified, so 'w' gets replaced by 'w=block/wood' so block/wood, which is a Wooden Plank.
-crafting{key=<key>}, This is the same as above, except with this one the RecipeRetrievalEvent is fired with the given key. You can then set the 'recipe' field of this event to whatever recipe you want. This is a code based way of displaying recipes, it saves you the hassle of doing it in the above way, and it is more resistant to recipe updates.
-furnace{block/<ingredient>}block/<result> will create a furnace recipe. To include the sand-->glass recipe:  [furnace{block/sand}block/glass]
-texture/<texture> Will display the texture located at the given location. The size of the texture can be parsed by the following commands, which should be used BEFORE this one.
-theight=<height> adjusts the texture height to the value parsed.
-twidth=<width> adjusts the texture width to the value parsed.
-tsize=<size> adjusts how big the texture will be drawn. An example of drawing a texture is [theigth=16][twidth=16][texture/GuiInfo.png] . This will draw an info 16x16 pixel info icon.

TODO:
-clickable icon command for entities.
-changable keybinding
-support for multiple pages for the same subject (if there's lots of info).
-add GUI navigation (press 'i' in whatever GUI to open the IGW GUI of the right page, just like NEI does it with 'R' and 'U').
-...
