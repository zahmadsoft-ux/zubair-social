package com.zubair.social.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.zubair.social.ui.auth.LoginScreen
import com.zubair.social.ui.auth.SignUpScreen
import com.zubair.social.ui.home.HomeScreen

object Routes {
    const val Login = "login"
    const val SignUp = "signup"
    const val Home = "home"
}

/** Login/SignUp se Home: auth screens back stack se hata do, taake back par wapas na aayein. */
private fun NavHostController.goHome() = navigate(Routes.Home) {
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
    // Session check: Firebase user ko disk par yaad rakhta hai, isliye restart ke baad bhi milta hai.
    val start = if (FirebaseAuth.getInstance().currentUser == null) Routes.Login else Routes.Home

    NavHost(navController, startDestination = start, modifier = modifier) {
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
        composable(Routes.Home) {
            HomeScreen(onLogout = {
                FirebaseAuth.getInstance().signOut()
                navController.goLogin()
            })
        }
    }
}
