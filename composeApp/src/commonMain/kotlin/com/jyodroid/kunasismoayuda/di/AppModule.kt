package com.jyodroid.kunasismoayuda.di

import com.jyodroid.kunasismoayuda.core.beacon.EmergencyBeacon
import com.jyodroid.kunasismoayuda.core.beacon.createBeaconDevice
import com.jyodroid.kunasismoayuda.core.data.di.dataModule
import com.jyodroid.kunasismoayuda.core.location.createLocationProvider
import com.jyodroid.kunasismoayuda.core.domain.usecase.GetQuakesUseCase
import com.jyodroid.kunasismoayuda.core.domain.usecase.IdentifyAftershocksUseCase
import com.jyodroid.kunasismoayuda.core.domain.usecase.PrioritizeByRegionUseCase
import com.jyodroid.kunasismoayuda.ui.admin.AdminViewModel
import com.jyodroid.kunasismoayuda.ui.auth.AuthViewModel
import com.jyodroid.kunasismoayuda.ui.board.BoardViewModel
import com.jyodroid.kunasismoayuda.ui.fires.FiresViewModel
import com.jyodroid.kunasismoayuda.ui.moderation.ModerationViewModel
import com.jyodroid.kunasismoayuda.ui.quakes.QuakesViewModel
import com.jyodroid.kunasismoayuda.ui.search.SafeViewModel
import com.jyodroid.kunasismoayuda.ui.search.SearchViewModel
import com.jyodroid.kunasismoayuda.ui.settings.AppSettingsViewModel
import com.jyodroid.kunasismoayuda.ui.shelters.ShelterAdminViewModel
import com.jyodroid.kunasismoayuda.ui.shelters.SheltersViewModel
import com.jyodroid.kunasismoayuda.ui.sos.SosResponderViewModel
import com.jyodroid.kunasismoayuda.ui.sos.SosViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    includes(dataModule)

    single { createLocationProvider() }
    single { createBeaconDevice() }
    single { EmergencyBeacon(get()) }
    single { PrioritizeByRegionUseCase() }
    single { IdentifyAftershocksUseCase() }
    single { GetQuakesUseCase(get(), get()) }
    viewModelOf(::QuakesViewModel)
    viewModelOf(::FiresViewModel)
    viewModelOf(::SheltersViewModel)
    viewModelOf(::ShelterAdminViewModel)
    viewModelOf(::BoardViewModel)
    viewModelOf(::SearchViewModel)
    viewModelOf(::SafeViewModel)
    viewModelOf(::SosViewModel)
    viewModelOf(::SosResponderViewModel)
    viewModelOf(::AuthViewModel)
    viewModelOf(::AdminViewModel)
    viewModelOf(::ModerationViewModel)
    viewModelOf(::AppSettingsViewModel)
}
