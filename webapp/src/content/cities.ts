// Per-country city lists (department/province capitals + notable seismic towns), ported from the app's
// CountryRegions (core/domain *Regions.kt). Used to label FIRMS fire points — which carry no place —
// by their nearest city, and to bucket fires by region for the concentration chart (W3).

import type { CountryCode } from "./countries";

export interface City {
  name: string;
  lat: number;
  lon: number;
}

const CO: City[] = [
  { name: "Bogotá", lat: 4.711, lon: -74.0721 },
  { name: "Medellín", lat: 6.2442, lon: -75.5812 },
  { name: "Cali", lat: 3.4516, lon: -76.532 },
  { name: "Barranquilla", lat: 10.9685, lon: -74.7813 },
  { name: "Cartagena", lat: 10.391, lon: -75.4794 },
  { name: "Cúcuta", lat: 7.8939, lon: -72.5078 },
  { name: "Bucaramanga", lat: 7.1193, lon: -73.1227 },
  { name: "Pereira", lat: 4.8133, lon: -75.6961 },
  { name: "Santa Marta", lat: 11.2408, lon: -74.199 },
  { name: "Ibagué", lat: 4.4389, lon: -75.2322 },
  { name: "Manizales", lat: 5.0703, lon: -75.5138 },
  { name: "Villavicencio", lat: 4.142, lon: -73.6266 },
  { name: "Armenia", lat: 4.5339, lon: -75.6811 },
  { name: "Pasto", lat: 1.2136, lon: -77.2811 },
  { name: "Neiva", lat: 2.9273, lon: -75.2819 },
  { name: "Popayán", lat: 2.4448, lon: -76.6147 },
  { name: "Tunja", lat: 5.5353, lon: -73.3678 },
  { name: "Valledupar", lat: 10.4631, lon: -73.2532 },
  { name: "Montería", lat: 8.7479, lon: -75.8814 },
  { name: "Sincelejo", lat: 9.3047, lon: -75.3978 },
  { name: "Riohacha", lat: 11.5444, lon: -72.9072 },
  { name: "Quibdó", lat: 5.6947, lon: -76.6583 },
  { name: "Yopal", lat: 5.3378, lon: -72.3959 },
  { name: "Arauca", lat: 7.0847, lon: -70.7591 },
  { name: "Florencia", lat: 1.6144, lon: -75.6062 },
  { name: "Mocoa", lat: 1.1503, lon: -76.6472 },
  { name: "Leticia", lat: -4.2153, lon: -69.9406 },
  { name: "San José del Guaviare", lat: 2.5716, lon: -72.6416 },
  { name: "Inírida", lat: 3.8653, lon: -67.9239 },
  { name: "Mitú", lat: 1.2537, lon: -70.234 },
  { name: "Puerto Carreño", lat: 6.189, lon: -67.4859 },
  { name: "San Andrés", lat: 12.5847, lon: -81.7006 },
];

const ID: City[] = [
  { name: "Jakarta", lat: -6.2088, lon: 106.8456 },
  { name: "Surabaya", lat: -7.2575, lon: 112.7521 },
  { name: "Bandung", lat: -6.9175, lon: 107.6191 },
  { name: "Medan", lat: 3.5952, lon: 98.6722 },
  { name: "Semarang", lat: -6.9932, lon: 110.4203 },
  { name: "Makassar", lat: -5.1477, lon: 119.4327 },
  { name: "Palembang", lat: -2.9761, lon: 104.7754 },
  { name: "Yogyakarta", lat: -7.7956, lon: 110.3695 },
  { name: "Denpasar", lat: -8.6705, lon: 115.2126 },
  { name: "Padang", lat: -0.9471, lon: 100.4172 },
  { name: "Banda Aceh", lat: 5.5483, lon: 95.3238 },
  { name: "Mataram", lat: -8.5833, lon: 116.1167 },
  { name: "Palu", lat: -0.8917, lon: 119.8707 },
  { name: "Manado", lat: 1.4748, lon: 124.8421 },
  { name: "Ambon", lat: -3.6954, lon: 128.1814 },
  { name: "Jayapura", lat: -2.5337, lon: 140.7181 },
  { name: "Kupang", lat: -10.1772, lon: 123.607 },
  { name: "Maumere", lat: -8.6199, lon: 122.2111 },
  { name: "Ende", lat: -8.8432, lon: 121.6626 },
  { name: "Labuan Bajo", lat: -8.4964, lon: 119.8877 },
];

