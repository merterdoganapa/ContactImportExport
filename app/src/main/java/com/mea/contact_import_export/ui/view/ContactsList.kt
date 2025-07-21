package com.mea.contact_import_export.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Divider
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.mea.contact_import_export.R
import com.mea.contact_import_export.data.model.Contact
import com.mea.contact_import_export.ui.components.SearchBar
import com.mea.contact_import_export.ui.theme.PrimaryColor
import com.mea.contact_import_export.ui.theme.Typography

@Composable
fun ContactsList(
    contacts: List<Contact>,
    selectedContacts: List<Contact>,
    onContactSelected: (Contact) -> Unit,
    onDeselectAllClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onImportSelectedContactsClick: (() -> Unit)? = null,
    onExportSelectedContactsClick: (() -> Unit)? = null,
    onSearchTextChange: (String) -> Unit,
    searchText: String
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SearchBar(
                    searchText = searchText,
                    onSearchTextChange = onSearchTextChange
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = onSelectAllClick,
                    ) {
                        Text(text = stringResource(id = R.string.select_all))
                    }
                    TextButton(
                        onClick = onDeselectAllClick,
                        enabled = selectedContacts.isNotEmpty()
                    ) {
                        Text(text = stringResource(id = R.string.deselect_all))
                    }
                }

                if (selectedContacts.isNotEmpty()) {
                    if (onImportSelectedContactsClick != null) {
                        TextButton(
                            onClick = onImportSelectedContactsClick
                        ) {
                            Text(text = stringResource(id = R.string.import_selected_contacts) + " ${selectedContacts.size}")
                        }
                    } else if (onExportSelectedContactsClick != null) {
                        TextButton(
                            onClick = onExportSelectedContactsClick
                        ) {
                            Text(text = stringResource(id = R.string.export_selected_contacts) + " ${selectedContacts.size}")
                        }
                    }
                } else {
                    Text(text = stringResource(id = R.string.no_contacts_selected))
                }
            }
        }

        val filteredContacts = contacts.filter { contact ->
            contact.name.contains(searchText, ignoreCase = true) ||
                    contact.phoneNumber?.contains(searchText, ignoreCase = true) == true
        }

        items(filteredContacts.size) { index ->
            ContactCard(
                contact = filteredContacts[index],
                isSelected = selectedContacts.contains(filteredContacts[index])
            ) {
                onContactSelected(filteredContacts[index])
            }
        }
    }
}

@Composable
fun ContactCard(
    contact: Contact,
    isSelected: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp)
            .clickable { onCheckedChange(!isSelected) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(PrimaryColor),
            contentAlignment = Alignment.Center
        ) {
            val initials = getInitials(contact.name)
            Text(
                text = initials,
                color = Color.White,
                style = Typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 16.dp),
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(text = contact.name)
                    Text(text = contact.phoneNumber ?: "-")
                }

                Spacer(modifier = Modifier.width(8.dp))

                Checkbox(
                    checked = isSelected,
                    onCheckedChange = onCheckedChange
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Divider()
        }
    }
}

fun getInitials(name: String): String {
    val words = name.trim().split(" ")
    return if (words.size > 1) {
        "${words[0].first()}${words[1].first()}".uppercase()
    } else {
        words[0].first().uppercase()
    }
}