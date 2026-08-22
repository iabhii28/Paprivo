package com.abhii.paprivo.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.abhii.paprivo.ui.screens.create.*
import com.abhii.paprivo.ui.screens.home.HomeScreen
import com.abhii.paprivo.ui.screens.scanner.ScanDocumentScreen
import com.abhii.paprivo.ui.screens.settings.*
import com.abhii.paprivo.ui.screens.tools.*
import com.abhii.paprivo.ui.screens.viewer.PdfViewerScreen
import com.abhii.paprivo.ui.theme.LocalPaprivoColors
import com.abhii.paprivo.viewmodel.MainViewModel

data class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

@Composable
fun MainNavigation(
    navController: NavHostController,
    viewModel: MainViewModel
) {
    val colors = LocalPaprivoColors.current
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val bottomNavItems = listOf(
        BottomNavItem(Screen.Home.route, "Home", Icons.Outlined.Home, Icons.Outlined.Home),
        BottomNavItem(Screen.Create.route, "Create", Icons.Outlined.AddCircleOutline, Icons.Outlined.AddCircleOutline),
        BottomNavItem(Screen.Settings.route, "Settings", Icons.Outlined.Settings, Icons.Outlined.Settings)
    )

    val isTopLevelScreen = currentRoute in listOf(
        Screen.Home.route,
        Screen.Create.route,
        Screen.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (isTopLevelScreen) {
                NavigationBar(
                    containerColor = colors.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    bottomNavItems.forEach { item ->
                        val selected = currentRoute == item.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                if (currentRoute != item.route) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            icon = {
                                Icon(
                                    imageVector = item.selectedIcon,
                                    contentDescription = item.label,
                                    tint = if (selected) colors.primary else colors.secondary
                                )
                            },
                            label = {
                                Text(
                                    text = item.label,
                                    fontSize = 11.sp,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) colors.primary else colors.secondary
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                indicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                                selectedIconColor = colors.primary,
                                unselectedIconColor = colors.secondary,
                                selectedTextColor = colors.primary,
                                unselectedTextColor = colors.secondary
                            )
                        )
                    }
                }
            }
        },
        containerColor = colors.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(paddingValues),
            enterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            exitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(250)
                )
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(250)) + slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            },
            popExitTransition = {
                fadeOut(animationSpec = tween(250)) + slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(250)
                )
            }
        ) {
            // Top Level
            composable(Screen.Home.route) {
                HomeScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.Create.route) {
                CreateScreen(navController = navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }

            // Create Tools
            composable(
                route = "${Screen.ImageToPdf.route}?type={type}",
                arguments = listOf(navArgument("type") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val type = backStackEntry.arguments?.getString("type")
                ImageToPdfScreen(navController = navController, viewModel = viewModel, imageTypeFilter = type)
            }
            composable(Screen.ImageToPdf.route) {
                ImageToPdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.TextToPdf.route) {
                TextToPdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.BlankPdf.route) {
                BlankPdfScreen(navController = navController, viewModel = viewModel)
            }

            // PDF Tools
            composable(Screen.MergePdf.route) {
                MergePdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SplitPdf.route) {
                SplitPdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.CompressPdf.route) {
                CompressPdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.ExtractPages.route) {
                ExtractPagesScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.DeletePages.route) {
                DeletePagesScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.ReorderPages.route) {
                ReorderPagesScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.RotatePdf.route) {
                RotatePdfScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.PdfToImages.route) {
                PdfToImagesScreen(navController = navController, viewModel = viewModel)
            }
            composable(
                route = "${Screen.PdfViewer.route}?uri={uri}",
                arguments = listOf(androidx.navigation.navArgument("uri") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { backStackEntry ->
                val uri = backStackEntry.arguments?.getString("uri")
                PdfViewerScreen(navController = navController, viewModel = viewModel, initialUri = uri)
            }
            composable(Screen.PdfViewer.route) {
                PdfViewerScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.ScanDocument.route) {
                ScanDocumentScreen(navController = navController, viewModel = viewModel)
            }

            // Settings Screens
            composable(Screen.SettingsAppearance.route) {
                AppearanceScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsAnimations.route) {
                AnimationsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsHaptics.route) {
                HapticFeedbackScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsPdf.route) {
                PdfSettingsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsPdfReader.route) {
                PdfReaderSettingsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsScanner.route) {
                ScannerSettingsScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsStorage.route) {
                StorageScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsPrivacy.route) {
                PrivacyScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsBackup.route) {
                BackupScreen(navController = navController, viewModel = viewModel)
            }
            composable(Screen.SettingsAbout.route) {
                AboutScreen(navController = navController, viewModel = viewModel)
            }
        }
    }
}
