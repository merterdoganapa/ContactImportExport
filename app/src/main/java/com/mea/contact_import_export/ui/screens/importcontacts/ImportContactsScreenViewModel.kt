package com.mea.contact_import_export.ui.screens.importcontacts

import android.app.Activity
import android.app.Application
import android.content.ContentProviderOperation
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.result.ActivityResult
import androidx.lifecycle.ViewModel
import com.mea.contact_import_export.data.AdManager
import com.mea.contact_import_export.data.model.Contact
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.BufferedReader
import java.io.InputStreamReader
import javax.inject.Inject

data class ImportContactsUIState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    var allContacts: List<Contact> = emptyList(),
    var selectedContacts: MutableList<Contact> = mutableListOf(),
    var hasPermission: Boolean = false,
    val searchText: String = ""
)

@HiltViewModel
class ImportContactsScreenViewModel @Inject constructor(
    application: Application,
    private val adManager: AdManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ImportContactsUIState())
    val uiState: StateFlow<ImportContactsUIState> = _uiState.asStateFlow()
    private var launcher: ManagedActivityResultLauncher<Intent, ActivityResult>? = null

    fun setLauncher(launcher: ManagedActivityResultLauncher<Intent, ActivityResult>) {
        launcher.let {
            this.launcher = it
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

    fun selectVcfFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "text/x-vcard"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        launcher?.launch(intent)
    }

    fun processVcfFile(context: Context, uri: Uri) {
        val contacts = mutableListOf<Contact>()
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val reader = BufferedReader(InputStreamReader(inputStream))
            var line: String?

            var name: String? = null
            var phoneNumber: String? = null
            var email: String? = null
            var adr: String? = null

            while (reader.readLine().also { line = it } != null) {
                line?.let {
                    if (it.startsWith("FN:")) {
                        name = it.substringAfter("FN:")
                    } else if (it.startsWith("TEL:")) {
                        phoneNumber = it.substringAfter("TEL:")
                    } else if (it.startsWith("EMAIL:")) {
                        email = it.substringAfter("EMAIL:")
                    } else if (it.startsWith("ADR:")) {
                        adr = it.substringAfter("ADR:")
                    }

                    if (name != null && phoneNumber != null && email != null && adr != null) {
                        contacts.add(Contact("", name!!, phoneNumber!!, adr, email))
                        name = null
                        phoneNumber = null
                        email = null
                        adr = null
                    }
                }
            }

            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        _uiState.value = _uiState.value.copy(allContacts = contacts)
        println(contacts)
    }

    fun addContactsToPhone(
        context: Context, contacts: List<Contact>,
        addContactsSuccess: String,
        addContactsFailed: String
    ) {
        try {
            contacts.forEach { contact ->
                val operations = ArrayList<ContentProviderOperation>()
                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                        .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME,
                            contact.name
                        )
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            contact.phoneNumber
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Phone.TYPE,
                            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                        )
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
                            contact.address
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
                            ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME
                        )
                        .build()
                )

                operations.add(
                    ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                        .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                        .withValue(
                            ContactsContract.Data.MIMETYPE,
                            ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Email.ADDRESS,
                            contact.email
                        )
                        .withValue(
                            ContactsContract.CommonDataKinds.Email.TYPE,
                            ContactsContract.CommonDataKinds.Email.TYPE_WORK
                        )
                        .build()
                )

                context.contentResolver.applyBatch(ContactsContract.AUTHORITY, operations)
                Toast.makeText(context, addContactsSuccess, Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, addContactsFailed, Toast.LENGTH_LONG).show()
        }
    }

    fun searchContacts(searchText: String) {
        _uiState.update { it.copy(searchText = searchText) }
    }

    fun showAdAndProcessVcfFile(activity: Activity, context: Activity, uri: Uri) {
        adManager.showRewardedAd(
            activity,
            onRewarded = {
                processVcfFile(context, uri)
            },
            onAdClosed = {
            },
            onAdFailedToShow = {
                processVcfFile(context, uri)
            }
        )
    }

}

