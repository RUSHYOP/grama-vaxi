package com.gramavaxi.app.schedule

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class VaccineScheduleTest {

    @Test
    fun `goat returns sheep-goat schedule`() {
        val schedule = VaccineSchedule.forSpecies("Goat")
        assertThat(schedule.map { it.name }).contains("PPR (Peste des Petits Ruminants)")
        assertThat(schedule.map { it.name }).contains("Sheep/Goat Pox")
        assertThat(schedule.map { it.name }).doesNotContain("HS (Haemorrhagic Septicaemia)")
    }

    @Test
    fun `sheep returns sheep-goat schedule`() {
        val schedule = VaccineSchedule.forSpecies("Sheep")
        assertThat(schedule).isNotEmpty()
        assertThat(schedule.map { it.name }).contains("PPR (Peste des Petits Ruminants)")
    }

    @Test
    fun `cow returns cattle schedule`() {
        val schedule = VaccineSchedule.forSpecies("Cow")
        assertThat(schedule.map { it.name }).contains("HS (Haemorrhagic Septicaemia)")
        assertThat(schedule.map { it.name }).contains("Black Quarter")
        assertThat(schedule.map { it.name }).doesNotContain("PPR (Peste des Petits Ruminants)")
    }

    @Test
    fun `species lookup is case-insensitive`() {
        assertThat(VaccineSchedule.forSpecies("COW")).isEqualTo(VaccineSchedule.forSpecies("cow"))
        assertThat(VaccineSchedule.forSpecies("BUFFALO")).isEqualTo(VaccineSchedule.forSpecies("Cow"))
    }

    @Test
    fun `unknown species defaults to sheep-goat schedule`() {
        val unknown = VaccineSchedule.forSpecies("Camel")
        val goat = VaccineSchedule.forSpecies("Goat")
        assertThat(unknown).isEqualTo(goat)
    }

    @Test
    fun `every vaccine has positive cycle days`() {
        listOf("Goat", "Sheep", "Cow").forEach { species ->
            VaccineSchedule.forSpecies(species).forEach { def ->
                assertThat(def.cycleDays).isGreaterThan(0)
                assertThat(def.firstShotAfterDays).isAtLeast(0)
            }
        }
    }

    @Test
    fun `deworming is included for both species`() {
        assertThat(VaccineSchedule.forSpecies("Goat").map { it.name }).contains("Deworming")
        assertThat(VaccineSchedule.forSpecies("Cow").map { it.name }).contains("Deworming")
    }
}
