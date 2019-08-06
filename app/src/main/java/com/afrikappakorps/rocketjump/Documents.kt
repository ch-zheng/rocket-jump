package com.afrikappakorps.rocketjump

import com.google.firebase.Timestamp

data class LeaderEntry(
    val name: String? = null,
    val score: Long? = null,
    val timestamp: Timestamp? = null
)