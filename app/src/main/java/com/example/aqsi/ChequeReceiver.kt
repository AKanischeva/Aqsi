package com.example.aqsi

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ChequeReceiver(private val listener: BroadcastListener) : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        listener.doSomething(intent)
    }

}