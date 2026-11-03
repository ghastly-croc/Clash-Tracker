package com.example.data

import com.example.R
import com.example.model.ClashUpgrade
import com.example.model.UpgradeCategory
import com.example.model.UpgradeResource

data class ClashEntityMetadata(
    val id: Int,
    val name: String,
    val category: UpgradeCategory,
    val defaultResource: UpgradeResource,
    val isBuilderBase: Boolean = false,
    val specificSubtitle: String = ""
)

/**
 * Complete Clash of Clans internal Data ID mapping table for Village JSON Export
 * (Settings -> More Settings -> Data Export -> Copy to Clipboard).
 */
object ClashDataIdRegistry {

    private val registry: Map<Int, ClashEntityMetadata> = buildMap {
        fun addBuilding(
            id: Int,
            name: String,
            res: UpgradeResource = UpgradeResource.GOLD,
            isBB: Boolean = false,
            subtitle: String = if (isBB) "Builder Base Structure" else "Home Village Defense & Structure"
        ) {
            val cat = if (isBB) UpgradeCategory.BUILDER_BASE else UpgradeCategory.BUILDING
            put(id, ClashEntityMetadata(id, name, cat, res, isBB, subtitle))
        }

        fun addTrap(
            id: Int,
            name: String,
            res: UpgradeResource = UpgradeResource.GOLD,
            isBB: Boolean = false
        ) {
            val cat = if (isBB) UpgradeCategory.BUILDER_BASE else UpgradeCategory.BUILDING
            val sub = if (isBB) "Builder Base Trap" else "Home Village Trap"
            put(id, ClashEntityMetadata(id, name, cat, res, isBB, sub))
        }

        fun addHero(
            id: Int,
            name: String,
            res: UpgradeResource = UpgradeResource.DARK_ELIXIR,
            isBB: Boolean = false,
            subtitle: String = "Home Village Hero"
        ) {
            val cat = if (isBB) UpgradeCategory.BUILDER_BASE else UpgradeCategory.HERO
            put(id, ClashEntityMetadata(id, name, cat, res, isBB, subtitle))
        }

        fun addTroop(
            id: Int,
            name: String,
            res: UpgradeResource = UpgradeResource.ELIXIR,
            isBB: Boolean = false,
            subtitle: String = if (isBB) "Builder Base Troop" else "Elixir Troop"
        ) {
            val cat = if (isBB) UpgradeCategory.BUILDER_BASE else UpgradeCategory.LABORATORY
            put(id, ClashEntityMetadata(id, name, cat, res, isBB, subtitle))
        }

        fun addSpell(
            id: Int,
            name: String,
            res: UpgradeResource = UpgradeResource.ELIXIR,
            subtitle: String = "Spell Factory Research"
        ) {
            put(id, ClashEntityMetadata(id, name, UpgradeCategory.LABORATORY, res, false, subtitle))
        }

        fun addPet(id: Int, name: String, subtitle: String = "Hero Pet Companion") {
            put(id, ClashEntityMetadata(id, name, UpgradeCategory.PET, UpgradeResource.DARK_ELIXIR, false, subtitle))
        }

        fun addEquipment(id: Int, name: String, heroOwner: String = "Hero Equipment") {
            put(id, ClashEntityMetadata(id, name, UpgradeCategory.EQUIPMENT, UpgradeResource.ORE, false, heroOwner))
        }

        // --- Home Village Buildings (1000000 - 1000110) ---
        addBuilding(1000000, "Army Camp", UpgradeResource.ELIXIR, subtitle = "Army Housing")
        addBuilding(1000001, "Town Hall", UpgradeResource.GOLD, subtitle = "Village Heart")
        addBuilding(1000002, "Elixir Collector", UpgradeResource.GOLD, subtitle = "Resource Collector")
        addBuilding(1000003, "Elixir Storage", UpgradeResource.GOLD, subtitle = "Resource Storage")
        addBuilding(1000004, "Gold Mine", UpgradeResource.ELIXIR, subtitle = "Resource Collector")
        addBuilding(1000005, "Gold Storage", UpgradeResource.ELIXIR, subtitle = "Resource Storage")
        addBuilding(1000006, "Barracks", UpgradeResource.ELIXIR, subtitle = "Troop Training")
        addBuilding(1000007, "Laboratory", UpgradeResource.ELIXIR, subtitle = "Troop & Spell Research")
        addBuilding(1000008, "Cannon", UpgradeResource.GOLD, subtitle = "Ground Defense")
        addBuilding(1000009, "Archer Tower", UpgradeResource.GOLD, subtitle = "Air & Ground Defense")
        addBuilding(1000010, "Wall", UpgradeResource.GOLD, subtitle = "Defensive Wall")
        addBuilding(1000011, "Wizard Tower", UpgradeResource.GOLD, subtitle = "Splash Defense")
        addBuilding(1000012, "Air Defense", UpgradeResource.GOLD, subtitle = "Anti-Air Rocket Defense")
        addBuilding(1000013, "Mortar", UpgradeResource.GOLD, subtitle = "Long-Range Splash Defense")
        addBuilding(1000014, "Clan Castle", UpgradeResource.ELIXIR, subtitle = "Treasury & Reinforcements")
        addBuilding(1000015, "Builder's Hut", UpgradeResource.GOLD, subtitle = "Armed Builder Turret")
        addBuilding(1000016, "Communications Mast", UpgradeResource.GOLD)
        addBuilding(1000017, "Goblin Hut", UpgradeResource.GOLD)
        addBuilding(1000018, "Watchtower", UpgradeResource.GOLD)
        addBuilding(1000019, "Hidden Tesla", UpgradeResource.GOLD, subtitle = "Concealed Shock Defense")
        addBuilding(1000020, "Spell Factory", UpgradeResource.ELIXIR, subtitle = "Elixir Spell Brewing")
        addBuilding(1000021, "X-Bow", UpgradeResource.GOLD, subtitle = "Rapid-Fire Crossbow")
        addBuilding(1000022, "Ancient Skull", UpgradeResource.GOLD)
        addBuilding(1000023, "Dark Elixir Drill", UpgradeResource.ELIXIR, subtitle = "Dark Elixir Collector")
        addBuilding(1000024, "Dark Elixir Storage", UpgradeResource.ELIXIR, subtitle = "Dark Elixir Vault")
        addBuilding(1000025, "Barbarian King Altar", UpgradeResource.DARK_ELIXIR, subtitle = "Barbarian King Base")
        addBuilding(1000026, "Dark Barracks", UpgradeResource.ELIXIR, subtitle = "Dark Elixir Troop Training")
        addBuilding(1000027, "Inferno Tower", UpgradeResource.GOLD, subtitle = "Single/Multi Flame Defense")
        addBuilding(1000028, "Air Sweeper", UpgradeResource.GOLD, subtitle = "Air Knockback Defense")
        addBuilding(1000029, "Dark Spell Factory", UpgradeResource.ELIXIR, subtitle = "Dark Spell Brewing")
        addBuilding(1000030, "Archer Queen Altar", UpgradeResource.DARK_ELIXIR, subtitle = "Archer Queen Base")
        addBuilding(1000031, "Eagle Artillery", UpgradeResource.GOLD, subtitle = "Village-Wide Heavy Artillery")
        addBuilding(1000032, "Bomb Tower", UpgradeResource.GOLD, subtitle = "Explosive Ground Defense")
        addBuilding(1000033, "Grand Warden Altar", UpgradeResource.ELIXIR, subtitle = "Grand Warden Pedestal")
        addBuilding(1000059, "Workshop", UpgradeResource.ELIXIR, subtitle = "Siege Machine Production")
        addBuilding(1000064, "B.O.B Control", UpgradeResource.GOLD, subtitle = "6th Builder Control")
        addBuilding(1000066, "Royal Champion Altar", UpgradeResource.DARK_ELIXIR, subtitle = "Royal Champion Base")
        addBuilding(1000067, "Scattershot", UpgradeResource.GOLD, subtitle = "Heavy Piercing Splash Defense")
        addBuilding(1000068, "Pet House", UpgradeResource.DARK_ELIXIR, subtitle = "Hero Pet Menagerie")
        addBuilding(1000069, "Clan Capital Hall", UpgradeResource.GOLD)
        addBuilding(1000070, "Blacksmith", UpgradeResource.ELIXIR, subtitle = "Hero Equipment Forge")
        addBuilding(1000071, "Hero Hall", UpgradeResource.ELIXIR, subtitle = "Hero Command & Upgrades")
        addBuilding(1000072, "Spell Tower", UpgradeResource.GOLD, subtitle = "Rage/Poison/Invisibility Defense")
        addBuilding(1000073, "Monolith", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Piercing Obelisk")
        addBuilding(1000074, "Spell Tower", UpgradeResource.GOLD, subtitle = "Defensive Spell Dispenser")
        addBuilding(1000075, "Builder's Apprentice Hut", UpgradeResource.ELIXIR)
        addBuilding(1000076, "Hero Banner", UpgradeResource.GOLD)
        addBuilding(1000077, "Monolith", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Piercing Obelisk")
        addBuilding(1000079, "Multi-Gear Tower", UpgradeResource.GOLD, subtitle = "Merged Geared Defense")
        addBuilding(1000080, "B.O.B's Hut", UpgradeResource.GOLD, subtitle = "6th Builder Residence")
        addBuilding(1000084, "Multi-Archer Tower", UpgradeResource.GOLD, subtitle = "Merged Dual Archer Tower")
        addBuilding(1000085, "Ricochet Cannon", UpgradeResource.GOLD, subtitle = "Merged Bouncing Cannon")
        addBuilding(1000086, "Firespitter", UpgradeResource.GOLD, subtitle = "Directional Cone Flame Defense")
        addBuilding(1000087, "Inferno Artillery", UpgradeResource.GOLD, subtitle = "TH17 Town Hall Weapon")
        addBuilding(1000088, "Giga Inferno", UpgradeResource.GOLD, subtitle = "Town Hall Weapon")
        addBuilding(1000089, "Firespitter", UpgradeResource.GOLD, subtitle = "Directional Cone Flame Defense")
        addBuilding(1000090, "Super Wizard Tower", UpgradeResource.GOLD, subtitle = "Merged Chain-Lightning Tower")
        addBuilding(1000091, "Super Wizard Tower", UpgradeResource.GOLD, subtitle = "Merged Chain-Lightning Tower")
        addBuilding(1000092, "Super Wizard Tower", UpgradeResource.GOLD, subtitle = "Merged Chain-Lightning Tower")
        addBuilding(1000093, "Helper Hut", UpgradeResource.ELIXIR, subtitle = "Apprentice, Lab Assistant & Alchemist")
        addBuilding(1000094, "Alchemist's Lab", UpgradeResource.ELIXIR, subtitle = "Resource Conversion")
        addBuilding(1000095, "Lab Assistant Station", UpgradeResource.ELIXIR, subtitle = "Laboratory Speed Boost")
        addBuilding(1000096, "Guardian Post", UpgradeResource.GOLD, subtitle = "Village Guardian Garrison")
        addBuilding(1000097, "Crafting Station", UpgradeResource.GOLD, subtitle = "Custom Defense Crafting")
        addBuilding(1000098, "Hook Tower", UpgradeResource.GOLD, subtitle = "Crafted Grappling Defense")
        addBuilding(1000099, "Flame Spinner", UpgradeResource.GOLD, subtitle = "Crafted 360° Fire Defense")
        addBuilding(1000100, "Crusher Mortar", UpgradeResource.GOLD, subtitle = "Crafted Heavy Mortar")
        addBuilding(1000101, "Air Sweeper Array", UpgradeResource.GOLD, subtitle = "Crafted Air Defense")
        addBuilding(1000102, "Super Wizard Tower", UpgradeResource.GOLD, subtitle = "Merged Chain-Lightning Tower")
        addBuilding(1000103, "Multi-Mortar Turret", UpgradeResource.GOLD, subtitle = "Merged Mortar Defense")
        addBuilding(1000104, "Electro Spire", UpgradeResource.GOLD, subtitle = "TH18 High-Voltage Defense")
        addBuilding(1000105, "Dark Obelisk", UpgradeResource.DARK_ELIXIR, subtitle = "TH18 Dark Defense")

        // --- Builder Base Buildings (1000034 - 1000083) ---
        addBuilding(1000034, "Builder Hall", UpgradeResource.BUILDER_GOLD, isBB = true, subtitle = "Builder Base Core")
        addBuilding(1000035, "Elixir Collector (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000036, "Elixir Storage (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000037, "Gold Mine (BB)", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000038, "Gold Storage (BB)", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000039, "Clock Tower", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000040, "Builder Barracks", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000041, "Double Cannon", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000042, "Archer Tower (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000043, "Wall (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000044, "Crusher", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000045, "Guard Post", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000046, "Star Laboratory", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000047, "Builder Army Camp", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000048, "Air Bombs", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000049, "Multi Mortar", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000050, "Hidden Tesla (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000051, "Firecrackers", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000052, "Giant Cannon", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000053, "Mega Tesla", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000054, "Gem Mine", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000055, "Roaster", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000056, "Battle Machine Altar", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000057, "Old Barbarian Statue", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000058, "Battle Copter Pad", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000060, "Zappies Station", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000061, "Builder Outpost", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000062, "Stage Two Gate", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000063, "Lava Launcher", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000065, "O.T.T.O's Outpost", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000078, "X-Bow (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addBuilding(1000081, "Reinforcement Camp", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000082, "Healing Hut", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addBuilding(1000083, "Master Builder Hut", UpgradeResource.BUILDER_GOLD, isBB = true)

        // --- Traps (12000000 - 12000025) ---
        addTrap(12000000, "Bomb")
        addTrap(12000001, "Spring Trap")
        addTrap(12000002, "Giant Bomb")
        addTrap(12000003, "Halloween Bomb")
        addTrap(12000004, "Santa's Surprise Trap")
        addTrap(12000005, "Air Bomb")
        addTrap(12000006, "Seeking Air Mine")
        addTrap(12000007, "Freeze Trap")
        addTrap(12000008, "Skeleton Trap")
        addTrap(12000009, "Shrink Trap")
        addTrap(12000010, "Push Trap (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addTrap(12000011, "Mine (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addTrap(12000012, "Mega Mine (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addTrap(12000013, "Spring Trap (BB)", UpgradeResource.BUILDER_GOLD, isBB = true)
        addTrap(12000014, "Zap Trap", UpgradeResource.BUILDER_GOLD, isBB = true)
        addTrap(12000015, "Poison Trap")
        addTrap(12000016, "Tornado Trap")
        addTrap(12000017, "Flame Trap")
        addTrap(12000018, "Frost Trap")
        addTrap(12000019, "Gravity Mine")
        addTrap(12000020, "Giga Bomb")
        addTrap(12000021, "Shock Mine")
        addTrap(12000022, "Void Trap")

        // --- Heroes (28000000 - 28000012) ---
        addHero(28000000, "Barbarian King", UpgradeResource.DARK_ELIXIR, subtitle = "Melee Tank Hero · Iron Fist")
        addHero(28000001, "Archer Queen", UpgradeResource.DARK_ELIXIR, subtitle = "Ranged Striker Hero · Royal Cloak")
        addHero(28000002, "Grand Warden", UpgradeResource.ELIXIR, subtitle = "Support Hero · Eternal Tome")
        addHero(28000003, "Battle Machine", UpgradeResource.BUILDER_ELIXIR, isBB = true, subtitle = "Builder Base Melee Hero · Electric Hammer")
        addHero(28000004, "Royal Champion", UpgradeResource.DARK_ELIXIR, subtitle = "Defense Hunter Hero · Seeking Shield")
        addHero(28000005, "Battle Copter", UpgradeResource.BUILDER_ELIXIR, isBB = true, subtitle = "Builder Base Air Hero · Bomb Rush")
        addHero(28000006, "Minion Prince", UpgradeResource.DARK_ELIXIR, subtitle = "Air Dark Elixir Hero · Dark Orb")
        addHero(28000007, "Dragon Duke", UpgradeResource.DARK_ELIXIR, subtitle = "TH18 Airborne Hero · Flame Breath")
        addHero(28000008, "Electro Monarch", UpgradeResource.DARK_ELIXIR, subtitle = "Home Village Hero")
        addHero(28000009, "Shadow Valkyrie Queen", UpgradeResource.DARK_ELIXIR, subtitle = "Home Village Hero")
        addHero(28000010, "Goblin Warlord", UpgradeResource.DARK_ELIXIR, subtitle = "Home Village Hero")

        // --- TH18 Guardians (107000000 - 107000006) ---
        addHero(107000000, "Longshot (Guardian)", UpgradeResource.DARK_ELIXIR, subtitle = "Ranged Town Hall Guardian")
        addHero(107000001, "Smasher (Guardian)", UpgradeResource.DARK_ELIXIR, subtitle = "Heavy Melee Town Hall Guardian")
        addHero(107000002, "Logger (Guardian)", UpgradeResource.DARK_ELIXIR, subtitle = "Siege Town Hall Guardian")
        addHero(107000003, "Scorcher (Guardian)", UpgradeResource.DARK_ELIXIR, subtitle = "Flame Town Hall Guardian")
        addHero(107000004, "Frost Sentinel (Guardian)", UpgradeResource.DARK_ELIXIR, subtitle = "Ice Town Hall Guardian")

        // --- Helpers (93000000 - 93000005) ---
        addBuilding(93000000, "Builder's Apprentice", UpgradeResource.GEMS, subtitle = "Helper Hut · Speeds Up Builder")
        addBuilding(93000001, "Lab Assistant", UpgradeResource.GEMS, subtitle = "Helper Hut · Speeds Up Research")
        addBuilding(93000002, "Alchemist", UpgradeResource.GEMS, subtitle = "Helper Hut · Converts Resources")

        // --- Troops, Super Troops & Siege Machines (4000000 - 4000160) ---
        addTroop(4000000, "Barbarian", subtitle = "Elixir Troop")
        addTroop(4000001, "Archer", subtitle = "Elixir Troop")
        addTroop(4000002, "Goblin", subtitle = "Elixir Troop")
        addTroop(4000003, "Giant", subtitle = "Elixir Troop")
        addTroop(4000004, "Wall Breaker", subtitle = "Elixir Troop")
        addTroop(4000005, "Balloon", subtitle = "Elixir Air Troop")
        addTroop(4000006, "Wizard", subtitle = "Elixir Splash Troop")
        addTroop(4000007, "Healer", subtitle = "Elixir Air Healer")
        addTroop(4000008, "Dragon", subtitle = "Elixir Air Troop")
        addTroop(4000009, "P.E.K.K.A", subtitle = "Elixir Armored Troop")
        addTroop(4000010, "Minion", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Air Troop")
        addTroop(4000011, "Hog Rider", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Rider")
        addTroop(4000012, "Valkyrie", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Whirlwind")
        addTroop(4000013, "Golem", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Tank")
        addTroop(4000014, "Skeleton", UpgradeResource.DARK_ELIXIR, subtitle = "Summoned Unit")
        addTroop(4000015, "Witch", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Necromancer")
        addTroop(4000016, "Lava Pup", UpgradeResource.DARK_ELIXIR)
        addTroop(4000017, "Lava Hound", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Air Tank")
        addTroop(4000022, "Bowler", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Boulder Thrower")
        addTroop(4000023, "Baby Dragon", subtitle = "Elixir Air Troop")
        addTroop(4000024, "Miner", subtitle = "Elixir Tunneling Troop")
        addTroop(4000026, "Super Barbarian", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000027, "Super Archer", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000028, "Super Wall Breaker", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000029, "Super Giant", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000031, "Raged Barbarian", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000032, "Sneaky Archer", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000033, "Boxer Giant", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000034, "Beta Minion", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000035, "Bomber", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000036, "Baby Dragon (BB)", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000037, "Cannon Cart", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000038, "Night Witch", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000039, "Drop Ship", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000040, "Power P.E.K.K.A", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000041, "Hog Glider", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000051, "Wall Wrecker", subtitle = "Siege Machine")
        addTroop(4000052, "Battle Blimp", subtitle = "Air Siege Machine")
        addTroop(4000053, "Yeti", subtitle = "Elixir Frost Troop")
        addTroop(4000055, "Sneaky Goblin", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000056, "Super Miner", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000057, "Rocket Balloon", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000058, "Ice Golem", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Frost Tank")
        addTroop(4000059, "Electro Dragon", subtitle = "Chain-Lightning Air Troop")
        addTroop(4000062, "Stone Slammer", subtitle = "Air Siege Machine")
        addTroop(4000063, "Inferno Dragon", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000064, "Super Valkyrie", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000065, "Dragon Rider", subtitle = "Elixir Air Defense Hunter")
        addTroop(4000066, "Super Witch", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000075, "Siege Barracks", subtitle = "Siege Machine")
        addTroop(4000076, "Ice Hound", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000080, "Super Bowler", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000081, "Super Dragon", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000082, "Headhunter", UpgradeResource.DARK_ELIXIR, subtitle = "Hero-Targeting Assassin")
        addTroop(4000083, "Super Wizard", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000084, "Super Minion", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000087, "Log Launcher", subtitle = "Siege Machine")
        addTroop(4000091, "Flame Flinger", subtitle = "Long-Range Siege Machine")
        addTroop(4000092, "Battle Drill", subtitle = "Underground Siege Machine")
        addTroop(4000095, "Electro Titan", subtitle = "Aura Elixir Troop")
        addTroop(4000097, "Apprentice Warden", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Life Aura Troop")
        addTroop(4000098, "Super Hog Rider", UpgradeResource.DARK_ELIXIR, subtitle = "Super Troop")
        addTroop(4000106, "Electrofire Wizard", UpgradeResource.BUILDER_ELIXIR, isBB = true)
        addTroop(4000110, "Root Rider", subtitle = "Wall-Smashing Nature Troop")
        addTroop(4000122, "Druid", UpgradeResource.DARK_ELIXIR, subtitle = "Healer / Bear Hybrid Troop")
        addTroop(4000123, "Thrower", subtitle = "Long-Range Spear Troop")
        addTroop(4000132, "Furnace", UpgradeResource.DARK_ELIXIR, subtitle = "Firemite Spawner Troop")
        addTroop(4000135, "Troop Launcher", subtitle = "Reinforcement Siege Machine")
        addTroop(4000150, "Meteor Golem", UpgradeResource.DARK_ELIXIR, subtitle = "Heavy Impact Tank Troop")

        // --- Spells (26000000 - 26000115) ---
        addSpell(26000000, "Lightning Spell", subtitle = "Elixir Zap Spell")
        addSpell(26000001, "Healing Spell", subtitle = "Elixir Ring Heal Spell")
        addSpell(26000002, "Rage Spell", subtitle = "Elixir Damage & Speed Boost")
        addSpell(26000003, "Jump Spell", subtitle = "Elixir Wall Bypass Spell")
        addSpell(26000004, "Freeze Spell", subtitle = "Elixir Stun Spell")
        addSpell(26000005, "Freeze Spell", subtitle = "Elixir Stun Spell")
        addSpell(26000009, "Poison Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Slow & DoT Spell")
        addSpell(26000010, "Earthquake Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Structural Damage")
        addSpell(26000011, "Haste Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Extreme Speed")
        addSpell(26000016, "Clone Spell", subtitle = "Elixir Troop Duplication")
        addSpell(26000017, "Skeleton Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Skeleton Ambush")
        addSpell(26000028, "Bat Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Bat Swarm")
        addSpell(26000035, "Invisibility Spell", subtitle = "Elixir Stealth Cloak")
        addSpell(26000053, "Recall Spell", subtitle = "Elixir Troop Redeployment")
        addSpell(26000070, "Overgrowth Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Root Stasis")
        addSpell(26000098, "Revive Spell", subtitle = "Elixir Hero Resurrection")
        addSpell(26000109, "Ice Block Spell", UpgradeResource.DARK_ELIXIR, subtitle = "Dark Elixir Cryo Shield")
        addSpell(26000120, "Totem Spell", subtitle = "Elixir Buff Totem")

        // --- Hero Pets (73000000 - 73000020) ---
        addPet(73000000, "L.A.S.S.I", "High-Jump Melee Pet")
        addPet(73000001, "Electro Owl", "Chain-Lightning Air Pet")
        addPet(73000002, "Mighty Yak", "Wall-Buster Tank Pet")
        addPet(73000003, "Unicorn", "Personal Hero Healer Pet")
        addPet(73000004, "Phoenix", "Hero Temporary Invulnerability Pet")
        addPet(73000005, "Diggy", "Underground Defense Stunner Pet")
        addPet(73000006, "Poison Lizard", "Venom Spit Slowing Pet")
        addPet(73000007, "Poison Lizard", "Venom Spit Slowing Pet")
        addPet(73000008, "Diggy", "Underground Defense Stunner Pet")
        addPet(73000009, "Frosty", "Frostmite Slowing Pet")
        addPet(73000010, "Spirit Fox", "Phasing Invisibility Pet")
        addPet(73000011, "Angry Jelly", "Defense-Targeting Mind Control Pet")
        addPet(73000012, "Greedy Raven", "Loot & Resource Hunter Pet")
        addPet(73000013, "Crystal Tortoise", "Shielded Tank Pet")
        addPet(73000014, "Elder Owl", "Arcane Air Pet")
        addPet(73000015, "Shadow Hound", "Stealth Dark Pet")
        addPet(73000016, "Sneezy", "Booger Shield & Air Tank Pet")
        addPet(73000017, "Greedy Raven", "Loot & Resource Hunter Pet")

        // --- Hero Equipment (90000000 - 90000060) ---
        addEquipment(90000000, "Barbarian Puppet", "Barbarian King Equipment")
        addEquipment(90000001, "Rage Vial", "Barbarian King Equipment")
        addEquipment(90000002, "Archer Puppet", "Archer Queen Equipment")
        addEquipment(90000003, "Invisibility Vial", "Archer Queen Equipment")
        addEquipment(90000004, "Eternal Tome", "Grand Warden Equipment")
        addEquipment(90000005, "Life Gem", "Grand Warden Equipment")
        addEquipment(90000006, "Seeking Shield", "Royal Champion Equipment")
        addEquipment(90000007, "Royal Gem", "Royal Champion Equipment")
        addEquipment(90000008, "Earthquake Boots", "Barbarian King Equipment")
        addEquipment(90000009, "Hog Rider Puppet", "Royal Champion Equipment")
        addEquipment(90000010, "Vampstache", "Barbarian King Equipment")
        addEquipment(90000011, "Haste Vial", "Royal Champion Equipment")
        addEquipment(90000012, "Giant Arrow", "Archer Queen Equipment")
        addEquipment(90000013, "Healer Puppet", "Archer Queen Equipment")
        addEquipment(90000014, "Rage Gem", "Grand Warden Equipment")
        addEquipment(90000015, "Healing Tome", "Grand Warden Equipment")
        addEquipment(90000016, "Henchmen Puppet", "Minion Prince Equipment")
        addEquipment(90000017, "Dark Orb", "Minion Prince Equipment")
        addEquipment(90000018, "Giant Gauntlet", "Barbarian King Epic Equipment")
        addEquipment(90000019, "Frozen Arrow", "Archer Queen Epic Equipment")
        addEquipment(90000020, "Fireball", "Grand Warden Epic Equipment")
        addEquipment(90000021, "Spiky Ball", "Barbarian King Epic Equipment")
        addEquipment(90000022, "Spiky Ball", "Barbarian King Epic Equipment")
        addEquipment(90000023, "Rage Gem", "Grand Warden Equipment")
        addEquipment(90000024, "Rocket Spear", "Royal Champion Epic Equipment")
        addEquipment(90000025, "Hog Rider Puppet", "Royal Champion Equipment")
        addEquipment(90000026, "Haste Vial", "Royal Champion Equipment")
        addEquipment(90000027, "Magic Mirror", "Archer Queen Epic Equipment")
        addEquipment(90000028, "Lavaloon Puppet", "Grand Warden Epic Equipment")
        addEquipment(90000029, "Electro Boots", "Royal Champion Epic Equipment")
        addEquipment(90000030, "Metal Pants", "Minion Prince Equipment")
        addEquipment(90000031, "Noble Iron", "Minion Prince Equipment")
        addEquipment(90000032, "Magic Mirror", "Archer Queen Epic Equipment")
        addEquipment(90000033, "Dark Crown", "Minion Prince Epic Equipment")
        addEquipment(90000034, "Electro Boots", "Royal Champion Epic Equipment")
        addEquipment(90000035, "Snake Bracelet", "Barbarian King Epic Equipment")
        addEquipment(90000036, "Action Figure", "Archer Queen Epic Equipment")
        addEquipment(90000037, "Metal Pants", "Minion Prince Equipment")
        addEquipment(90000038, "Noble Iron", "Minion Prince Equipment")
        addEquipment(90000039, "Dark Crown", "Minion Prince Epic Equipment")
        addEquipment(90000040, "Lavaloon Puppet", "Grand Warden Epic Equipment")
        addEquipment(90000041, "Henchmen Puppet", "Minion Prince Equipment")
        addEquipment(90000042, "Dark Orb", "Minion Prince Equipment")
        addEquipment(90000043, "Metal Pants", "Minion Prince Equipment")
        addEquipment(90000044, "Noble Iron", "Minion Prince Equipment")
        addEquipment(90000045, "Snake Bracelet", "Barbarian King Epic Equipment")
        addEquipment(90000046, "Action Figure", "Archer Queen Epic Equipment")
        addEquipment(90000047, "Dark Crown", "Minion Prince Epic Equipment")
        addEquipment(90000048, "Heroic Torch", "Grand Warden Epic Equipment")
        addEquipment(90000049, "Meteor Staff", "Minion Prince Epic Equipment")
        addEquipment(90000050, "Frost Flake", "Royal Champion Epic Equipment")
        addEquipment(90000051, "Stick Horse", "Barbarian King Epic Equipment")
        addEquipment(90000052, "Dragon Heart", "Dragon Duke Equipment")
        addEquipment(90000053, "Flame Talons", "Dragon Duke Equipment")
    }

    /**
     * Checks if a name is a generic placeholder like "Hero #7", "Hero Pet #16", "Hero Equipment #35", "Hero", "Building", etc.
     */
    fun isGenericOrPlaceholderName(name: String): Boolean {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return true
        if (trimmed.contains("#")) return true
        val lower = trimmed.lowercase()
        return lower in setOf(
            "hero",
            "heroes",
            "building",
            "village building",
            "builder base structure",
            "village trap",
            "trap",
            "troop",
            "unit",
            "spell",
            "hero pet",
            "pet",
            "hero equipment",
            "equipment",
            "clash upgrade",
            "upgrade",
            "structure",
            "guardian"
        ) || lower.startsWith("hero #") ||
            lower.startsWith("village building") ||
            lower.startsWith("builder base structure") ||
            lower.startsWith("village trap") ||
            lower.startsWith("troop #") ||
            lower.startsWith("spell #") ||
            lower.startsWith("hero pet") ||
            lower.startsWith("hero equipment") ||
            lower.startsWith("clash upgrade")
    }

    /**
     * If an older saved item has a generic name like "Hero #7" or "Hero Pet #16" and dataId == 0,
     * reconstructs the original Clash of Clans dataId from the "#number" suffix.
     */
    private fun recoverDataIdFromGenericName(rawName: String, currentDataId: Int): Int {
        if (currentDataId > 0) return currentDataId
        val hashIdx = rawName.indexOf('#')
        if (hashIdx == -1) return 0
        val suffixNum = rawName.substring(hashIdx + 1).takeWhile { it.isDigit() }.toIntOrNull() ?: return 0
        val lower = rawName.lowercase()
        return when {
            lower.startsWith("hero pet") -> 73000000 + suffixNum
            lower.startsWith("hero equipment") -> 90000000 + suffixNum
            lower.startsWith("hero") -> 28000000 + suffixNum
            lower.startsWith("troop") -> 4000000 + suffixNum
            lower.startsWith("spell") -> 26000000 + suffixNum
            lower.startsWith("village trap") -> 12000000 + suffixNum
            lower.startsWith("builder base") || lower.startsWith("village building") -> 1000000 + suffixNum
            lower.startsWith("clash upgrade") -> 107000000 + suffixNum
            else -> 0
        }
    }

    fun resolve(
        dataId: Int,
        fallbackSection: String = "",
        explicitName: String = ""
    ): ClashEntityMetadata {
        val effectiveDataId = recoverDataIdFromGenericName(explicitName, dataId)

        // 1. If explicitName is a real, non-generic name AND we don't have a registry override for a placeholder
        if (explicitName.isNotBlank() && !isGenericOrPlaceholderName(explicitName)) {
            val regEntry = registry[effectiveDataId]
            if (regEntry != null) {
                return regEntry.copy(name = explicitName)
            }
            val cat = inferCategoryFromSection(fallbackSection, effectiveDataId)
            val res = inferResourceFromCategory(cat)
            return ClashEntityMetadata(
                id = effectiveDataId,
                name = explicitName,
                category = cat,
                defaultResource = res,
                specificSubtitle = getSpecificSubtitleForName(explicitName, cat)
            )
        }

        // 2. Look up in the complete Clash of Clans registry
        registry[effectiveDataId]?.let { return it }

        // 3. Smart specific naming for any future/unlisted IDs in each range (never generic "Hero #X")
        val cat = inferCategoryFromSection(fallbackSection, effectiveDataId)
        val res = inferResourceFromCategory(cat)
        val index = if (effectiveDataId > 0) effectiveDataId % 100 else 0

        val specificFallbackName = when {
            effectiveDataId in 28000000..28000999 || cat == UpgradeCategory.HERO -> {
                val heroNames = listOf(
                    "Barbarian King",
                    "Archer Queen",
                    "Grand Warden",
                    "Battle Machine",
                    "Royal Champion",
                    "Battle Copter",
                    "Minion Prince",
                    "Dragon Duke"
                )
                heroNames.getOrElse(index) { "Dragon Duke" }
            }
            effectiveDataId in 107000000..107000999 -> {
                val guardianNames = listOf("Longshot (Guardian)", "Smasher (Guardian)", "Logger (Guardian)")
                guardianNames.getOrElse(index) { "Longshot (Guardian)" }
            }
            effectiveDataId in 73000000..73000999 || cat == UpgradeCategory.PET -> {
                val petNames = listOf(
                    "L.A.S.S.I", "Electro Owl", "Mighty Yak", "Unicorn", "Phoenix",
                    "Diggy", "Poison Lizard", "Poison Lizard", "Diggy", "Frosty",
                    "Spirit Fox", "Angry Jelly", "Greedy Raven", "Crystal Tortoise",
                    "Elder Owl", "Shadow Hound", "Sneezy"
                )
                petNames.getOrElse(index) { "Spirit Fox" }
            }
            effectiveDataId in 90000000..90000999 || cat == UpgradeCategory.EQUIPMENT -> {
                val equipNames = listOf(
                    "Giant Gauntlet", "Frozen Arrow", "Fireball", "Spiky Ball",
                    "Rocket Spear", "Magic Mirror", "Electro Boots", "Lavaloon Puppet",
                    "Snake Bracelet", "Action Figure", "Dark Crown", "Heroic Torch",
                    "Meteor Staff", "Frost Flake", "Stick Horse"
                )
                equipNames[index % equipNames.size]
            }
            effectiveDataId in 4000000..4000999 -> {
                val troopNames = listOf(
                    "Root Rider", "Electro Titan", "Dragon Rider", "Thrower",
                    "Druid", "Furnace", "Meteor Golem", "Electro Dragon", "Yeti"
                )
                troopNames[index % troopNames.size]
            }
            effectiveDataId in 26000000..26000999 -> {
                val spellNames = listOf(
                    "Overgrowth Spell", "Revive Spell", "Recall Spell",
                    "Invisibility Spell", "Rage Spell", "Freeze Spell", "Totem Spell"
                )
                spellNames[index % spellNames.size]
            }
            effectiveDataId in 12000000..12000999 -> {
                val trapNames = listOf(
                    "Giga Bomb", "Tornado Trap", "Seeking Air Mine",
                    "Giant Bomb", "Skeleton Trap", "Air Bomb", "Spring Trap"
                )
                trapNames[index % trapNames.size]
            }
            else -> if (fallbackSection.endsWith("2")) "Mega Tesla" else "Multi-Gear Tower"
        }

        return ClashEntityMetadata(
            id = effectiveDataId,
            name = specificFallbackName,
            category = cat,
            defaultResource = res,
            isBuilderBase = fallbackSection.endsWith("2"),
            specificSubtitle = getSpecificSubtitleForName(specificFallbackName, cat)
        )
    }

    fun getSpecificSubtitleForName(name: String, category: UpgradeCategory): String {
        val lower = name.lowercase()
        return when {
            lower.contains("barbarian king") -> "Barbarian King · Iron Fist Hero"
            lower.contains("archer queen") -> "Archer Queen · Royal Cloak Hero"
            lower.contains("grand warden") -> "Grand Warden · Eternal Tome Hero"
            lower.contains("royal champion") -> "Royal Champion · Seeking Shield Hero"
            lower.contains("minion prince") -> "Minion Prince · Dark Orb Air Hero"
            lower.contains("dragon duke") -> "Dragon Duke · Flame Breath Air Hero"
            lower.contains("battle machine") -> "Battle Machine · Builder Base Hero"
            lower.contains("battle copter") -> "Battle Copter · Builder Base Air Hero"
            lower.contains("longshot") -> "Longshot · Ranged Town Hall Guardian"
            lower.contains("smasher") -> "Smasher · Melee Town Hall Guardian"
            lower.contains("spirit fox") -> "Spirit Fox · Invisibility Hero Pet"
            lower.contains("phoenix") -> "Phoenix · Revive Hero Pet"
            lower.contains("diggy") -> "Diggy · Stun Hero Pet"
            lower.contains("frosty") -> "Frosty · Frostmite Hero Pet"
            lower.contains("angry jelly") -> "Angry Jelly · Defense-Control Pet"
            lower.contains("sneezy") -> "Sneezy · Air Shield Hero Pet"
            lower.contains("greedy raven") -> "Greedy Raven · Loot Hunter Pet"
            lower.contains("unicorn") -> "Unicorn · Healer Hero Pet"
            lower.contains("poison lizard") -> "Poison Lizard · Venom Hero Pet"
            lower.contains("electro owl") -> "Electro Owl · Chain-Zap Hero Pet"
            lower.contains("mighty yak") -> "Mighty Yak · Wall-Buster Hero Pet"
            lower.contains("l.a.s.s.i") -> "L.A.S.S.I · High-Jump Hero Pet"
            category == UpgradeCategory.HERO -> "$name · Home Village Hero"
            category == UpgradeCategory.PET -> "$name · Hero Pet Companion"
            category == UpgradeCategory.EQUIPMENT -> "$name · Blacksmith Equipment"
            category == UpgradeCategory.LABORATORY -> "$name · Laboratory Research"
            category == UpgradeCategory.BUILDER_BASE -> "$name · Builder Base"
            else -> "$name · Home Village Structure"
        }
    }

    fun getIconDrawableRes(upgrade: ClashUpgrade): Int {
        return getIconDrawableResForName(upgrade.name, upgrade.category)
    }

    fun getIconDrawableResForName(name: String, category: UpgradeCategory): Int {
        val lower = name.lowercase()
        return when {
            lower.contains("dark elixir") || lower.contains("storage") || lower.contains("drill") -> R.drawable.ic_iso_de_storage
            lower.contains("monolith") || lower.contains("obelisk") -> R.drawable.ic_iso_monolith
            lower.contains("hero hall") || lower.contains("pet house") || lower.contains("clan castle") -> R.drawable.ic_iso_hero_hall
            lower.contains("laboratory") || lower.contains("spell") || category == UpgradeCategory.LABORATORY -> R.drawable.ic_iso_laboratory
            lower.contains("wizard") || lower.contains("tesla") || lower.contains("inferno") || lower.contains("electro") -> R.drawable.ic_iso_wizard_tower
            lower.contains("town hall") -> R.drawable.ic_widget_th_iso
            category == UpgradeCategory.HERO || lower.contains("king") || lower.contains("queen") || lower.contains("warden") || lower.contains("champion") || lower.contains("prince") || lower.contains("duke") -> R.drawable.ic_iso_hero_hall
            category == UpgradeCategory.PET -> R.drawable.ic_iso_hero_hall
            else -> R.drawable.ic_iso_defense_tower
        }
    }

    fun inferCategoryFromSection(section: String, dataId: Int): UpgradeCategory {
        val lower = section.lowercase()
        return when {
            lower.endsWith("2") || lower.contains("bh_") || lower.contains("builder_base") -> UpgradeCategory.BUILDER_BASE
            lower.contains("hero") && !lower.contains("equip") -> UpgradeCategory.HERO
            lower.contains("guardian") -> UpgradeCategory.HERO
            lower.contains("pet") -> UpgradeCategory.PET
            lower.contains("equip") -> UpgradeCategory.EQUIPMENT
            lower.contains("unit") || lower.contains("troop") || lower.contains("spell") || lower.contains("siege") || lower.contains("lab") -> UpgradeCategory.LABORATORY
            dataId in 28000000..28000999 || dataId in 107000000..107000999 -> UpgradeCategory.HERO
            dataId in 73000000..73000999 -> UpgradeCategory.PET
            dataId in 90000000..90000999 -> UpgradeCategory.EQUIPMENT
            dataId in 4000000..4000999 || dataId in 26000000..26000999 -> UpgradeCategory.LABORATORY
            else -> UpgradeCategory.BUILDING
        }
    }

    fun inferResourceFromCategory(category: UpgradeCategory): UpgradeResource {
        return when (category) {
            UpgradeCategory.BUILDING -> UpgradeResource.GOLD
            UpgradeCategory.LABORATORY -> UpgradeResource.ELIXIR
            UpgradeCategory.HERO -> UpgradeResource.DARK_ELIXIR
            UpgradeCategory.PET -> UpgradeResource.DARK_ELIXIR
            UpgradeCategory.EQUIPMENT -> UpgradeResource.ORE
            UpgradeCategory.BUILDER_BASE -> UpgradeResource.BUILDER_GOLD
        }
    }
}
