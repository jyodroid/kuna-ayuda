package com.jyodroid.kunasismoayuda.core.domain.model

/**
 * A compact seed list of major Italian cities plus the country's well-known seismic hot-spots
 * (the Central Apennines — L'Aquila / Amatrice / Norcia, 2009 & 2016; Friuli, 1976; Irpinia, 1980;
 * the Messina Strait & Calabria, 1908; Emilia, 2012; Etna/Catania), with approximate centroids and
 * populations, mirroring [ColombiaRegions]/[SpainRegions]. [Region.department] holds the region
 * (regione) the city belongs to.
 */
object ItalyRegions {
    val all: List<Region> = listOf(
        Region("Roma", "Lazio", 41.9028, 12.4964, 2_760_000),
        Region("Milano", "Lombardia", 45.4642, 9.1900, 1_370_000),
        Region("Napoli", "Campania", 40.8518, 14.2681, 910_000),
        Region("Torino", "Piemonte", 45.0703, 7.6869, 850_000),
        Region("Palermo", "Sicilia", 38.1157, 13.3615, 630_000),
        Region("Genova", "Liguria", 44.4056, 8.9463, 560_000),
        Region("Bologna", "Emilia-Romagna", 44.4949, 11.3426, 390_000),
        Region("Firenze", "Toscana", 43.7696, 11.2558, 380_000),
        Region("Catania", "Sicilia", 37.5079, 15.0830, 300_000),
        Region("Messina", "Sicilia", 38.1938, 15.5540, 220_000),
        Region("Reggio Calabria", "Calabria", 38.1113, 15.6619, 170_000),
        Region("L'Aquila", "Abruzzo", 42.3498, 13.3995, 70_000),
        Region("Perugia", "Umbria", 43.1107, 12.3908, 165_000),
        Region("Norcia", "Umbria", 42.7924, 13.0964, 4_500),
        Region("Amatrice", "Lazio", 42.6296, 13.2896, 2_500),
        Region("Udine", "Friuli-Venezia Giulia", 46.0711, 13.2346, 99_000),
    )
}
