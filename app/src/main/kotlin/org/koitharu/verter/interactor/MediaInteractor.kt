package org.koitharu.verter.interactor

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.core.net.toUri
import javax.inject.Inject
import kotlinx.coroutines.flow.*
import org.koitharu.verter.BuildConfig
import org.koitharu.verter.core.media.PlayerMetadata
import org.koitharu.verter.core.media.PlayerState
import org.koitharu.verter.core.ssh.RemoteProcessException

class MediaInteractor @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
) {

	suspend fun isPlayerCtlAvailable(): Boolean = try {
		deviceInteractor.requireConnection().execute("playerctl --version").startsWith("v")
	} catch (e: RemoteProcessException) {
		if (e.exitCode == RemoteProcessException.EXIT_CODE_NOT_FOUND) {
			false
		} else {
			throw e
		}
	}

	suspend fun playPause() {
		deviceInteractor.requireConnection().execute("playerctl play-pause")
	}

	suspend fun skip(seconds: Int) {
		deviceInteractor.requireConnection().execute("playerctl position +$seconds")
	}

	suspend fun rewind(seconds: Int) {
		deviceInteractor.requireConnection().execute("playerctl position -$seconds")
	}

	suspend fun setPosition(seconds: Int) {
		deviceInteractor.requireConnection().execute("playerctl position $seconds")
	}

	suspend fun previousTrack() {
		deviceInteractor.requireConnection().execute("playerctl previous")
	}

	suspend fun nextTrack() {
		deviceInteractor.requireConnection().execute("playerctl next")
	}

	suspend fun isPlaying(): Boolean {
		return deviceInteractor.requireConnection().execute("playerctl status")
			.equals("Playing", ignoreCase = true)
	}

	suspend fun getPlayerStatus(): PlayerState {
		val rawValue = deviceInteractor.requireConnection().execute("playerctl status")
		return parseState(rawValue)
	}

	suspend fun getCoverArtOrNull(uri: String): Bitmap? {
		try {
			val path = uri.toUri().path ?: return null
			val byteArray = deviceInteractor.requireConnection().getFileContent(path)
			return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
		} catch (e: Throwable) {
			if (BuildConfig.DEBUG) {
				e.printStackTrace()
			}
			return null
		}
	}

	fun observeState(): Flow<PlayerState> {
		return deviceInteractor.getActiveConnectionAsFlow().flatMapLatest { connection ->
			connection?.executeContinuously("playerctl status -F")?.map { x -> parseState(x) }
				?: flowOf(PlayerState.UNKNOWN)
		}
	}

	fun observeMetadata(): Flow<PlayerMetadata?> {
		return deviceInteractor.getActiveConnectionAsFlow().flatMapLatest { connection ->
			connection?.executeContinuously("playerctl metadata -f ${PlayerMetadata.FORMAT} -F")?.map { line ->
				PlayerMetadata.tryParse(line)
			} ?: flowOf(null)
		}.distinctUntilChanged()
	}

	private fun parseState(rawValue: String): PlayerState = when (rawValue.lowercase()) {
		"playing" -> PlayerState.PLAYING
		"paused" -> PlayerState.PAUSED
		else -> PlayerState.UNKNOWN
	}
}