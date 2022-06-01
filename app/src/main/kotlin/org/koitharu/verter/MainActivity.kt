package org.koitharu.verter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import org.koitharu.verter.ui.actions.editor.ActionEditor
import org.koitharu.verter.ui.common.NavBridge
import org.koitharu.verter.ui.common.theme.VerterTheme
import org.koitharu.verter.ui.devices.DeviceEditorScreen
import org.koitharu.verter.ui.main.MainScreen
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

	@Inject
	lateinit var navBridge: NavBridge

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContent {
			VerterTheme(dynamicColor = true) {
				val navController = rememberNavController()
				navBridge.bind(navController)
				NavHost(navController = navController, startDestination = NavBridge.Target.MAIN) {
					composable(NavBridge.Target.MAIN) { MainScreen() }
					composable(NavBridge.Target.DEVICE_EDITOR) { DeviceEditorScreen(navController) }
					composable(NavBridge.Target.ACTION_EDITOR) { backStackEntry ->
						val actionId = backStackEntry.arguments?.getString("action_id")?.toIntOrNull() ?: 0
						ActionEditor(navController, actionId)
					}
				}
			}
		}
	}
}