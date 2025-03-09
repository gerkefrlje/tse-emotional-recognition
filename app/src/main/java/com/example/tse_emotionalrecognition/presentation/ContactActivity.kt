package com.example.tse_emotionalrecognition.presentation

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.provider.ContactsContract
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Create an extension property for DataStore.
private val Context.dataStore by preferencesDataStore(name = "contact_settings")

// Keys for the contact configuration.
private val CONTACT_NAME_KEY = stringPreferencesKey("contact_name")
private val CONTACT_PHONE_KEY = stringPreferencesKey("contact_phone")

class ContactActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setTheme(android.R.style.Theme_DeviceDefault)

        setContent {
            MaterialTheme {
                ContactScreen()
            }
        }
    }

    @Composable
    fun ContactScreen() {
        // Get the current context.
        val context = this
        // Create a coroutine scope for saving or clearing data.
        val scope = rememberCoroutineScope()

        // Mutable state to hold the configured contact.
        var contactName by remember { mutableStateOf("") }
        var contactPhone by remember { mutableStateOf("") }
        var isConfigured by remember { mutableStateOf(false) }

        // Load data from DataStore when the composable is first launched.
        LaunchedEffect(Unit) {
            context.dataStore.data.collect { preferences ->
                contactName = preferences[CONTACT_NAME_KEY] ?: ""
                contactPhone = preferences[CONTACT_PHONE_KEY] ?: ""
                isConfigured = contactName.isNotEmpty() && contactPhone.isNotEmpty()
            }
        }

        // Conditional UI based on whether a contact has been configured.
        if (isConfigured) {
            CallScreen(
                contactName = contactName,
                contactPhone = contactPhone,
                onCall = {
                    // Launch an intent to dial the configured phone number.
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$contactPhone")
                    }
                    startActivity(intent)
                },
                onReconfigure = {
                    // Clear the saved contact from DataStore.
                    scope.launch {
                        context.dataStore.edit { preferences ->
                            preferences.remove(CONTACT_NAME_KEY)
                            preferences.remove(CONTACT_PHONE_KEY)
                        }
                    }
                }
            )
        } else {
            ConfigureScreen(onSave = { name, phone ->
                // Save the contact details to DataStore.
                scope.launch {
                    context.dataStore.edit { preferences ->
                        preferences[CONTACT_NAME_KEY] = name
                        preferences[CONTACT_PHONE_KEY] = phone
                    }
                }
            })
        }
    }

    private suspend fun getContactDetails(context: Context, uri: Uri): Pair<String, String>? {
        return withContext(Dispatchers.IO) {
            val cursor = context.contentResolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null,
                null,
                null
            )
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                    val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    val name = if (nameIndex != -1) it.getString(nameIndex) else ""
                    val phone = if (phoneIndex != -1) it.getString(phoneIndex) else ""
                    return@withContext Pair(name, phone)
                }
            }
            return@withContext null
        }
    }

    @Composable
    fun ConfigureScreen(onSave: (String, String) -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        val contactPickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickContact()
        ) { uri: Uri? ->
            if (uri != null) {
                scope.launch {
                    val details = getContactDetails(context, uri)
                    details?.let { (name, phone) ->
                        onSave(name, phone)
                    }
                }
            }
        }

        // Simple UI showing a button to select a contact
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { contactPickerLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
            ) {
                Text("Select Contact", textAlign = TextAlign.Center)
            }
        }
    }

    @Composable
    fun CallScreen(
        contactName: String,
        contactPhone: String,
        onCall: () -> Unit,
        onReconfigure: () -> Unit
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Configured Contact:",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(4.dp))
            Text(
                text = "Name: $contactName",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Text(
                text = "Phone: $contactPhone",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.padding(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = onCall,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.primary)
                ) {
                    Text("Call", textAlign = TextAlign.Center)
                }
                Button(
                    onClick = onReconfigure,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Gray)
                ) {
                    Text("Reconfigure", textAlign = TextAlign.Center)
                }
            }
        }
    }
}
