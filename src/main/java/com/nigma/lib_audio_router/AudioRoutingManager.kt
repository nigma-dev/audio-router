package com.nigma.lib_audio_router

import android.bluetooth.BluetoothManager
import android.content.Context
import android.media.AudioManager
import com.nigma.lib_audio_router.callback.AudioRoutingChangesListener
import com.nigma.lib_audio_router.model.AudioDevice
import timber.log.Timber

/**
 * @author enigma
 */
class AudioRoutingManager
constructor(
    private val context: Context,
    private val callback: AudioRoutingChangesListener
) {

    /**
     *
     */
    val isAuxiliaryAudioDevice: Boolean
        get() {
            return isBluetoothConnected() || isAudioJackConnected()
        }

    var selectedDevice: AudioDevice? = null
        get() {
            return if (field == null) {
                getSystemCurrentRoutedDevice()
            } else {
                field
            }
        }

    val availableDevice = mutableListOf<AudioDevice>()


    private val defaultDevice: AudioDevice
        get() {
            return getSystemCurrentRoutedDevice()
        }

    var userDefaultDevice: AudioDevice? = null

    private val androidAudioManager: AudioManager
        get() {
            return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        }

    private val bluetoothAdapter by lazy {
        (context
            .getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager)
            .adapter
    }

    private val arBlueManager by lazy {
        ARBluetoothManager(bluetoothAdapter)
        { connected ->
            /**
             * method will be invoked when
             * bluetooth headset {connected, disconnected}
             */
            setDevice(
                if (connected)
                    AudioDevice.BLUETOOTH
                else
                    defaultDevice
            )
            Timber.d("onBluetoothDeviceStateChange connected plugged -> %s", connected)
        }
    }

    private val arAudioJackManager by lazy {
        ARWiredHeadsetManager(androidAudioManager)
        { plugged ->
            /**
             * method will be invoked when
             * audio jack {plugged, unplugged}
             */
            setDevice(
                if (plugged)
                    AudioDevice.AUDIO_JACK
                else
                    defaultDevice
            )
            Timber.d("onBluetoothDeviceStateChange plugged -> %s", plugged)
        }
    }


    fun start() {
        androidAudioManager.mode = AudioManager.MODE_IN_COMMUNICATION
        Timber.d("start")
        arBlueManager.start(context)
        arAudioJackManager.start(context)
        Timber.d("start : currentDevice %s", defaultDevice)
        updateAvailableDevices()
        setDevice(defaultDevice)
    }


    fun release() {
        try {
            arBlueManager.stop(context)
            arAudioJackManager.stop(context)
            androidAudioManager.mode = AudioManager.MODE_NORMAL
            setDevice(AudioDevice.SPEAKER)
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    @Throws(IllegalArgumentException::class)
    fun selectDevice(device: AudioDevice) {
        updateAvailableDevices()
        if (!availableDevice.contains(device)) {
            throw IllegalArgumentException("selected audio devices not available, name : ${device.name}")
        }
        setDevice(device)
    }

    fun switchDevice() {
        val device = if (selectedDevice == AudioDevice.SPEAKER)
            AudioDevice.EARPIECE
        else
            AudioDevice.SPEAKER
        selectDevice(device)
    }

    /**
     *
     */
    private fun setDevice(device: AudioDevice) {
        Timber.d("setDevice : selected device -> %s %s", device, defaultDevice)
        when (device) {
            AudioDevice.SPEAKER -> enableSpeakerState()
            AudioDevice.EARPIECE -> androidAudioManager.isSpeakerphoneOn = false
            AudioDevice.BLUETOOTH -> enableBluetoothState()
            AudioDevice.AUDIO_JACK -> enableWireHeadsetState()
        }
        selectedDevice = device
        callback.onAudioRoutedDeviceChanged(device, availableDevice)
    }

    private fun updateAvailableDevices() {
        with(availableDevice) {
            clear()
            if (isAudioJackConnected()) {
                add(AudioDevice.AUDIO_JACK)
            }
            if (isBluetoothConnected()) {
                add(AudioDevice.BLUETOOTH)
            }
            /*if (isSpeaker()) {
            }*/
            add(AudioDevice.SPEAKER)
            if (!contains(AudioDevice.AUDIO_JACK) && !contains(AudioDevice.BLUETOOTH)
            ) {
                add(AudioDevice.EARPIECE)
            }
        }
    }

    /**
     * method will return current system routed audio device
     * @return{AudioDevice}
     */
    private fun getSystemCurrentRoutedDevice(): AudioDevice {
        Timber.d("getSystemCurrentRoutedDevice")
        return when {
            isAudioJackConnected() -> AudioDevice.AUDIO_JACK
            isBluetoothConnected() -> AudioDevice.BLUETOOTH
            isSpeaker() -> userDefaultDevice ?: AudioDevice.SPEAKER
            else -> userDefaultDevice ?: AudioDevice.EARPIECE
        }
    }

    private fun enableSpeakerState() {
        androidAudioManager.isSpeakerphoneOn = true
    }

    private fun enableBluetoothState() {
        androidAudioManager.isBluetoothScoOn = true
        androidAudioManager.isSpeakerphoneOn = false
    }

    private fun enableWireHeadsetState() {
        androidAudioManager.isSpeakerphoneOn = false
    }

    private fun isBluetoothConnected(): Boolean {
        return arBlueManager.isBluetoothConnected(androidAudioManager)
    }

    private fun isAudioJackConnected(): Boolean {
        return arAudioJackManager.isAudioJackConnected()
    }

    private fun isSpeaker(): Boolean {
        return androidAudioManager.isSpeakerphoneOn
    }

}