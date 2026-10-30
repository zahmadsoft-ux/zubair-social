package com.zubair.social.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.Radius
import com.zubair.social.ui.theme.Spacing
import com.zubair.social.ui.theme.ZubairSocialTheme

/**
 * Login/Sign up forms ka input. Stateless: value aur onValueChange bahar se aate hain.
 * isPassword = true ho to text chhupa rehta hai aur "Show/Hide" toggle milta hai.
 * errorMessage null nahi ho to field laal ho jata hai aur message neeche dikhta hai.
 */
@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    helperText: String? = null,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    // Show/Hide ka state sirf is component ka apna hai, bahar wale ko iski zarurat nahi.
    var passwordVisible by remember { mutableStateOf(false) }
    val hidden = isPassword && !passwordVisible

    Column(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            isError = errorMessage != null,
            shape = RoundedCornerShape(Radius.md),
            visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (isPassword) KeyboardType.Password else keyboardType
            ),
            trailingIcon = if (isPassword) {
                {
                    TextButton(onClick = { passwordVisible = !passwordVisible }) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                }
            } else null,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                errorContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
        )
        val footnote = errorMessage ?: helperText
        if (footnote != null) {
            Text(
                text = footnote,
                color = if (errorMessage != null) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = Spacing.md, top = Spacing.xs),
            )
        }
    }
}

private fun Modifier.fillMaxWidth(): Modifier =
    this.then(Modifier.fillMaxWidth())

@Preview(showBackground = true)
@Composable
private fun AppTextFieldPreview() {
    ZubairSocialTheme {
        Column(modifier = Modifier.padding(Spacing.md)) {
            AppTextField(label = "Email", value = "zubair@example.com", onValueChange = {})
            AppTextField(label = "Password", value = "secret123", onValueChange = {}, isPassword = true)
            AppTextField(label = "Username", value = "a b", onValueChange = {}, errorMessage = "Only letters, numbers, and underscores are allowed")
        }
    }
}
