package com.zubair.social.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.ui.auth.LoginScreen
import com.zubair.social.ui.auth.SignUpScreen
import com.zubair.social.ui.common.PlaceholderScreen
import com.zubair.social.ui.components.BottomNavBar
import com.zubair.social.ui.components.NavTab
import com.zubair.social.ui.profile.ProfileScreen

object Routes {
    const val Login = "login"
    const val SignUp = "signup"
}

/** Har tab ka route. Tab -> route mapping yahan, taake BottomNavBar component navigation se alag rahe. */
private fun NavTab.route() = name.lowercase()

/** Login/SignUp se Home: auth screens back stack se hata do, taake back par wapas na aayein. */
private fun NavHostController.goHome() = navigate(NavTab.Home.route()) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}

private fun NavHostController.goLogin() = navigate(Routes.Login) {
    popUpTo(graph.id) { inclusive = true }
    launchSingleTop = true
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val selectedTab = NavTab.entries.firstOrNull { tab ->
        backStackEntry?.destination?.hierarchy?.any { it.route == tab.route() } == true
    }

    // Session check: Firebase user ko disk par yaad rakhta hai, isliye restart ke baad bhi milta hai.
    val start = if (FirebaseAuth.getInstance().currentUser == null) Routes.Login else NavTab.Home.route()

    Scaffold(
        modifier = modifier,
        // Bottom bar sirf tab screens par; Login/SignUp par nahi.
        bottomBar = {
            if (selectedTab != null) {
                BottomNavBar(selected = selectedTab, onSelect = { tab ->
                    navController.navigate(tab.route()) {
                        // Tabs ke beech jaate hue stack lamba na ho: Home tak pop, har tab ki state save/restore.
                        popUpTo(NavTab.Home.route()) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                })
            }
        },
    ) { innerPadding ->
        NavHost(navController, startDestination = start, modifier = Modifier.padding(innerPadding)) {
            composable(Routes.Login) {
                LoginScreen(
                    onLoggedIn = navController::goHome,
                    onSignUpClick = { navController.navigate(Routes.SignUp) },
                )
            }
            composable(Routes.SignUp) {
                SignUpScreen(
                    onSignedUp = navController::goHome,
                    onLoginClick = { navController.popBackStack() },
                )
            }
            composable(NavTab.Home.route()) { PlaceholderScreen("Home") }
            composable(NavTab.Search.route()) { PlaceholderScreen("Search") }
            composable(NavTab.Create.route()) { PlaceholderScreen("Create") }
            composable(NavTab.Activity.route()) { PlaceholderScreen("Activity") }
            composable(NavTab.Profile.route()) {
                ProfileScreen(onLogout = {
                    FirebaseAuth.getInstance().signOut()
                    navController.goLogin()
                })
            }
        }
    }
}
