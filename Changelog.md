# PneumaticCraft: Repressurized - Changelog

This is an overview of significant new features and fixes by release.  See https://github.com/TeamPneumatic/pnc-repressurized/commits/master for a detailed list of all changes.

Changes are in reverse chronological order; newest changes at the top.

## Minecraft 1.14.4

This release brings a very major internal rewrite and many many major new and modified gameplay elements. See also https://gist.github.com/desht/b604bd670f7f718bb4e6f20ff53893e2

## 1.0.2-11 (May 4 2020)

This release contains various fixes and small improvements backported from the 1.15.2 release

### Updates
* Added fluid tags for most PneumaticCraft fluid (tag names same as fluid name, e.g. "pneumaticcraft:lpg" fluid tag contains "pneumaticcraft:lpg" by default)
  * Also updated Refinery and Thermopneumatic Processing Plant recipes to use fluid tags instead of fluids directly
  * Makes it easier to support other fluids (e.g. for the Refinery to use another mod's oil, add that mod's "mod:oil" fluid to "pneumaticcraft:oil" tag in your datapack; no need to add a whole new recipe now)
* Increased Jet Boots speed a little, back to 1.12.2 levels
* Air Grate uses less air now, and only when actually pushing/pulling entities
* A few texture tweaks to some items and blocks
* A camouflaged elevator now renders the camo on the elevator floor at all times (not just when the elevator is at the bottom)
* Removed experience drop for smelting Plastic Construction Bricks™ back to Plastic Sheets (much too easy to exploit for infinite exp)
* Drones no longer use their owner's UUID for their fake player.
  * While this was convenient for protection mods, it introduced some subtle problems, where the server associated a player's UUID with a fake player object instead of the real player.  The most obvious effect of this was advancements often not working.
  * Protection mods should now use "<playername>_drone" to permit a given player's drones.

