package com.example.nammaplatform.model

data class Train(
    val train_number: String,
    val train_name: String,
    val train_name_kn: String,
    val arrival_time: String,
    val platform: Int,
    val destination: String,
    val destination_kn: String,
    val coaches: List<String>
)

data class Station(
    val id: String,
    val name: String,
    val name_kn: String,
    val trains: List<Train>
)

data class TrainsData(
    val stations: List<Station>
)
