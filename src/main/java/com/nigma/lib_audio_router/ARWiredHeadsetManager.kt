package com.nigma.lib_audio_router

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioDeviceInfo
import android.media.AudioManager
import timber.log.Timber

class ARWiredHeadsetManager(
    private val am: AudioManager,
    val callback: (plugged: Boolean) -> Unit
) {

    private var hasWiredHeadset = false
        @Synchronized
        get() {
            return isAudioJackConnected()
        }
        @Synchronized
        set(value) {
            callback(value)
            field = value
        }

    private val receiver by lazy { AudioJackReceiver() }


    fun start(context: Context) {
        Timber.d("start")
        context
            .registerReceiver(
                receiver,
                IntentFilter(
                    Intent.ACTION_HEADSET_PLUG
                )
            )
    }

    fun stop(context: Context) {
        Timber.d("stop")
        context
            .unregisterReceiver(
                receiver
            )
    }


    /**
     * This wrapper function to check whether audio jack was connected or not.
     * @see AudioManager.isWiredHeadsetOn
     * @see AudioManager.getDevices
     */
    @Suppress("DEPRECATION")
    fun isAudioJackConnected(): Boolean {
        Timber.d("isAudioJackConnected")
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            for (device in am.getDevices(AudioManager.GET_DEVICES_OUTPUTS)) {
                if (device.type == AudioDeviceInfo.TYPE_WIRED_HEADSET || device.type == AudioDeviceInfo.TYPE_WIRED_HEADPHONES) {
                    return true
                }
            }
            return false
        } else {
            return am.isWiredHeadsetOn
        }
    }


    inner class AudioJackReceiver : BroadcastReceiver() {
        override fun onReceive(p0: Context?, p1: Intent?) {
            hasWiredHeadset = isAudioJackConnected()
            Timber.d("onReceive %s", hasWiredHeadset)
        }
    }
}