const ES: City[] = [
  { name: "Madrid", lat: 40.4168, lon: -3.7038 },
  { name: "Barcelona", lat: 41.3874, lon: 2.1686 },
  { name: "Valencia", lat: 39.4699, lon: -0.3763 },
  { name: "Sevilla", lat: 37.3891, lon: -5.9845 },
  { name: "Zaragoza", lat: 41.6488, lon: -0.8891 },
  { name: "Málaga", lat: 36.7213, lon: -4.4214 },
  { name: "Murcia", lat: 37.9922, lon: -1.1307 },
  { name: "Lorca", lat: 37.6712, lon: -1.7018 },
  { name: "Granada", lat: 37.1773, lon: -3.5986 },
  { name: "Almería", lat: 36.834, lon: -2.4637 },
  { name: "Alicante", lat: 38.3452, lon: -0.481 },
  { name: "Bilbao", lat: 43.263, lon: -2.935 },
  { name: "Las Palmas de Gran Canaria", lat: 28.1235, lon: -15.4363 },
  { name: "Santa Cruz de Tenerife", lat: 28.4636, lon: -16.2518 },
  { name: "Jaén", lat: 37.7796, lon: -3.7849 },
  { name: "Pamplona", lat: 42.8125, lon: -1.6458 },
];

const IT: City[] = [
  { name: "Roma", lat: 41.9028, lon: 12.4964 },
  { name: "Milano", lat: 45.4642, lon: 9.19 },
  { name: "Napoli", lat: 40.8518, lon: 14.2681 },
  { name: "Torino", lat: 45.0703, lon: 7.6869 },
  { name: "Palermo", lat: 38.1157, lon: 13.3615 },
  { name: "Genova", lat: 44.4056, lon: 8.9463 },
  { name: "Bologna", lat: 44.4949, lon: 11.3426 },
  { name: "Firenze", lat: 43.7696, lon: 11.2558 },
  { name: "Catania", lat: 37.5079, lon: 15.083 },
  { name: "Messina", lat: 38.1938, lon: 15.554 },
  { name: "Reggio Calabria", lat: 38.1113, lon: 15.6619 },
  { name: "L'Aquila", lat: 42.3498, lon: 13.3995 },
  { name: "Perugia", lat: 43.1107, lon: 12.3908 },
  { name: "Norcia", lat: 42.7924, lon: 13.0964 },
  { name: "Amatrice", lat: 42.6296, lon: 13.2896 },
  { name: "Udine", lat: 46.0711, lon: 13.2346 },
];

const PE: City[] = [
  { name: "Lima", lat: -12.0464, lon: -77.0428 },
  { name: "Callao", lat: -12.0566, lon: -77.1181 },
  { name: "Arequipa", lat: -16.409, lon: -71.5375 },
  { name: "Trujillo", lat: -8.1116, lon: -79.0288 },
  { name: "Chiclayo", lat: -6.7714, lon: -79.8409 },
  { name: "Piura", lat: -5.1945, lon: -80.6328 },
  { name: "Cusco", lat: -13.5319, lon: -71.9675 },
  { name: "Iquitos", lat: -3.7491, lon: -73.2538 },
  { name: "Huancayo", lat: -12.0686, lon: -75.2103 },
  { name: "Chimbote", lat: -9.0745, lon: -78.5936 },
  { name: "Tacna", lat: -18.0066, lon: -70.2463 },
  { name: "Ica", lat: -14.0678, lon: -75.7286 },
  { name: "Pucallpa", lat: -8.3791, lon: -74.5539 },
  { name: "Cajamarca", lat: -7.1638, lon: -78.5003 },
  { name: "Ayacucho", lat: -13.1588, lon: -74.2232 },
  { name: "Puno", lat: -15.8402, lon: -70.0219 },
  { name: "Huaraz", lat: -9.5278, lon: -77.5278 },
  { name: "Pisco", lat: -13.71, lon: -76.2036 },
  { name: "Moquegua", lat: -17.1934, lon: -70.935 },
  { name: "Tumbes", lat: -3.5669, lon: -80.4515 },
  { name: "Nazca", lat: -14.8299, lon: -74.9401 },
];

export const CITIES: Record<CountryCode, City[]> = { CO, ID, ES, IT, PE };
