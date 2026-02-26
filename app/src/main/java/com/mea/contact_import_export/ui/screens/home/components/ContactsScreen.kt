package com.mea.contact_import_export.ui.screens.home.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mea.contact_import_export.R
import com.mea.contact_import_export.data.model.Contact
import com.mea.contact_import_export.ui.components.PrimaryButton
import com.mea.contact_import_export.ui.theme.PrimaryColor
import kotlinx.coroutines.delay

@Composable
fun ContactsScreen(
    modifier: Modifier = Modifier,
    contacts: List<Contact>,
    selectedContacts: List<Contact>,
    searchText: String,
    onSearchTextChange: (String) -> Unit,
    onSync: () -> Unit,
    onContactClick: (Contact) -> Unit,
    onSelectContacts: (List<Contact>) -> Unit,
    onImportContacts: () -> Unit,
    onExportSelected: () -> Unit,
    onDeleteSelected: () -> Unit,
    onMergeSelected: () -> Unit
) {
    val duplicateContacts = remember(contacts) { findDuplicateContacts(contacts) }
    val duplicateCount = duplicateContacts.size
    var showOnlyDuplicates by rememberSaveable { mutableStateOf(false) }

    val sourceContacts = if (showOnlyDuplicates) duplicateContacts else contacts
    val allSourceSelected = sourceContacts.isNotEmpty() && sourceContacts.all { contact ->
        selectedContacts.contains(contact)
    }
    val filteredContacts = remember(sourceContacts, searchText) {
        sourceContacts.filter { contact ->
            contact.name.contains(searchText, ignoreCase = true) ||
                    contact.phoneNumber.orEmpty().contains(searchText, ignoreCase = true) ||
                    contact.email.orEmpty().contains(searchText, ignoreCase = true)
        }
    }

    val groupedContacts = remember(filteredContacts) {
        filteredContacts.groupBy { contact ->
            contact.name.firstOrNull()?.uppercaseChar()?.toString() ?: "#"
        }.toSortedMap()
    }
    val isEmptyState = contacts.isEmpty() && searchText.isBlank() && !showOnlyDuplicates

    Box(modifier = modifier.fillMaxSize()) {
        val selectedCountLabel =
            if (selectedContacts.size > 999) "999+" else selectedContacts.size.toString()

        if (isEmptyState) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                ContactsEmptyState(
                    onImportContacts = onImportContacts,
                    onGoToExport = onExportSelected
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 10.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (!isEmptyState) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(id = R.string.contacts_tab),
                                    style = TextStyle(fontSize = 34.sp, lineHeight = 40.sp),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${contacts.size} ${stringResource(id = R.string.total_label)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = PrimaryColor
                                )
                            }
//                            CircleActionButton(
//                                onClick = onSync,
//                                icon = Icons.Default.Refresh,
//                                contentDescription = stringResource(id = R.string.sync_contacts)
//                            )
                        }
                    }
                }

                if (!isEmptyState) {
                    item {
                        OutlinedTextField(
                            value = searchText,
                            onValueChange = onSearchTextChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            placeholder = { Text(stringResource(id = R.string.contacts_search_placeholder)) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = stringResource(id = R.string.search)
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White
                            )
                        )
                    }
                }

                if (!isEmptyState) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            if (allSourceSelected) {
                                TextButton(
                                    onClick = { onSelectContacts(emptyList()) }
                                ) {
                                    Text(text = stringResource(id = R.string.deselect_all))
                                }
                            } else {
                                TextButton(
                                    onClick = { onSelectContacts(sourceContacts) },
                                    enabled = sourceContacts.isNotEmpty()
                                ) {
                                    Text(text = stringResource(id = R.string.select_all))
                                }
                            }
                        }
                    }
                }

                if (duplicateCount > 0) {
                    item {
                        DuplicatesCard(
                            duplicateCount = duplicateCount,
                            showOnlyDuplicates = showOnlyDuplicates,
                            onCleanUpClick = {
                                showOnlyDuplicates = !showOnlyDuplicates
                                onSelectContacts(duplicateContacts)
                            }
                        )
                    }
                }

                if (groupedContacts.isEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (searchText.isNotBlank()) {
                                    stringResource(id = R.string.no_search_results)
                                } else if (showOnlyDuplicates) {
                                    stringResource(id = R.string.no_duplicates_found)
                                } else {
                                    stringResource(id = R.string.no_contacts_to_export)
                                },
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    groupedContacts.forEach { (initial, people) ->
                        item {
                            Text(
                                text = initial,
                                style = MaterialTheme.typography.labelLarge,
                                color = PrimaryColor,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF3F5F8))
                                    .padding(vertical = 4.dp)
                            )
                        }
                        items(people, key = { "${it.id}-${it.phoneNumber}" }) { contact ->
                            ContactListRow(
                                contact = contact,
                                selected = selectedContacts.contains(contact),
                                onClick = { onContactClick(contact) }
                            )
                        }
                    }
                }
            }
            if (selectedContacts.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .navigationBarsPadding(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1724))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .defaultMinSize(minWidth = 18.dp, minHeight = 18.dp)
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(PrimaryColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = selectedCountLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(id = R.string.selected_label),
                                color = Color.White,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                            BottomActionIcon(
                                icon = Icons.Default.Share,
                                text = stringResource(id = R.string.export_title),
                                tint = Color.White,
                                onClick = onExportSelected
                            )
                            BottomActionIcon(
                                icon = Icons.Default.Delete,
                                text = stringResource(id = R.string.delete_label),
                                tint = Color(0xFFFB7185),
                                onClick = onDeleteSelected
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactsEmptyState(
    onImportContacts: () -> Unit,
    onGoToExport: () -> Unit
) {
    val carouselItems = listOf(
        EmptyActionItem(
            iconRes = R.drawable.ic_empty_import,
            title = R.string.contacts_empty_import_title,
            description = R.string.contacts_empty_import_description,
            primary = true,
            buttonText = R.string.import_contacts,
            onClick = onImportContacts
        ),
        EmptyActionItem(
            iconRes = R.drawable.ic_empty_export,
            title = R.string.contacts_empty_export_title,
            description = R.string.contacts_empty_export_description,
            primary = false,
            buttonText = R.string.go_to_export,
            onClick = onGoToExport
        )
    )
    val carouselState = rememberLazyListState()
    val currentPage = carouselState.firstVisibleItemIndex.coerceIn(0, carouselItems.lastIndex)
    var autoScrollToken by remember { mutableIntStateOf(0) }
    var isAutoScrolling by remember { mutableStateOf(false) }

    LaunchedEffect(carouselItems.size) {
        if (carouselItems.size <= 1) return@LaunchedEffect
        var wasScrolling = false
        snapshotFlow { carouselState.isScrollInProgress }.collect { scrolling ->
            if (scrolling) {
                wasScrolling = true
            } else if (wasScrolling && !isAutoScrolling) {
                wasScrolling = false
                autoScrollToken++
            }
        }
    }
    LaunchedEffect(autoScrollToken, carouselItems.size) {
        if (carouselItems.size <= 1) return@LaunchedEffect
        delay(3500)
        if (carouselState.isScrollInProgress) return@LaunchedEffect
        val next = (carouselState.firstVisibleItemIndex + 1) % carouselItems.size
        isAutoScrolling = true
        try {
            carouselState.animateScrollToItem(next)
        } finally {
            isAutoScrolling = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(192.dp, 203.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_empty_contacts_state),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.fillMaxSize()
            )
        }

        Text(
            text = stringResource(id = R.string.contacts_empty_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF111827)
        )
        Text(
            text = stringResource(id = R.string.contacts_empty_description),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFF6B7280),
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            state = carouselState,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(0.dp),
            flingBehavior = rememberSnapFlingBehavior(lazyListState = carouselState)
        ) {
            items(carouselItems.size) { index ->
                val item = carouselItems[index]
                EmptyActionCard(
                    modifier = Modifier
                        .fillParentMaxWidth(1f)
                        .padding(horizontal = 6.dp),
                    iconRes = item.iconRes,
                    title = stringResource(id = item.title),
                    description = stringResource(id = item.description),
                    primary = item.primary,
                    buttonText = stringResource(id = item.buttonText),
                    onClick = item.onClick
                )
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(carouselItems.size) { index ->
                val isSelected = index == currentPage
                Box(
                    modifier = Modifier
                        .height(6.dp)
                        .width(if (isSelected) 18.dp else 6.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(if (isSelected) PrimaryColor else Color(0xFFD2D9E3))
                )
            }
        }
    }
}

private data class EmptyActionItem(
    val iconRes: Int,
    val title: Int,
    val description: Int,
    val primary: Boolean,
    val buttonText: Int,
    val onClick: () -> Unit
)

@Composable
private fun EmptyActionCard(
    modifier: Modifier = Modifier,
    iconRes: Int,
    title: String,
    description: String,
    primary: Boolean,
    buttonText: String,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE7F0FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF667085)
            )
            if (primary) {
                PrimaryButton(
                    text = buttonText,
                    onClick = onClick
                )
            } else {
                OutlinedButton(
                    onClick = onClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PrimaryColor
                    ),
                    border = BorderStroke(1.5.dp, Color(0xFFAED3FF))
                ) {
                    Text(text = buttonText, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun CircleActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String
) {
    Surface(
        modifier = Modifier.size(36.dp),
        shape = CircleShape,
        color = Color.White,
        shadowElevation = 3.dp
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = PrimaryColor
            )
        }
    }
}

