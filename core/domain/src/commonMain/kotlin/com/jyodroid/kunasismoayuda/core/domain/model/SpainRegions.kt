package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A compact seed list of major Spanish cities (with emphasis on the seismically active south-east:
 * Granada, Almería, Murcia/Lorca) with approximate centroids and populations, mirroring
 * [ColombiaRegions]. [Region.department] holds the autonomous community / province.
 */
object SpainRegions {
    val all: List<Region> = listOf(
        Region("Madrid", "Comunidad de Madrid", 40.4168, -3.7038, 3_300_000),
        Region("Barcelona", "Cataluña", 41.3874, 2.1686, 1_600_000),
        Region("Valencia", "Comunidad Valenciana", 39.4699, -0.3763, 800_000),
        Region("Sevilla", "Andalucía", 37.3891, -5.9845, 680_000),
        Region("Zaragoza", "Aragón", 41.6488, -0.8891, 680_000),
        Region("Málaga", "Andalucía", 36.7213, -4.4214, 580_000),
        Region("Murcia", "Región de Murcia", 37.9922, -1.1307, 460_000),
        Region("Lorca", "Región de Murcia", 37.6712, -1.7018, 95_000),
        Region("Granada", "Andalucía", 37.1773, -3.5986, 230_000),
        Region("Almería", "Andalucía", 36.8340, -2.4637, 200_000),
        Region("Alicante", "Comunidad Valenciana", 38.3452, -0.4810, 340_000),
        Region("Bilbao", "País Vasco", 43.2630, -2.9350, 350_000),
        Region("Las Palmas de Gran Canaria", "Canarias", 28.1235, -15.4363, 380_000),
        Region("Santa Cruz de Tenerife", "Canarias", 28.4636, -16.2518, 210_000),
        Region("Jaén", "Andalucía", 37.7796, -3.7849, 110_000),
        Region("Pamplona", "Navarra", 42.8125, -1.6458, 200_000),
    )
}
