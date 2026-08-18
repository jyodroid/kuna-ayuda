package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A compact seed list of major Indonesian cities (province capitals / seismic hotspots) with
 * approximate centroids and populations, mirroring [ColombiaRegions]. Used to estimate affected
 * areas and prioritize the quake feed. [Region.department] holds the province.
 */
object IndonesiaRegions {
    val all: List<Region> = listOf(
        Region("Jakarta", "DKI Jakarta", -6.2088, 106.8456, 10_600_000),
        Region("Surabaya", "Jawa Timur", -7.2575, 112.7521, 2_900_000),
        Region("Bandung", "Jawa Barat", -6.9175, 107.6191, 2_500_000),
        Region("Medan", "Sumatera Utara", 3.5952, 98.6722, 2_400_000),
        Region("Semarang", "Jawa Tengah", -6.9932, 110.4203, 1_700_000),
        Region("Makassar", "Sulawesi Selatan", -5.1477, 119.4327, 1_400_000),
        Region("Palembang", "Sumatera Selatan", -2.9761, 104.7754, 1_700_000),
        Region("Yogyakarta", "DI Yogyakarta", -7.7956, 110.3695, 420_000),
        Region("Denpasar", "Bali", -8.6705, 115.2126, 930_000),
        Region("Padang", "Sumatera Barat", -0.9471, 100.4172, 910_000),
        Region("Banda Aceh", "Aceh", 5.5483, 95.3238, 250_000),
        Region("Mataram", "Nusa Tenggara Barat", -8.5833, 116.1167, 430_000),
        Region("Palu", "Sulawesi Tengah", -0.8917, 119.8707, 370_000),
        Region("Manado", "Sulawesi Utara", 1.4748, 124.8421, 450_000),
        Region("Ambon", "Maluku", -3.6954, 128.1814, 350_000),
        Region("Jayapura", "Papua", -2.5337, 140.7181, 400_000),
        // Nusa Tenggara Timur / Flores — seismically very active (Ende, Maumere, Labuan Bajo, Kupang).
        Region("Kupang", "Nusa Tenggara Timur", -10.1772, 123.6070, 440_000),
        Region("Maumere", "Nusa Tenggara Timur", -8.6199, 122.2111, 85_000),
        Region("Ende", "Nusa Tenggara Timur", -8.8432, 121.6626, 90_000),
        Region("Labuan Bajo", "Nusa Tenggara Timur", -8.4964, 119.8877, 15_000),
    )
}
