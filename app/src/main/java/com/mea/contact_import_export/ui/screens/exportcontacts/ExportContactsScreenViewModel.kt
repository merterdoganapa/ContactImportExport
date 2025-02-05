package com.mea.contact_import_export.ui.screens.exportcontacts

import android.app.Activity
import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mea.contact_import_export.data.AdManager
import com.mea.contact_import_export.data.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject


@HiltViewModel
class ExportContactsScreenViewModel @Inject constructor(
    application: Application,
    private val adManager: AdManager
) : ViewModel() {

    private val contentResolver: ContentResolver = application.contentResolver

    private val _uiState = MutableStateFlow(ExportContactsUIState())
    val uiState: StateFlow<ExportContactsUIState> = _uiState.asStateFlow()

    fun loadContacts() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val contactList = fetchContacts()
            _uiState.value = _uiState.value.copy(
                isLoading = false,
                allContacts = contactList,
                selectedContacts = mutableListOf()
            )
        }
    }

    fun updateSelectedContacts(contact: Contact) {
        val tempList = _uiState.value.selectedContacts.toMutableList()
        if (tempList.contains(contact)) {
            tempList.remove(contact)
        } else {
            tempList.add(contact)
        }
        updateSelectedContacts(tempList)
    }

    fun updateSelectedContacts(contacts: MutableList<Contact>) {
        _uiState.update { it.copy(selectedContacts = contacts) }
    }

    private suspend fun fetchContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val contacts = mutableMapOf<String, Contact>()
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
        )

        cursor?.use {
            val idIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

            while (it.moveToNext()) {
                val id = it.getString(idIndex)
                val name = it.getString(nameIndex)
                var phoneNumber = it.getString(phoneIndex)
                var address = ""
                var email = ""

                phoneNumber = phoneNumber?.replace(Regex("[^\\d+]"), "") ?: ""

                val emailCur = contentResolver.query(
                    ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )

                emailCur?.apply {
                    while (this.moveToNext()) {
                        val emailIndex =
                            this.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA)
                        email = this.getString(emailIndex)
                    }
                }

                emailCur?.close()

                val addressCur = contentResolver.query(
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_URI,
                    null,
                    ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID + " = ?",
                    arrayOf(id), null
                )

                addressCur?.apply {
                    while (this.moveToNext()) {
                        val addressIndex =
                            this.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
                        address = this.getString(addressIndex)
                    }
                }

                addressCur?.close()

                // Check if the contact with this phone number already exists
                val existingContact = contacts[phoneNumber]

                if (existingContact != null) {
                    // If the contact exists, update the fields (only if they are non-empty)
                    val updatedAddress =
                        if (address.isNotEmpty()) address else existingContact.address
                    val updatedEmail = if (email.isNotEmpty()) email else existingContact.email

                    // Update the contact in the map
                    contacts[phoneNumber] =
                        existingContact.copy(address = updatedAddress, email = updatedEmail)
                } else {
                    // If the contact doesn't exist, add it to the map
                    contacts[phoneNumber] = Contact(id, name, phoneNumber, address, email)
                }
            }
        }
        return@withContext contacts.values.toList()
    }

    fun syncContacts(
        askPermission: () -> Unit
    ) {
        if (_uiState.value.hasPermission) {
            loadContacts()
        } else {
            askPermission()
        }
    }

    fun exportContacts(context: Context) {
        val vCardData = StringBuilder()
        for (contact in _uiState.value.selectedContacts) {
            vCardData.append(createVCard(contact)).append("\n")
        }
        try {
            val file = File(context.getExternalFilesDir(null), "contacts_export.vcf")
            val fos = FileOutputStream(file)
            fos.write(vCardData.toString().toByteArray())
            fos.close()
            shareVCardFile(context, file)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun createVCard(contact: Contact): String {
        return """
        BEGIN:VCARD
        VERSION:3.0
        FN:${contact.name}
        TEL:${contact.phoneNumber ?: ""}
        EMAIL:${contact.email ?: ""}
        ADR:${contact.address ?: ""}
        END:VCARD
    """.trimIndent()
    }

    private fun shareVCardFile(context: Context, file: File) {
        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/x-vcard"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, "Share Contacts via"))
    }

    fun searchContacts(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun showAdAndExportContacts(activity: Activity, context: Activity) {
        adManager.showRewardedAd(
            activity,
            onRewarded = {
                exportContacts(context)
            },
            onAdClosed = {
            },
            onAdFailedToShow = {
                exportContacts(context)
            }
        )
    }

}

data class ExportContactsUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    var allContacts: List<Contact> = emptyList(),
    var selectedContacts: MutableList<Contact> = mutableListOf(),
    var hasPermission: Boolean = false,
    val searchText: String = ""
)