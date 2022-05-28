package org.koitharu.verter.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive

fun tickerFlow(delayMs: Long): Flow<Long> = channelFlow {
	while (isActive && !trySend(System.currentTimeMillis()).isClosed) {
		delay(delayMs)
	}
}

fun <T> Flow<T>.chunked(size: Int): Flow<List<T>> = flow {
	var accumulator = ArrayList<T>(size)
	collect { value ->
		accumulator.add(value)
		if (accumulator.size == size) {
			emit(accumulator)
			accumulator = ArrayList(size)
		}
	}
}