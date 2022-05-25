package org.koitharu.verter.interactor

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koitharu.verter.core.actions.RemoteAction
import org.koitharu.verter.core.db.AppDatabase
import org.koitharu.verter.core.db.entity.ActionEntity
import org.koitharu.verter.core.db.entity.toAction
import javax.inject.Inject

class ActionsInteractor @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
	private val database: AppDatabase,
) {

	fun observeActions(): Flow<List<RemoteAction>> {
		return deviceInteractor.device.flatMapLatest { device ->
			if (device == null) {
				database.actionsDao.observeAllCommon()
			} else {
				database.actionsDao.observeAll(device.id)
			}
		}.map { entities ->
			entities.map { x -> x.toAction() }
		}
	}

	suspend fun createAction(name: String, cmdline: String) {
		val entity = ActionEntity(
			id = 0,
			name = name,
			cmdline = cmdline,
			deviceId = null,
			enabled = true,
			orderKey = 0,
		)
		database.actionsDao.insert(entity)
	}

	suspend fun executeAction(action: RemoteAction): String? {
		return deviceInteractor.execute(action.cmdline)
	}
}