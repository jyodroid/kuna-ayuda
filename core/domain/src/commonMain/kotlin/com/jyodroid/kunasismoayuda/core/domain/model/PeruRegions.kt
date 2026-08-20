package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A compact seed list of major Peruvian cities plus the country's well-known seismic hot-spots.
 * Peru sits on the Nazca–South American subduction zone and is one of the most seismically active
 * countries in the region: the Áncash 1970 disaster (Huaraz / Yungay), Pisco/Ica 2007, Arequipa 2001,
 * Nazca 1996, and frequent southern-coast (Tacna/Moquegua) and Lima events. Coordinates are approximate
 * centroids with rough populations, mirroring [ColombiaRegions]/[ItalyRegions]. [Region.department]
 * holds the region (departamento) the city belongs to.
 */
object PeruRegions {
    val all: List<Region> = listOf(
        Region("Lima", "Lima", -12.0464, -77.0428, 9_750_000),
        Region("Callao", "Callao", -12.0566, -77.1181, 1_130_000),
        Region("Arequipa", "Arequipa", -16.4090, -71.5375, 1_080_000),
        Region("Trujillo", "La Libertad", -8.1116, -79.0288, 920_000),
        Region("Chiclayo", "Lambayeque", -6.7714, -79.8409, 600_000),
        Region("Piura", "Piura", -5.1945, -80.6328, 480_000),
        Region("Cusco", "Cusco", -13.5319, -71.9675, 430_000),
        Region("Iquitos", "Loreto", -3.7491, -73.2538, 470_000),
        Region("Huancayo", "Junín", -12.0686, -75.2103, 380_000),
        Region("Chimbote", "Áncash", -9.0745, -78.5936, 370_000),
        Region("Tacna", "Tacna", -18.0066, -70.2463, 300_000),
        Region("Ica", "Ica", -14.0678, -75.7286, 280_000),
        Region("Pucallpa", "Ucayali", -8.3791, -74.5539, 320_000),
        Region("Cajamarca", "Cajamarca", -7.1638, -78.5003, 220_000),
        Region("Ayacucho", "Ayacucho", -13.1588, -74.2232, 180_000),
        Region("Puno", "Puno", -15.8402, -70.0219, 140_000),
        Region("Huaraz", "Áncash", -9.5278, -77.5278, 120_000),
        Region("Pisco", "Ica", -13.7100, -76.2036, 105_000),
        Region("Moquegua", "Moquegua", -17.1934, -70.9350, 75_000),
        Region("Tumbes", "Tumbes", -3.5669, -80.4515, 120_000),
        Region("Nazca", "Ica", -14.8299, -74.9401, 27_000),
    )
}
