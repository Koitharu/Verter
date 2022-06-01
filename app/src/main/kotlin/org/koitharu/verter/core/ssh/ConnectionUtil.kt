package org.koitharu.verter.core.ssh

import com.trilead.ssh2.ChannelCondition
import com.trilead.ssh2.Connection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.ByteArrayOutputStream
import java.io.File

@Throws(RemoteProcessException::class)
fun Connection.execute(cmdline: String): String {
	return openSession().use { session ->
		session.execCommand(cmdline)
		session.waitForCondition(ChannelCondition.EXIT_STATUS, 5_000L)
		if (session.exitStatus == 0) {
			session.stdout.bufferedReader().use { it.readText() }.trim()
		} else {
			val errMsg = session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
			throw RemoteProcessException(
				exitCode = session.exitStatus,
				message = errMsg,
			)
		}
	}
}

fun Connection.executeContinuously(cmdline: String): Flow<String> {
	return channelFlow {
		openSession().use { session ->
			session.execCommand(cmdline)
			invokeOnClose {
				session.close()
			}
			session.stdout.bufferedReader().use {
				for (line in it.lineSequence()) {
					val result = trySend(line.trim())
					if (result.isClosed) {
						break
					}
				}
			}
			if (session.exitStatus == 0) {
				close()
			} else {
				val errMsg = session.stderr.bufferedReader().use { it.readText() }.trim().ifEmpty { null }
				close(
					RemoteProcessException(
						exitCode = session.exitStatus,
						message = errMsg,
					)
				)
			}
		}
	}
}

fun Connection.getFileContent(path: String): ByteArray {
	val client = createSCPClient()
	val output = ByteArrayOutputStream()
	client.get(path, output)
	return output.toByteArray()
}

fun Connection.writeFile(file: File, targetDirectory: String) {
	val client = createSCPClient()
	client.put(file.absolutePath, targetDirectory)
}