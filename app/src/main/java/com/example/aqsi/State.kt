package com.example.aqsi

sealed class State {
    class Start : State()
    class Loading : State()
    class Error(val message: String) : State()
    class Ready() : State()
}