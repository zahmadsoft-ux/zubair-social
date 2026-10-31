package com.zubair.social.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.zubair.social.ui.theme.ZubairSocialTheme

/** App ke 5 main tabs. Route mapping navigation layer mein hai, component sirf label + icon jaanta hai. */
enum class NavTab(val label: String, val icon: ImageVector) {
    Home("Home", Icons.Filled.Home),
    Search("Search", Icons.Filled.Search),
    Create("Create", Icons.Filled.Add),
    Activity("Activity", Icons.Filled.Favorite),
    Profile("Profile", Icons.Filled.Person),
}

/** Stateless: kaunsa tab selected hai woh bahar se aata hai, tap par onSelect call hota hai. */
@Composable
fun BottomNavBar(
    selected: NavTab,
    onSelect: (NavTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    NavigationBar(modifier = modifier, containerColor = MaterialTheme.colorScheme.surface) {
        NavTab.entries.forEach { tab ->
            NavigationBarItem(
                selected = tab == selected,
                onClick = { onSelect(tab) },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label, style = MaterialTheme.typography.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarHomePreview() {
    ZubairSocialTheme { BottomNavBar(selected = NavTab.Home, onSelect = {}) }
}

@Preview(showBackground = true)
@Composable
private fun BottomNavBarProfilePreview() {
    ZubairSocialTheme { BottomNavBar(selected = NavTab.Profile, onSelect = {}) }
}
