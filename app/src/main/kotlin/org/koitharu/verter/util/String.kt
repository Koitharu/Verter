package org.koitharu.verter.util

import java.math.BigInteger
import java.security.MessageDigest

fun Int.formatTimeSeconds(): String {
	if (this == 0) {
		return "00:00"
	}
	val hours = this / 3600
	val minutes = (this % 3600) / 60
	val seconds = this % 60

	return if (hours == 0) {
		"%02d:%02d".format(minutes, seconds)
	} else {
		"%02d:%02d:%02d".format(hours, minutes, seconds)
	}
}

fun String.nullIfEmpty() = ifEmpty { null }

fun String.lineCount(): Int {
	return count { x -> x == '\n' }
}

fun String.md5(): String {
	val md = MessageDigest.getInstance("MD5")
	return BigInteger(1, md.digest(toByteArray())).toString(16).padStart(32, '0')
}