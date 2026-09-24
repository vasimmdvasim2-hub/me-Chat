package com.example.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MeChatGreen
import com.example.ui.theme.MeChatTeal

data class CountryOption(val name: String, val code: String)

@Composable
fun PhoneInputScreen(
    initialCountryCode: String = "+91",
    initialCountryName: String = "India",
    onProceedToOtp: (countryCode: String, countryName: String, phone: String) -> Unit
) {
    val countries = listOf(
        CountryOption("India", "+91"),
        CountryOption("United States", "+1"),
        CountryOption("United Kingdom", "+44"),
        CountryOption("Canada", "+1"),
        CountryOption("Australia", "+61"),
        CountryOption("Germany", "+49"),
        CountryOption("France", "+33"),
        CountryOption("United Arab Emirates", "+971"),
        CountryOption("Saudi Arabia", "+966"),
        CountryOption("Pakistan", "+92"),
        CountryOption("Bangladesh", "+880")
    )

    var selectedCountry by remember {
        mutableStateOf(
            countries.firstOrNull { it.code == initialCountryCode } ?: CountryOption(initialCountryName, initialCountryCode)
        )
    }
    var phoneNumber by remember { mutableStateOf("") }
    var isCountryDropdownExpanded by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top Row: Title + 3-dot menu
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Spacer(modifier = Modifier.size(24.dp))
                    Text(
                        text = "Enter your phone number",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MeChatTeal
                    )
                    IconButton(onClick = { /* Help */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Verification notice
                Text(
                    text = "MeChat will need to verify your phone number. Carrier charges may apply.",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "What's my number?",
                    fontSize = 14.sp,
                    color = MeChatTeal,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { }
                )

                Spacer(modifier = Modifier.height(30.dp))

                // Country Selector Dropdown
                Box(modifier = Modifier.fillMaxWidth(0.85f)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isCountryDropdownExpanded = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCountry.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select country",
                                tint = MeChatGreen
                            )
                        }
                        HorizontalDivider(thickness = 1.5.dp, color = MeChatGreen)
                    }

                    DropdownMenu(
                        expanded = isCountryDropdownExpanded,
                        onDismissRequest = { isCountryDropdownExpanded = false }
                    ) {
                        countries.forEach { item ->
                            DropdownMenuItem(
                                text = { Text("${item.name} (${item.code})") },
                                onClick = {
                                    selectedCountry = item
                                    isCountryDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Phone Input Row (Code + Number)
                Row(
                    modifier = Modifier.fillMaxWidth(0.85f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country Code
                    OutlinedTextField(
                        value = selectedCountry.code,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.width(80.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeChatGreen,
                            unfocusedBorderColor = MeChatGreen
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    // Phone Number Field
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { input ->
                            val digitsOnly = input.filter { it.isDigit() }
                            if (digitsOnly.length <= 10) {
                                phoneNumber = digitsOnly
                                errorMessage = ""
                            }
                        },
                        placeholder = { Text("phone number") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("phone_number_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MeChatGreen,
                            unfocusedBorderColor = Color.LightGray
                        )
                    )
                }

                if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Bottom Next Button
            Button(
                onClick = {
                    if (phoneNumber.length in 7..10) {
                        showConfirmDialog = true
                    } else {
                        errorMessage = "Please enter a valid phone number"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(44.dp)
                    .testTag("phone_next_button"),
                shape = RoundedCornerShape(22.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MeChatGreen)
            ) {
                Text(
                    text = "NEXT",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }

        // MeChat Confirmation Dialog
        if (showConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showConfirmDialog = false },
                title = {
                    Text("You entered the phone number:", fontSize = 16.sp)
                },
                text = {
                    Column {
                        Text(
                            text = "${selectedCountry.code} $phoneNumber",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Is this OK, or would you like to edit the number?",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showConfirmDialog = false
                            onProceedToOtp(selectedCountry.code, selectedCountry.name, phoneNumber)
                        },
                        modifier = Modifier.testTag("confirm_phone_yes_button")
                    ) {
                        Text("YES", color = MeChatGreen, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showConfirmDialog = false }) {
                        Text("EDIT", color = MeChatGreen, fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}
