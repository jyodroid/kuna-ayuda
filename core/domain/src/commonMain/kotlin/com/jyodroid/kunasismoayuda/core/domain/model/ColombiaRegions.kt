package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * Colombian department capitals (all 32 + the capital district) with approximate centroids and
 * populations. Used to estimate affected areas, prioritize the quake feed, and name the nearest place
 * for a wildfire hotspot. Full national coverage matters for the fire feed: FIRMS points land anywhere
 * — including the Amazon/Orinoquía — so the sparse "major cities" list left remote fires unnamed. A
 * fuller DANE-sourced municipality dataset can replace this later without changing the API.
 */
object ColombiaRegions {
    val all: List<Region> = listOf(
        // Andean / Caribbean / Pacific capitals
        Region("Bogotá", "Cundinamarca", 4.7110, -74.0721, 7_400_000),
        Region("Medellín", "Antioquia", 6.2442, -75.5812, 2_500_000),
        Region("Cali", "Valle del Cauca", 3.4516, -76.5320, 2_200_000),
        Region("Barranquilla", "Atlántico", 10.9685, -74.7813, 1_200_000),
        Region("Cartagena", "Bolívar", 10.3910, -75.4794, 1_000_000),
        Region("Cúcuta", "Norte de Santander", 7.8939, -72.5078, 780_000),
        Region("Bucaramanga", "Santander", 7.1193, -73.1227, 580_000),
        Region("Pereira", "Risaralda", 4.8133, -75.6961, 470_000),
        Region("Santa Marta", "Magdalena", 11.2408, -74.1990, 500_000),
        Region("Ibagué", "Tolima", 4.4389, -75.2322, 530_000),
        Region("Manizales", "Caldas", 5.0703, -75.5138, 400_000),
        Region("Villavicencio", "Meta", 4.1420, -73.6266, 530_000),
        Region("Armenia", "Quindío", 4.5339, -75.6811, 300_000),
        Region("Pasto", "Nariño", 1.2136, -77.2811, 390_000),
        Region("Neiva", "Huila", 2.9273, -75.2819, 360_000),
        Region("Popayán", "Cauca", 2.4448, -76.6147, 320_000),
        Region("Tunja", "Boyacá", 5.5353, -73.3678, 170_000),
        Region("Valledupar", "Cesar", 10.4631, -73.2532, 490_000),
        Region("Montería", "Córdoba", 8.7479, -75.8814, 490_000),
        Region("Sincelejo", "Sucre", 9.3047, -75.3978, 280_000),
        Region("Riohacha", "La Guajira", 11.5444, -72.9072, 280_000),
        Region("Quibdó", "Chocó", 5.6947, -76.6583, 130_000),
        Region("Yopal", "Casanare", 5.3378, -72.3959, 150_000),
        Region("Arauca", "Arauca", 7.0847, -70.7591, 90_000),
        // Amazon / Orinoquía / insular capitals — sparse, but the only anchors for remote fires
        Region("Florencia", "Caquetá", 1.6144, -75.6062, 180_000),
        Region("Mocoa", "Putumayo", 1.1503, -76.6472, 45_000),
        Region("Leticia", "Amazonas", -4.2153, -69.9406, 48_000),
        Region("San José del Guaviare", "Guaviare", 2.5716, -72.6416, 68_000),
        Region("Inírida", "Guainía", 3.8653, -67.9239, 30_000),
        Region("Mitú", "Vaupés", 1.2537, -70.2340, 30_000),
        Region("Puerto Carreño", "Vichada", 6.1890, -67.4859, 18_000),
        Region("San Andrés", "San Andrés y Providencia", 12.5847, -81.7006, 65_000),
    )
}
