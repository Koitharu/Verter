package org.koitharu.verter.interactor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import kotlinx.coroutines.flow.*
import org.koitharu.verter.BuildConfig
import org.koitharu.verter.core.media.PlayerMetadata
import org.koitharu.verter.core.media.PlayerState
import org.koitharu.verter.core.ssh.RemoteProcessException
import javax.inject.Inject

class MediaInteractor @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
) {

	suspend fun isPlayerCtlAvailable(): Boolean {
		try {
			return deviceInteractor.execute("playerctl --version")?.startsWith("v") ?: true
		} catch (e: RemoteProcessException) {
			if (e.exitCode == RemoteProcessException.EXIT_CODE_NOT_FOUND) {
				return false
			} else {
				throw e
			}
		}
	}

	suspend fun playPause() {
		deviceInteractor.execute("playerctl play-pause")
	}

	suspend fun skip(seconds: Int) {
		deviceInteractor.execute("playerctl position +$seconds")
	}

	suspend fun rewind(seconds: Int) {
		deviceInteractor.execute("playerctl position -$seconds")
	}

	suspend fun setPosition(seconds: Int) {
		deviceInteractor.execute("playerctl position $seconds")
	}

	suspend fun previousTrack() {
		deviceInteractor.execute("playerctl previous")
	}

	suspend fun nextTrack() {
		deviceInteractor.execute("playerctl next")
	}

	suspend fun isPlaying(): Boolean {
		return deviceInteractor.execute("playerctl status")?.equals("Playing", ignoreCase = true) ?: false
	}

	suspend fun getPlayerStatus(): PlayerState {
		val rawValue = deviceInteractor.execute("playerctl status") ?: return PlayerState.UNKNOWN
		return parseState(rawValue)
	}

	suspend fun getCoverArtOrNull(uri: String): Bitmap? {
		try {
			val path = uri.toUri().path ?: return null
			val byteArray = deviceInteractor.getFileContent(path) ?: return null
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
		} catch (e: Throwable) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace()
			}
			return null
		}
	}

	fun observeState(): Flow<PlayerState> {
		return deviceInteractor.device.flatMapLatest { device ->
			if (device == null) {
				flowOf(PlayerState.UNKNOWN)
			} else {
				deviceInteractor.executeContinuously("playerctl status -F")
					.map { x -> parseState(x) }
			}
		}
	}

	fun observeMetadata(): Flow<PlayerMetadata?> {
		return deviceInteractor.device.flatMapLatest { device ->
			if (device == null) {
				flowOf(null)
			} else {
				deviceInteractor.executeContinuously("playerctl metadata -f ${PlayerMetadata.FORMAT} -F")
					.map { line ->
						PlayerMetadata.tryParse(line)
					}
			}
		}.distinctUntilChanged()
	}

	private fun parseState(rawValue: String): PlayerState = when (rawValue.lowercase()) {
		"playing" -> PlayerState.PLAYING
		"paused" -> PlayerState.PAUSED
		else -> PlayerState.UNKNOWN
	}
}