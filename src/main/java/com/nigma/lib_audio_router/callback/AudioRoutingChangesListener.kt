package com.nigma.lib_audio_router.callback

import com.nigma.lib_audio_router.model.AudioDevice

interface AudioRoutingChangesListener {

    fun onAudioRoutedDeviceChanged(selectedDevice: AudioDevice, availableDevice: List<AudioDevice>)

}