### Fixes
* Fixed fluids not syncing in the Refinery GUI (sync issue only - Refinery was working properly serverside)
* Fixed misleading tooltip on non-PneumaticCraft fluid buckets (e.g. Silent's Gems oil bucket) suggesting it could be used in the Refinery. It can't by default (datapack is needed to add the fluid to appropriate tag, i.e. "pneumaticcraft:oil")
* Pressure Chamber Interface door now shrinks as it opens, so it doesn't overlap adjacent blocks (which looked dumb when the adjacent block was Pressure Glass...)
* Fixed possible crash in tube module syncing due to client trying to send a packet intended for server-client usage
* Sentry Turret now renders its bullet tracers when the block itself is off-screen
* Fixed multiblock elevators sounding much too loud
* Logistics advancements no longer require plastic to be unlocked (since logistics items no longer require plastic...)

## 1.0.1-10 (Apr 17 2020)

### Fixes
* Fixed Block Tracker behaviour (performance and crashes) with Hackables
  * Also, Block Tracker now picks up blocks added by block tag (doors & buttons)
* Fixed client crash (NoSuchMethodError) when pressing Return (insert line) in an Aphorism Tile gui, when on dedicated server
* Fixed Elevator not always rendering when extended
* Fixed Gas Lift: block model now only shows tube connectors where connected, and GUI now shows fluid in the tank
* Fixed Air Cannon not being able to fling entities when Entity Tracker upgrade is installed
* Refinery Output block now has the right block shape

## 1.0.0-8 (Mar 22 2020)

### Known issues
* On dedicated server, JEI may not show custom machine recipes when you log in. 
  * If this happens, a "F3+T" asset reload will fix the problem.
  * A timing issue as far as I can tell (JEI processes recipes before the client gets custom recipes from the server); don't have a fix for it yet.
* JEI currently doesn't show recipes for a few special crafting operations (but the recipes themselves should work fine):
  * Drone colouring (drone + dye)
  * One Probe helmet (Pneumatic Helmet & Probe)
  * Minigun potion ammo (regular ammo + any potion)
  * Crafting Patchouli guide (book + compressed iron ingot)
  * Drone upgrade crafting (any basic drone + Printed Circuit Board)
* Mouse-wheel scrolling GUI side tabs with scrollbars doesn't work if JEI is active and has icons on that side of the GUI
  * Mousing over the scrollbar itself and scrolling should work
  * JEI issue 

### New
#### Recipes
* Recipes in general use a lot less iron, especially in the early game.  However, more stone will be required (so it's worth smelting up a stack or two of cobblestone before you start on the mod).
  * Added a collection of Reinforced Stone blocks (bricks, slabs, pillars, walls...), made with stone and (a little) Compressed Iron.  
    * These are both good for building with (high blast resistance), and are used in many recipes to reduce compressed iron requirements.
* For modpack makers: all machine recipes are now loaded from datapacks (`data/<modid>/pneumaticcraft/machine_recipes/<machine_type/*.json`) so can be easily overridden and reloaded on the fly with the `/reload` command.  To remove an existing recipe, simply create a JSON file of the same name in your datapack with an empty JSON document: `{}`
  * You can see all default recipes at https://github.com/TeamPneumatic/pnc-repressurized/tree/1.14/src/main/resources/data/pneumaticcraft/pneumaticcraft/machine_recipes
#### Machines
* Pressure Chamber
  * The unloved Pressure Chamber Interface filter system is gone.  The Interface will now only pull crafted items (there is a button in the Interface GUI to pull everything in case the chamber needs to be emptied).
  * The Pressure Chamber Interface also accepts a Dispenser Upgrade; if installed it will eject items into the world if there is no adjacent inventory.  (Note that the interface still auto-pushes items; no need to pull items from it).
  * Several new default recipes, including ways to make slime balls, ice, packed ice, and blue ice
* Refinery
  * There is now a separate Refinery Controller block in addition to the four Refinery Outputs
  * Refinery Outputs can be stacked beside or on top of the Refinery Controller
  * Outputs only output fluid, never accept it (except from the Controller)
  * Controller only accepts input fluid (Oil by default)
* Thermopneumatic Processing Plant
  * Can now take recipes which produce an item output in addition to or instead of a fluid output
    * One such recipe is added by default: an Upgrade Matrix taking Lapis and Water, which is used to craft upgrades in a more lapis-efficient way
* Etching Tank
  * This is a new machine which replaces the old in-world crafting of PCB's with Etching Acid
  * It's faster than the old method of etching PCB's (twice as fast by default, much faster if the tank is heated)
  * With sufficient infrastructure, this can be even faster than using the Assembly System to mass-produce PCB's (but the Assembly System remains a simpler & more convenient option)
  * Separate output slots for succesful and failed PCB's; failed PCB's can be recycled in a Blast Furnace (which also yields a little XP)
* UV Light Box
  * The GUI now has a slider to set the exposure threshold instead of using redstone emission
  * Exposed items (past the configured threshold) are moved to a new output slot, which can be extracted from using automation
* Programmer improvements
  * Fix inconsistent zoom out/zoom in behaviour
  * Now possible to merge programs from Pastebin, clipboard or saved items
* Gas Lift
  * Now uses new Drill Pipe block instead of Pressure Tube
  * Drill Pipe is a non-tile entity block so it's also suitable for decorative building
#### Plastic
* Coloured plastic is gone, and so has the Plastic Mixer.
  * There is now only one type of plastic: the Plastic Sheet.
    * Which also means there's only one type of Programing Puzzle Piece, making Drone programming a lot easier.
  * You can make Plastic Sheets by pouring a bucket of Molten Plastic (made in the Thermopneumatic Processing Plant from LPG & Coal as before) into the world.  It will solidify after 10 ticks.
  * Alternatively, put a bucket (or tank) of Molten Plastic in an inventory with a Heat Frame attached, and chill the Heat Frame as much as possible (-75C is optimum) for bonus Plastic Sheet output; up to 1.75x.
  * New Plastic Construction Brick™, made from plastic and any dye. Can be used for building; they also damage any entity that steps on them without footwear.
#### Semiblocks 
* Semiblocks are now entities. No direct gameplay effect, but rendering and syncing of semiblocks should be far more robust now.
  * Semiblocks include crop supports, logistics frames, heat frame, spawner agitators and transfer gadgets.
#### Storage
* Added Reinforced Chest - a 36-slot, blast-proof inventory which keeps its contents when broken
* Added Smart Chest - a 72-slot blast-proof inventory which keeps its contents when broken
  * Smart Chest can also filter each slot to only hold a certain item
  * And close one or more slots, effectively giving the chest a configurable inventory size
  * *And* push and pull items to adjacent inventories
  * *AND* eject items like a dropper and absorb nearby items like a vacuum hopper
  * Super powerful, but not the cheapest to make (needs a PCB)
* Added three new fluid storage tanks, imaginatively title the Small Fluid Tank (32000mB), Medium Fluid Tank (64000mB) and Large Fluid Tank (128000mB)
  * Denser storage than the Liquid Hopper
  * Can be stacked vertically, and combined (with a wrench) into a sort-of-multiblock
  * Has a GUI where items can be inserted to transfer fluid, e.g. buckets, liquid hoppers, memory sticks (see below)
#### Items & tools
* Camo applicator right-click behaviour changed a little
  * Right click any non-camo block to copy its appearance
  * Sneak right click to clear camo
  * Right click any camo block to apply (or remove) camo
* Vortex Cannon
  * Reduced air usage by half
  * Increased crop/leaves breaking range from 3x3x3 to 5x5x5
  * Can now also break webs (very efficiently)
* Memory Stick (new)
  * Can be used to store and retrieve experience
  * Added a new Memory Essence fluid which is liquid experience (20mB = 1 XP point)
  * Memory Essence can be extracted from a Memory Stick via Liquid Hopper or any machine that can pull fluid from an item (e.g. the new tanks mentioned above)
  * Memory Stick will go in a Curio slot
  * Memory Essence is supported by the Aerial Interface
* Minigun
  * Freezing Ammo now creates a cloud (like a potion cloud) instead of "ice" blocks.  The cloud will freeze and damage entities in it.
#### Heat System
* Added new Heat Pipe block, perfect for transferring heat from one place to another.
  * Heat Pipe loses no heat to adjacent air or fluid blocks.  Consider it a much more compact alternative to lines of Compressed Iron Blocks surrounded by insulating blocks.
  * Heat Pipe can be camouflaged to run through walls/floors, and is waterloggable.
* Heat Frame Cooling expanded a little
  * Fluids to be cooled can now be provided with tanks, not just buckets (assuming the tank item provides a fluid handler capability)
  * Recipes can have a bonus output based on the temperature (new Molten Plastic -> Plastic Sheet recipe does this, but the old lava->obsidian and water->ice recipes do not)
* Campfire is recognised as a heat source, and is better than lighting a fire on netherrack.
#### Amadron
* Significant internal rewrite to hopefully fix the various syncing problems encountered in 1.12.2.
* Villager trades work much as before, but with support for trade levels: higher level trades will appear much more rarely in the random offers list.
* Player->player trades are still added via the tablet GUI, and are stored in `config/pneumaticcraft/AmadronPlayerOffers.cfg`.
* All static Amadron trades are now loaded from datapacks (`data/<modid>/pneumaticcraft/amadron_offers`) so are much easier to modify or disable.  Because of this, adding static/periodic trades via the tablet GUI is no longer a thing.
  * See https://github.com/TeamPneumatic/pnc-repressurized/tree/1.14/src/main/resources/data/pneumaticcraft/pneumaticcraft/amadron_offers for the default offers
  * Default offers can be disabled by simply using an empty JSON document `{}`.
#### Villagers
* You might get lucky and find a Pneumatic Villager house when exploring (there are different houses for each of the five village biomes)
  * These houses have a couple of basic PneumaticCraft tools and machines, and a chest with some handy loot
* The Pneumatic Villager (Mechanic) has a massively expanded trade list, with some nice trades at higher levels
  * Villager point-of-interest is the Charging Station, so unemployed villagers can become Mechanics
  * Because of this, the old way of creating mechanics in the pressure chamber no longer works
#### Drones
* Added two new basic (non-programmable) drones:
  * Guard Drone: attacks any mobs in a 31x31x31 area around where it's deployed
  * Collector Drone: collects nearby items (17x17x17) and puts them in an inventory that it's deployed on or adjacent to. Has some basic item filtering functionality. Can take range upgrades to expand the item collection range.
#### Logistics
* Added the ability to filter by mod ID
* Added support for the Tag Filter item, a way to filter by item tags (the replacement for ore dictionary matching)
  * Use a Tag Workbench (new block) to create Tag Filters
* Reduced the amount of air used to transport items & fluids, and made the amount adjustable in config (see "Logistics" section in `pneumaticcraft-common.toml`)
#### Upgrades
* Max upgrade amounts are now hard-enforced in machine & item GUIs
  * e.g. if a machine takes a max of 10 Speed Upgrades, it is now impossible to put more than 10 Speed Upgrades in the machine (unlike in 1.12.2 where any number could be added but only 10 were used).
* Volume upgrades have changed a bit
  * Max volume upgrades in anything is 25.
  * They have diminishing returns as more added: 1 upgrade will double the base volume, 4 upgrades will quadruple it, 25 upgrades will increase the volume by a factor of 10.
* New upgrades:
  * Jumping Upgrade - replaces Range Upgrade in Pneumatic Leggings
  * Inventory Upgrade - replaces Dispenser Upgrade in Drones
  * Flippers Upgrade - for Pneumatic Boots, swim speed increase
  * Standby Upgrade - for Logistics & Harvesting drones; allows them to go on standby when idle, saving air
  * Minigun Upgrade - replaces Entity Tracker Upgrade in drones  
* Some upgrades are now *tiered*, meaning there is a different crafting recipe for each tier. 
  * Jet Boots Upgrade and Jumping Upgrade are examples of such upgrades.
#### Tube Modules
* Regulator Module now by default regulates to 0.1 bar below tube pressure (4.9 for basic tubes, 19.9 for advanced tubes)
  * A full redstone signal will reduce the regulation amount to 0 (acting like a shut-off valve), intermediate signal levels interpolate the regulation level
  * With an Advanced PCB installed, fine-grained control is available as it used to be
* Safety Valve Module now leaks air at 0.1 bar below tube pressure (4.9 for basic tubes, 19.9 for advanced tubes)
  * Again, adding an Advanced PCB allows fine-grained control, as it used to
* Redstone Module will now take input from a Pressure Gauge module on the same tube section
* Tubes with inline modules (the Regulator and Flow Detector) can *only* be connected to on the two ends of the module
  * no more connecting to a Regulator module on the side (which never worked properly anyway) 
#### Misc
* Added a Display Table, a simple one-item inventory which displays its held item. For both aesthetic and possible automation purposes.
* Fuels now have a burn rate multiplier in addition to a "total air produced" amount
  * Diesel burns more slowly now, but produces slightly more air overall
  * LPG burns a little faster, same overall production
  * Gasoline burns significantly faster, same overall production
  * Kerosene is unchanged
* Kerosene Lamp uses much less fuel now, especially at larger ranges
* Lots of general GUI cleanup and polishing
* Updated textures for many items
* Fluid-containing items (Liquid Hopper, tanks, machines...) now show their contained fluid while in item form
* Pressure Tubes are now waterloggable
#### Mod Integration
* Currently patchy compared with 1.12.2, but the following mods are currently supported:
  * The One Probe
  * WAILA/HWYLA
  * JEI
  * Patchouli (the guide book has been updated to reflect all the new changes)
  * Curios
* More mod support will be added in future, but this is not likely to happen until the 1.15.2 port.