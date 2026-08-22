package com.abhii.paprivo.ui.navigation

sealed class Screen(val route: String) {
    // Bottom Bar Screens
    object Home : Screen("home")
    object Create : Screen("create")
    object Settings : Screen("settings")

    // PDF Tools
    object MergePdf : Screen("tool_merge")
    object SplitPdf : Screen("tool_split")
    object CompressPdf : Screen("tool_compress")
    object ExtractPages : Screen("tool_extract")
    object DeletePages : Screen("tool_delete")
    object ReorderPages : Screen("tool_reorder")
    object RotatePdf : Screen("tool_rotate")
    object PdfToImages : Screen("tool_pdf_to_images")
    object PdfViewer : Screen("tool_viewer")
    object ScanDocument : Screen("tool_scan")

    // Create Screens
    object ImageToPdf : Screen("create_image_to_pdf")
    object TextToPdf : Screen("create_text_to_pdf")
    object BlankPdf : Screen("create_blank_pdf")

    // Settings Sub-Screens
    object SettingsAppearance : Screen("settings_appearance")
    object SettingsAnimations : Screen("settings_animations")
    object SettingsHaptics : Screen("settings_haptics")
    object SettingsPdf : Screen("settings_pdf")
    object SettingsPdfReader : Screen("settings_pdf_reader")
    object SettingsScanner : Screen("settings_scanner")
    object SettingsStorage : Screen("settings_storage")
    object SettingsPrivacy : Screen("settings_privacy")
    object SettingsBackup : Screen("settings_backup")
    object SettingsAbout : Screen("settings_about")
}
