package com.example.testapp.di

import com.example.testapp.presentation.screen.settings.ExcelExportCoordinator
import com.example.testapp.presentation.screen.settings.ImportCoordinator
import com.example.testapp.presentation.screen.settings.JsonExportCoordinator
import com.example.testapp.presentation.screen.settings.SettingsExcelExportGateway
import com.example.testapp.presentation.screen.settings.SettingsImportGateway
import com.example.testapp.presentation.screen.settings.SettingsJsonExportGateway
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class SettingsIoBindingModule {
    @Binds
    abstract fun bindSettingsImport(impl: ImportCoordinator): SettingsImportGateway

    @Binds
    abstract fun bindSettingsJsonExport(impl: JsonExportCoordinator): SettingsJsonExportGateway

    @Binds
    abstract fun bindSettingsExcelExport(impl: ExcelExportCoordinator): SettingsExcelExportGateway
}
