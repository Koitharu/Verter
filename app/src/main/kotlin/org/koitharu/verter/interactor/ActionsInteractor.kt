package org.koitharu.verter.interactor

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import org.koitharu.verter.core.actions.RemoteAction
import org.koitharu.verter.core.db.AppDatabase
import org.koitharu.verter.core.db.entity.ActionEntity
import org.koitharu.verter.core.db.entity.toAction
import org.koitharu.verter.core.ssh.tryExecute
import javax.inject.Inject

class ActionsInteractor @Inject constructor(
	private val deviceInteractor: DeviceInteractor,
	private val database: AppDatabase,
) {

	fun observeActions(): Flow<List<RemoteAction>> {
		return deviceInteractor.getCurrentDeviceAsFlow().flatMapLatest { device ->
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

	suspend fun updateAction(id: Int, name: String, cmdline: String) {
		database.withTransaction {
			val dao = database.actionsDao
			val entity = dao.get(id).copy(
				name = name,
				cmdline = cmdline,
			)
			dao.update(entity)
		}
	}

	suspend fun getAction(id: Int): RemoteAction {
		return database.actionsDao.get(id).toAction()
	}

	suspend fun deleteActions(ids: Collection<Int>) {
		val dao = database.actionsDao
		database.withTransaction {
			for (id in ids) {
				dao.delete(id)
			}
		}
	}

	suspend fun executeAction(action: RemoteAction): String {
		return deviceInteractor.requireConnection().execute(action.cmdline)
	}

	suspend fun getCompletion(cmdline: String): List<String>? {
		if (cmdline.startsWith('-')) {
			return null
		}
		val conn = deviceInteractor.currentConnection ?: return null
		val res = conn.tryExecute("compgen -c $cmdline").getOrNull() ?: return null
		return res.lines().distinct()
	}
}