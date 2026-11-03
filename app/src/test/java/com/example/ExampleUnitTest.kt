package com.example

import com.example.data.ClashDataIdRegistry
import com.example.model.ClashAccount
import com.example.model.ClashUpgrade
import com.example.model.UpgradeCategory
import com.example.model.WidgetDisplayPreferences
import com.example.model.calculateClashGems
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun verifyClashGemFormula() {
        assertEquals(0, calculateClashGems(0))
        assertEquals(1, calculateClashGems(45))
        assertTrue(calculateClashGems(3600) in 15..25)
        assertTrue(calculateClashGems(86400) in 250..270)
        assertTrue(calculateClashGems(604800) in 990..1010)
        assertTrue(calculateClashGems(1209600) > 1500)
    }

    @Test
    fun verifyClashDataIdRegistryMappingAndSpecificHeroNames() {
        val th = ClashDataIdRegistry.resolve(1000001, "buildings")
        assertEquals("Town Hall", th.name)
        assertEquals(UpgradeCategory.BUILDING, th.category)

        val multiGear = ClashDataIdRegistry.resolve(1000079, "buildings")
        assertEquals("Multi-Gear Tower", multiGear.name)
        assertEquals(UpgradeCategory.BUILDING, multiGear.category)

        val superWiz = ClashDataIdRegistry.resolve(1000090, "buildings")
        assertEquals("Super Wizard Tower", superWiz.name)

        val heroHall = ClashDataIdRegistry.resolve(1000071, "buildings")
        assertEquals("Hero Hall", heroHall.name)

        val queen = ClashDataIdRegistry.resolve(28000001, "heroes")
        assertEquals("Archer Queen", queen.name)
        assertEquals(UpgradeCategory.HERO, queen.category)

        val minionPrince = ClashDataIdRegistry.resolve(28000006, "heroes")
        assertEquals("Minion Prince", minionPrince.name)

        val dragonDuke = ClashDataIdRegistry.resolve(28000007, "heroes")
        assertEquals("Dragon Duke", dragonDuke.name)

        val sneezy = ClashDataIdRegistry.resolve(73000016, "pets")
        assertEquals("Sneezy", sneezy.name)

        val longshot = ClashDataIdRegistry.resolve(107000000, "guardians")
        assertEquals("Longshot (Guardian)", longshot.name)

        // Verify automatic healing of previously saved generic "#number" names
        val healedHero = ClashDataIdRegistry.resolve(0, "heroes", "Hero #7")
        assertEquals("Dragon Duke", healedHero.name)

        val healedPet = ClashDataIdRegistry.resolve(0, "pets", "Hero Pet #16")
        assertEquals("Sneezy", healedPet.name)

        val healedEquip = ClashDataIdRegistry.resolve(0, "equipment", "Hero Equipment #34")
        assertEquals("Electro Boots", healedEquip.name)
    }

    @Test
    fun verifyWidgetDisplayFormatting() {
        val now = System.currentTimeMillis()
        val goblinUpgrade = ClashUpgrade(
            name = "Multi-Gear Tower",
            targetLevel = 3,
            endTimeMs = now + 66840_000L,
            isGoblin = true
        )
        assertEquals("Multi-Gear Tower · G", goblinUpgrade.displayTitle)
        assertEquals("18h 34m", goblinUpgrade.formattedRemainingTime(now))
        assertTrue(goblinUpgrade.formattedFinishTime().startsWith("Finishes "))

        val helperUpgrade = ClashUpgrade(
            name = "Hero Hall",
            targetLevel = 12,
            endTimeMs = now + 365040_000L,
            helperTag = "H8",
            helperNote = "Apprentice · repeat forecast off"
        )
        assertEquals("Hero Hall · H8", helperUpgrade.displayTitle)
        assertEquals("4d 5h 24m", helperUpgrade.formattedRemainingTime(now))

        val account = ClashAccount(
            playerTag = "#QUJGYLGOP",
            townHallLevel = 18,
            totalBuilders = 6,
            activeUpgrades = listOf(goblinUpgrade, helperUpgrade)
        )
        assertTrue(account.hasGoblinBuilder)
        assertEquals("TH 18 · 1/6 BUILDERS + G", account.headerSubtitleText)
        assertEquals("BUILDERS  1/6 + Goblin", account.sectionHeaderText)
    }

    @Test
    fun verifyWidgetDisplayPreferencesSelection() {
        val now = System.currentTimeMillis()
        val buildingUpg = ClashUpgrade(
            id = "upg-1",
            name = "Monolith",
            targetLevel = 5,
            category = UpgradeCategory.BUILDING,
            endTimeMs = now + 100_000L
        )
        val labUpg = ClashUpgrade(
            id = "upg-2",
            name = "Root Rider",
            targetLevel = 3,
            category = UpgradeCategory.LABORATORY,
            endTimeMs = now + 200_000L
        )

        val defaultPrefs = WidgetDisplayPreferences()
        assertTrue(defaultPrefs.isUpgradeVisible(buildingUpg))
        assertTrue(defaultPrefs.isUpgradeVisible(labUpg))
        assertEquals(6, defaultPrefs.enabledSectionCount)

        val buildersOnlyPrefs = defaultPrefs.copy(
            showLaboratory = false,
            showVillageInventory = false
        )
        assertTrue(buildersOnlyPrefs.isUpgradeVisible(buildingUpg))
        assertFalse(buildersOnlyPrefs.isUpgradeVisible(labUpg))
        assertEquals(4, buildersOnlyPrefs.enabledSectionCount)

        val hiddenSingleUpgradePrefs = defaultPrefs.copy(
            hiddenUpgradeIds = setOf("upg-1")
        )
        assertFalse(hiddenSingleUpgradePrefs.isUpgradeVisible(buildingUpg))
        assertTrue(hiddenSingleUpgradePrefs.isUpgradeVisible(labUpg))
    }
}
