package com.combinate.soferapp.ui

import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.combinate.soferapp.ui.screens.DashboardScreen
import com.combinate.soferapp.ui.screens.LoginScreen
import com.combinate.soferapp.ui.screens.TripSelectionScreen
import com.combinate.soferapp.ui.screens.VehicleSelectionScreen
import com.combinate.soferapp.ui.state.AppStep

@Composable
fun SoferApp(viewModel: SoferAppViewModel) {
    val state by viewModel.state.collectAsState()
    Surface(modifier = Modifier) {
        when (state.step) {
            AppStep.Login -> LoginScreen(onLogin = viewModel::login, errorMessage = state.errorMessage)
            AppStep.VehicleSelection -> VehicleSelectionScreen(
                vehicles = state.vehicles,
                onVehicleSelected = viewModel::selectVehicle
            )
            AppStep.TripSelection -> TripSelectionScreen(
                trips = state.trips,
                onTripSelected = viewModel::selectTrip
            )
            AppStep.Dashboard -> DashboardScreen(
                state = state,
                onSync = viewModel::runSync,
                onStartBoarding = viewModel::startBoarding,
                onFinishTrip = viewModel::finishTrip,
                onReinitializeCash = viewModel::reinitializeCashRegister,
                onCloseDay = viewModel::closeDay,
                onUpdateCurrentStation = viewModel::updateCurrentStation,
                onUpdateFromStation = viewModel::updateFromStation,
                onUpdateToStation = viewModel::updateToStation,
                onIssueTicket = viewModel::issueTicket,
                onMarkReservationBoarded = viewModel::markReservationBoarded,
                onMarkReservationCancelled = viewModel::markReservationCancelled,
                onRecordPassValidation = viewModel::recordPassValidation,
                onCaptureSnapshot = viewModel::captureSnapshot,
                onResetMessage = viewModel::resetMessage,
                onSetConnectivity = viewModel::setConnectivity,
                onSetGpsStatus = viewModel::setGpsStatus
            )
        }
    }
}
