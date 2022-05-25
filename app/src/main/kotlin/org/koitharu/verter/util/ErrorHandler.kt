package org.koitharu.verter.util

import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class ErrorHandler : CoroutineExceptionHandler {

	private val outFlow = MutableSharedFlow<Throwable>(extraBufferCapacity = 1)

	val errors: SharedFlow<Throwable>
		get() = outFlow.asSharedFlow()

	override val key: CoroutineContext.Key<*> = CoroutineExceptionHandler

	override fun handleException(context: CoroutineContext, exception: Throwable) {
		outFlow.tryEmit(exception)
	}
}