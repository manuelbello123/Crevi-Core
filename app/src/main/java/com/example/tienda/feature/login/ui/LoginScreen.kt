package com.example.tienda.feature.login.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tienda.core.ui.components.ShowUiMessage
import com.example.tienda.core.ui.components.TiendaSnackbarHost
import com.example.tienda.core.ui.theme.Background
import com.example.tienda.core.ui.theme.Border
import com.example.tienda.core.ui.theme.OnPrimary
import com.example.tienda.core.ui.theme.Primary
import com.example.tienda.core.ui.theme.Surface
import com.example.tienda.core.ui.theme.SurfaceMuted
import com.example.tienda.core.ui.theme.TextDisabled
import com.example.tienda.core.ui.theme.TextPrimary
import com.example.tienda.core.ui.theme.TextSecondary
import com.example.tienda.core.ui.theme.TiendaShapes
import com.example.tienda.core.ui.theme.TiendaTheme

/**
 * Pantalla de login (stateless: estado + callbacks, previsualizable y sin
 * dependencia de Activity). El BiometricPrompt lo dispara el caller vía
 * [onBiometricLogin]; aquí solo se muestra el botón cuando aplica.
 */
@Composable
fun LoginScreen(
    state: LoginUiState,
    biometricAvailable: Boolean,
    onUsuarioChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLogin: () -> Unit,
    onBiometricLogin: () -> Unit,
    onErrorShown: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    ShowUiMessage(message = state.errorMessage, hostState = snackbarHostState, onShown = onErrorShown)

    val showBiometric = biometricAvailable && state.canOfferBiometric

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Background,
        snackbarHost = { TiendaSnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(96.dp))

            BrandMark()

            Spacer(Modifier.height(28.dp))
            Text(
                text = "Tienda",
                style = MaterialTheme.typography.displaySmall,
                color = TextPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Gestión de ventas y cobranza",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
            )

            Spacer(Modifier.height(44.dp))

            // ── Usuario ──
            LoginField(
                value = state.usuario,
                onValueChange = onUsuarioChange,
                label = "Usuario",
                leading = Icons.Filled.Person,
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            )

            Spacer(Modifier.height(16.dp))

            // ── Contraseña ──
            var passwordVisible by remember { mutableStateOf(false) }
            LoginField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = "Contraseña",
                leading = Icons.Filled.Lock,
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                onImeAction = onLogin,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailing = {
                    val icon = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    val desc = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña"
                    Icon(
                        imageVector = icon,
                        contentDescription = desc,
                        tint = TextSecondary,
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { passwordVisible = !passwordVisible }
                            .padding(4.dp),
                    )
                },
            )

            Spacer(Modifier.height(28.dp))

            // ── CTA primario ──
            PrimaryButton(
                text = "Entrar",
                enabled = state.canSubmit,
                loading = state.isLoading,
                onClick = onLogin,
            )

            if (showBiometric) {
                Spacer(Modifier.height(24.dp))
                DividerWithText("o")
                Spacer(Modifier.height(24.dp))
                BiometricButton(
                    enabled = !state.isLoading,
                    onClick = onBiometricLogin,
                )
            }

            Spacer(Modifier.height(48.dp))
        }
    }
}

@Composable
private fun BrandMark() {
    Box(
        modifier = Modifier
            .size(78.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Primary),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "T",
            color = OnPrimary,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun LoginField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leading: androidx.compose.ui.graphics.vector.ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    modifier: Modifier = Modifier,
    onImeAction: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: @Composable (() -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        label = { Text(label) },
        singleLine = true,
        leadingIcon = { Icon(leading, contentDescription = null) },
        trailingIcon = trailing,
        visualTransformation = visualTransformation,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { onImeAction() }, onGo = { onImeAction() }),
        shape = TiendaShapes.Card,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Primary,
            unfocusedBorderColor = Border,
            focusedLeadingIconColor = Primary,
            unfocusedLeadingIconColor = TextSecondary,
            focusedLabelColor = Primary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = Primary,
            focusedContainerColor = Surface,
            unfocusedContainerColor = Surface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
        ),
    )
}

@Composable
private fun PrimaryButton(
    text: String,
    enabled: Boolean,
    loading: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(TiendaShapes.Card)
            .background(if (enabled) Primary else SurfaceMuted)
            .clickable(enabled = enabled && !loading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(22.dp),
                color = OnPrimary,
                strokeWidth = 2.dp,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                color = if (enabled) OnPrimary else TextDisabled,
            )
        }
    }
}

@Composable
private fun BiometricButton(
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(TiendaShapes.Card)
            .border(1.dp, Border, TiendaShapes.Card)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Fingerprint,
                contentDescription = null,
                tint = Primary,
            )
            Spacer(Modifier.size(10.dp))
            Text(
                text = "Entrar con huella",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimary,
            )
        }
    }
}

@Composable
private fun DividerWithText(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        HorizontalDivider(modifier = Modifier.weight(1f), color = Border)
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF6F8FB, heightDp = 880)
@Composable
private fun LoginScreenPreview() {
    TiendaTheme {
        LoginScreen(
            state = LoginUiState(usuario = "vico", password = "secret", canOfferBiometric = true),
            biometricAvailable = true,
            onUsuarioChange = {},
            onPasswordChange = {},
            onLogin = {},
            onBiometricLogin = {},
            onErrorShown = {},
        )
    }
}
