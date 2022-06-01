package org.koitharu.verter.ui.files

import android.net.Uri
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koitharu.verter.core.files.FileTransferInteractor
import org.koitharu.verter.util.ErrorHandler
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
	private val interactor: FileTransferInteractor,
) : ViewModel() {

	val doneFiles = MutableSharedFlow<String>(extraBufferCapacity = 1)

	val isLoading = mutableStateOf(false)
	private val errorHandler = ErrorHandler()

	val errors
		get() = errorHandler.errors

	fun sendFile(uri: Uri) {
		viewModelScope.launch(errorHandler) {
			isLoading.value = true
			try {
				val result = withContext(Dispatchers.Default) {
					interactor.sendFile(uri)
				}
				doneFiles.emit(result)
			} finally {
				isLoading.value = false
			}
		}
	}
}