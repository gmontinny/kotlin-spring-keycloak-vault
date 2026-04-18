package br.com.springbootkeycloakoauth2.entity

import jakarta.persistence.*

@Entity
@Table(name = "geolocations")
class Geolocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "geolocation_zip_code_prefix", length = 10, nullable = false)
    val zipCodePrefix: String,

    @Column(name = "geolocation_lat", nullable = false)
    val lat: Double,

    @Column(name = "geolocation_lng", nullable = false)
    val lng: Double,

    @Column(name = "geolocation_city", length = 100, nullable = false)
    val city: String,

    @Column(name = "geolocation_state", length = 2, nullable = false)
    val state: String
)
