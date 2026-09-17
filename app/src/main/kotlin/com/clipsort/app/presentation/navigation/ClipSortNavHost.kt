package com.clipsort.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.clipsort.app.presentation.categorydetail.CategoryDetailScreen
import com.clipsort.app.presentation.library.LibraryScreen

private object Routes {
    const val LIBRARY = "library"
    const val CATEGORY_DETAIL = "category/{categoryId}"
    const val CATEGORY_ID_ARG = "categoryId"

    fun categoryDetail(categoryId: Long) = "category/$categoryId"
}

@Composable
fun ClipSortNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onBrowseClips = { navController.navigate("clips") { launchSingleTop = true } },
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.categoryDetail(categoryId))
                }
            )
        }
        composable("clips") {
            CategoryDetailScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = Routes.CATEGORY_DETAIL,
            arguments = listOf(navArgument(Routes.CATEGORY_ID_ARG) { type = NavType.LongType })
        ) {
            CategoryDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
