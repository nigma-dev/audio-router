package com.nigma.lib_audio_router.model

import androidx.annotation.DrawableRes
import com.nigma.lib_audio_router.R

enum class AudioDevice(val deviceName: String, @DrawableRes val icon: Int) {
    SPEAKER("speaker",R.drawable.ic_speaker),
    EARPIECE("earpiece", R.drawable.ic_earpiece),
    BLUETOOTH("bluetooth", R.drawable.ic_bluetooth),
    AUDIO_JACK("headset", R.drawable.ic_headset)
}