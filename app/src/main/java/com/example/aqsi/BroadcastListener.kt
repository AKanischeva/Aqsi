package com.example.aqsi

import android.content.Intent

interface BroadcastListener {
    fun doSomething(intent: Intent)
}