@Composable
private fun DuplicatesCard(
    duplicateCount: Int,
    showOnlyDuplicates: Boolean,
    onCleanUpClick: () -> Unit
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE8F1FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = PrimaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(id = R.string.duplicates_found_count, duplicateCount),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = stringResource(id = R.string.duplicate_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF75808F),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Button(
                    onClick = onCleanUpClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2F80ED),
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = if (showOnlyDuplicates) {
                            stringResource(id = R.string.show_all_label)
                        } else {
                            stringResource(id = R.string.cleanup_label)
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
        Text(
            text = "PRO",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .clip(RoundedCornerShape(bottomStart = 8.dp))
                .background(PrimaryColor)
                .padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ContactListRow(
    contact: Contact,
    selected: Boolean,
    onClick: () -> Unit
) {
    val subtitle = when {
        !contact.phoneNumber.isNullOrBlank() -> "Mobile: ${contact.phoneNumber}"
        !contact.email.isNullOrBlank() -> contact.email
        !contact.address.isNullOrBlank() -> contact.address
        else -> stringResource(id = R.string.no_info_available)
    }
    val showWorkTag = contact.email.isNullOrBlank() && !contact.address.isNullOrBlank()
    val backgroundColor = if (selected) Color(0xFFEDF4FF) else Color.White
    val borderColor = if (selected) Color(0xFFB8D7FF) else Color.Transparent

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = if (selected) BorderStroke(1.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = 74.dp)
                .padding(horizontal = 12.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .border(1.dp, Color(0xFFC8D2E0), CircleShape)
                    .background(if (selected) PrimaryColor else Color.Transparent),
                contentAlignment = Alignment.Center
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(contactAvatarColor(contact.name)),
                contentAlignment = Alignment.Center
            ) {
                if (contact.name.isBlank()) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color(0xFF98A2B3),
                        modifier = Modifier.size(18.dp)
                    )
                } else {
                    Text(
                        text = getInitials(contact.name),
                        color = Color(0xFF2D3748),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = contact.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF7A8593)
                )
            }
            if (showWorkTag) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "WORK",
                        color = Color(0xFF667085),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            } else {
                Text(
                    text = "\u203A",
                    color = Color(0xFFB0BAC8),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}

@Composable
private fun BottomActionIcon(
    icon: ImageVector,
    text: String,
    tint: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = text.uppercase(),
            color = tint,
            style = MaterialTheme.typography.labelSmall
        )
    }
}

