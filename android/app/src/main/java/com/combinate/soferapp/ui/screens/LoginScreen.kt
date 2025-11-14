package com.combinate.soferapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    onLogin: (String) -> Unit,
    errorMessage: String?
) {
    val (driverId, setDriverId) = remember { mutableStateOf("") }
    Surface(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Autentificare șofer", modifier = Modifier.padding(bottom = 16.dp))
            OutlinedTextField(
                value = driverId,
                onValueChange = setDriverId,
                label = { Text("ID șofer") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
            Button(
                onClick = { onLogin(driverId) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text(text = "Intră în aplicație")
            }
            if (!errorMessage.isNullOrBlank()) {
                Text(text = errorMessage, modifier = Modifier.padding(top = 12.dp))
            }
        }
    }
}
