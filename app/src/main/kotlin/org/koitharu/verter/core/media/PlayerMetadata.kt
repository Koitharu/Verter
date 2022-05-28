package org.koitharu.verter.core.media

import org.koitharu.verter.util.nullIfEmpty
import java.util.concurrent.TimeUnit

data class PlayerMetadata(
	val playerName: String,
	val title: String,
	val artist: String,
	val album: String,
	val position: Int,
	val length: Int,
	val artUrl: String?,
) {

	companion object {

		private val FIELDS = arrayOf(
			"playerName",
			"title",
			"artist",
			"album",
			"position",
			"mpris:length",
			"mpris:artUrl",
		)

		val FORMAT = FIELDS.joinToString("\\|") {
			"{{$it}}"
		}

		fun tryParse(line: String): PlayerMetadata? {
			val values = line.split('|')
			if (values.size != FIELDS.size) {
				return null
			}
			return PlayerMetadata(
				playerName = values[0],
				title = values[1],
				artist = values[2],
				album = values[3],
				position = values[4].toLongOrNull()?.let { TimeUnit.MICROSECONDS.toSeconds(it).toInt() } ?: 0,
				length = values[5].toLongOrNull()?.let { TimeUnit.MICROSECONDS.toSeconds(it).toInt() } ?: 0,
				artUrl = values[6].nullIfEmpty(),
			)
		}
	}
}