private fun contactAvatarColor(name: String): Color {
    val palette = listOf(
        Color(0xFFFCE4B2),
        Color(0xFFD1F2DE),
        Color(0xFFF8D6DD),
        Color(0xFFE4E7EC),
        Color(0xFFD9EDFF)
    )
    return palette[kotlin.math.abs(name.hashCode()) % palette.size]
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ").filter { it.isNotBlank() }
    return when {
        parts.isEmpty() -> "?"
        parts.size == 1 -> parts.first().take(1).uppercase()
        else -> "${parts[0].take(1)}${parts[1].take(1)}".uppercase()
    }
}

private fun findDuplicateContacts(contacts: List<Contact>): List<Contact> {
    if (contacts.isEmpty()) return emptyList()

    val grouped = contacts.groupBy { contact ->
        val normalizedPhone = normalizePhone(contact.phoneNumber)
        when {
            normalizedPhone != null -> "phone:$normalizedPhone"
            !contact.email.isNullOrBlank() -> "email:${contact.email.trim().lowercase()}"
            else -> "name:${contact.name.trim().lowercase()}"
        }
    }

    return grouped.values.filter { it.size > 1 }.flatten()
}

private fun normalizePhone(phone: String?): String? {
    if (phone.isNullOrBlank()) return null
    val normalized = phone.filter { it.isDigit() }
    return normalized.takeIf { it.length >= 7 }
}
