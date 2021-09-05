package com.example.aqsi

import android.content.Intent

interface BroadcastListener {
    fun processCheck(intent: Intent)
}