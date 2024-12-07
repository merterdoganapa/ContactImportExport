package com.mea.contact_import_export.data.model

data class Contact(
    val id: String,
    val name: String,
    val phoneNumber: String?,
    val address: String?,
    val email: String?
)