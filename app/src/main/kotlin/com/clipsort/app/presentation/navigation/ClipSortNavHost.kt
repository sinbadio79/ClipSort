package com.clipsort.app.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.clipsort.app.presentation.categorydetail.CategoryDetailScreen
import com.clipsort.app.presentation.library.LibraryScreen

private object Routes {
    const val LIBRARY = "library"
    const val CATEGORY_DETAIL = "category/{categoryId}"

    fun categoryDetail(categoryId: Long) = "category/$categoryId"
}

@Composable
fun ClipSortNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.LIBRARY) {
        composable(Routes.LIBRARY) {
            LibraryScreen(
                onCategoryClick = { categoryId ->
                    navController.navigate(Routes.categoryDetail(categoryId))
                }
            )
        }
        composable(Routes.CATEGORY_DETAIL) {
            CategoryDetailScreen()
        }
    }
}
