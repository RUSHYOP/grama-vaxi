package com.gramavaxi.app.schedule

/**
 * Default vaccination schedules per species.
 * cycleDays = re-vaccination interval after a dose is administered.
 * firstShotAfterDays = how many days after registration the first dose is due.
 */
data class VaccineDef(
    val name: String,
    val firstShotAfterDays: Int,
    val cycleDays: Int
)

object VaccineSchedule {
    private val SHEEP_GOAT = listOf(
        VaccineDef("PPR (Peste des Petits Ruminants)", 30, 365),
        VaccineDef("Sheep/Goat Pox", 60, 365),
        VaccineDef("Enterotoxemia", 90, 180),
        VaccineDef("FMD (Foot & Mouth)", 14, 180),
        VaccineDef("Deworming", 7, 90)
    )

    private val COW = listOf(
        VaccineDef("FMD (Foot & Mouth)", 14, 180),
        VaccineDef("HS (Haemorrhagic Septicaemia)", 30, 365),
        VaccineDef("Black Quarter", 60, 365),
        VaccineDef("Brucellosis", 90, 365),
        VaccineDef("Deworming", 7, 90)
    )

    fun forSpecies(species: String): List<VaccineDef> = when (species.lowercase()) {
        "cow", "cattle", "buffalo" -> COW
        else -> SHEEP_GOAT
    }
}
