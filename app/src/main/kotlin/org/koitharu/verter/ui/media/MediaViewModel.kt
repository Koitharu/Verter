package org.koitharu.verter.ui.media

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koitharu.verter.core.media.PlayerState
import org.koitharu.verter.interactor.DeviceInteractor
import org.koitharu.verter.interactor.MediaInteractor
import org.koitharu.verter.util.ErrorHandler
import javax.inject.Inject

@HiltViewModel
class MediaViewModel @Inject constructor(
	private val interactor: MediaInteractor,
	private val deviceInteractor: DeviceInteractor,
) : ViewModel() {

	private val errorHandler = ErrorHandler()
	private var rewindJob: Job? = null

	val errors
		get() = errorHandler.errors

	val metadata = interactor.observeMetadata()
		.stateIn(viewModelScope + Dispatchers.Default + errorHandler, SharingStarted.Lazily, null)

	val playerState = interactor.observeState()
		.stateIn(viewModelScope + Dispatchers.Default + errorHandler, SharingStarted.Lazily, PlayerState.UNKNOWN)

	val coverArt = metadata
		.map { it?.artUrl }
		.mapLatest { url ->
			if (url != null) {
				interactor.getCoverArtOrNull(url)
			} else {
				null
			}
		}.stateIn(viewModelScope + Dispatchers.Default + errorHandler, SharingStarted.Lazily, null)

	val isAvailable = MutableStateFlow(true)

	init {
		checkRequirements()
	}

	fun onPlayPauseClick() {
		viewModelScope.launch(errorHandler) {
			interactor.playPause()
		}
	}

	fun onNextClick() {
		viewModelScope.launch(errorHandler) {
			rewindJob?.cancelAndJoin()
			interactor.nextTrack()
		}
	}

	fun onPreviousClick() {
		viewModelScope.launch(errorHandler) {
			rewindJob?.cancelAndJoin()
			interactor.previousTrack()
		}
	}

	fun onSkip10Click() {
		val prevJob = rewindJob
		rewindJob = viewModelScope.launch(errorHandler) {
			prevJob?.join()
			interactor.skip(10)
		}
	}

	fun onRewind10Click() {
		val prevJob = rewindJob
		rewindJob = viewModelScope.launch(errorHandler) {
			prevJob?.join()
			interactor.rewind(10)
		}
	}

	fun onPositionChanged(newPositionSec: Int) {
		val prevJob = rewindJob
		rewindJob = viewModelScope.launch(errorHandler) {
			prevJob?.cancelAndJoin()
			delay(250)
			interactor.setPosition(newPositionSec)
		}
	}

	private fun checkRequirements() {
		viewModelScope.launch(errorHandler) {
			deviceInteractor.device.collect {
				isAvailable.value = interactor.isPlayerCtlAvailable()
			}
		}
	}
}