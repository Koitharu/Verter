package org.koitharu.verter.core.files

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runInterruptible
import org.koitharu.verter.interactor.DeviceInteractor
import org.koitharu.verter.util.md5
import java.io.File
import javax.inject.Inject

class FileTransferInteractor @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
	@ApplicationContext context: Context,
) {

	private val contentResolver = context.contentResolver
	private val tempDir = File(context.externalCacheDir ?: context.cacheDir, "scp")

	suspend fun sendFile(uri: Uri, destination: String): String {
		val connection = deviceInteractor.requireConnection()
		if (!tempDir.exists()) {
			tempDir.mkdir()
		}
		val tempFile = File(tempDir, uri.toString().md5())
		try {
			runInterruptible(Dispatchers.IO) {
				contentResolver.openInputStream(uri)?.use { input ->
					tempFile.outputStream().use { output ->
						input.copyTo(output)
						output.flush()
					}
				}
			}
			connection.writeFile(tempFile, destination)
			return connection.execute("realpath $destination/${tempFile.name}")
		} finally {
			tempFile.delete()
		}
	}

	suspend fun sendFile(uri: Uri): String {
		val connection = deviceInteractor.requireConnection()
		val destination = connection.execute("xdg-user-dir DOWNLOAD")
		return sendFile(uri, destination)
	}
}