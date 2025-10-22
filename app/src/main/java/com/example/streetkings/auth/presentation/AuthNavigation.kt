package com.example.streetkings.auth.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.streetkings.map.presentation.MapScreen
import androidx.navigation.compose.navigation
import com.example.streetkings.map.presentation.ParkTableScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.navigation
import com.example.streetkings.leaderboard.presentation.LeaderboardScreen
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.example.streetkings.map.presentation.ParkDetailScreen


object Routes {
    const val AUTH_GRAPH = "auth_graph"
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val MAP_SCREEN = "map_screen"
    const val PARK_TABLE_SCREEN = "park_table_screen"
    const val LEADERBOARD_SCREEN = "leaderboard_screen"
    const val PARK_DETAIL_SCREEN = "park_detail/{parkId}"
    const val WELCOME = "welcome"
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()


    NavHost(
        navController = navController,
        startDestination = Routes.AUTH_GRAPH
    ) {

        navigation(startDestination = Routes.WELCOME, route = Routes.AUTH_GRAPH) {

            composable(Routes.WELCOME) {
                WelcomeScreen(
                    onLoginClick = { navController.navigate(Routes.LOGIN) },
                    onRegisterClick = { navController.navigate(Routes.REGISTER) }
                )
            }

            composable(Routes.LOGIN) {
                LoginScreen(

                    onRegisterClick = { navController.navigate(Routes.REGISTER) },


                    onLoginSuccess = {
                        navController.navigate(Routes.MAP_SCREEN) {
                            popUpTo(Routes.AUTH_GRAPH) { inclusive = true }
                        }
                    }
                )
            }

            composable(Routes.REGISTER) {
                RegisterScreen(
                    onLoginClick = {

                        navController.navigate(Routes.LOGIN) {

                            popUpTo(Routes.AUTH_GRAPH) {
                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                )
            }
        }

            composable(Routes.MAP_SCREEN) {
                MapScreen(navController = navController)
            }


            composable(Routes.LEADERBOARD_SCREEN) {
                LeaderboardScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }


            composable(
                route = Routes.PARK_DETAIL_SCREEN,
                arguments = listOf(navArgument("parkId") { type = NavType.StringType })
            ) {
                ParkDetailScreen(navController = navController)
            }


            composable(Routes.PARK_TABLE_SCREEN) {
                ParkTableScreen(
                    onNavigateUp = { navController.navigateUp() }
                )
            }
        }
    }