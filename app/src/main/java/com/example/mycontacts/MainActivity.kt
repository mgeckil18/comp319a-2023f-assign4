@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.mycontacts

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import coil.compose.rememberImagePainter
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.mycontacts.ui.theme.MyContactsTheme
import java.io.File
import java.net.URLEncoder

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        copyCsvToInternalStorageIfNeeded(this)
        setContent {
            MyContactsTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val navController = rememberNavController()
    Scaffold(
        topBar = { AppBar(navController = navController) },
        content = { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                NavigationHost(navController = navController)
            }
        }
    )
}

@Composable
fun NavigationHost(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomePage(navController) }
        composable("edit/{contactId}") { backStackEntry ->
            EditContactPage(navController, backStackEntry.arguments?.getString("contactId")?.toInt() ?: -1)
        }
        composable("new") { NewContactPage(navController) }
        composable("list_view") { ListViewPage(navController) }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(navController: NavController, title: String = "My Contacts") {
    TopAppBar(
        title = { Text(title) },
        actions = {
            IconButton(onClick = { navController.navigate("new") }) {
                Icon(Icons.Filled.Add, contentDescription = "Add Contact")
            }
        }
    )
}





@Composable
fun HomePage(navController: NavController) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Welcome to My Contacts!",
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = { navController.navigate("list_view") },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("View Contacts")
            }
        }
    }
}


@Composable
fun ListViewPage(navController: NavController, context: Context = LocalContext.current) {
    var contacts = remember { mutableStateListOf<Contact>().apply { addAll(parseCsvData(context)) } }

    val imageLoadLimit = 10

    LazyColumn {
        itemsIndexed(contacts, key = { _, contact -> contact.id }) { index, contact ->
            ContactCard(
                contact = contact,
                navController = navController,
                loadImage = index < imageLoadLimit, // Pass a flag indicating whether to load the image
                onEdit = { id ->
                    navController.navigate("edit/$id")
                },
                onDelete = { id ->
                    deleteContact(context, id)
                    contacts.removeIf { it.id == id }
                }
            )
        }
    }
}

fun deleteContact(context: Context, contactId: Int) {
    val contacts = parseCsvData(context).toMutableList()
    val updatedContacts = contacts.filter { it.id != contactId }
    writeContactsToCsv(context, updatedContacts)
}

fun writeContactsToCsv(context: Context, contacts: List<Contact>) {
    context.openFileOutput("contacts.csv", Context.MODE_PRIVATE).use { outputStream ->
        val writer = outputStream.bufferedWriter()
        writer.use { out ->
            out.write("id,name,surname,phone_number,email,address\n")
            contacts.forEach { contact ->
                out.write("${contact.id},${contact.name},${contact.surname},${contact.phoneNumber},${contact.email},${contact.address}\n")
            }
        }
    }
}




fun saveContact(context: Context, updatedContact: Contact) {
    val contacts = parseCsvData(context).toMutableList()
    val index = contacts.indexOfFirst { it.id == updatedContact.id }
    if (index != -1) {
        contacts[index] = updatedContact
    } else {
        contacts.add(updatedContact) // In case the contact doesn't exist, add it.
    }
    writeContactsToCsv(context, contacts)
}

fun parseCsvData(context: Context): List<Contact> {
    val file = File(context.filesDir, "contacts.csv")
    return file.readLines().mapNotNull { line ->
        val parts = line.split(",")
        if (parts.size >= 6) {
            Contact(
                id = parts[0].toIntOrNull() ?: return@mapNotNull null,
                name = parts[1].trim(),
                surname = parts[2].trim(),
                phoneNumber = parts[3].trim(),
                email = parts.getOrNull(4)?.trim()?.ifEmpty { null },
                address = parts.getOrNull(5)?.trim()?.ifEmpty { null }
            )
        } else {
            null // Skip lines that don't have enough parts
        }
    }
}




data class Contact(
    val id: Int,
    val name: String,
    val surname: String,
    val phoneNumber: String,
    val email: String?,
    val address: String?
)

fun copyCsvToInternalStorageIfNeeded(context: Context) {
    val file = File(context.filesDir, "contacts.csv")
    if (!file.exists()) {
        context.resources.openRawResource(R.raw.contacts).use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}


@Composable
fun NewContactPage(
    navController: NavController,
    context: Context = LocalContext.current
) {
    var name by remember { mutableStateOf("") }
    var surname by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        TextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Surname") }
        )
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") }
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val newContactId = generateNewContactId(context)
            val newContact = Contact(newContactId, name, surname, phoneNumber, email, address)
            saveNewContact(context, newContact)
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}

fun saveNewContact(context: Context, newContact: Contact) {
    val contacts = parseCsvData(context).toMutableList()
    contacts.add(newContact)
    writeContactsToCsv(context, contacts)
}

fun generateNewContactId(context: Context): Int {
    val contacts = parseCsvData(context)
    return (contacts.maxOfOrNull { it.id } ?: 0) + 1
}


@Composable
fun EditContactPage(
    navController: NavController,
    contactId: Int,
    context: Context = LocalContext.current
) {
    val contact = remember { findContactById(context, contactId) }

    var name by remember { mutableStateOf(contact?.name ?: "") }
    var surname by remember { mutableStateOf(contact?.surname ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var email by remember { mutableStateOf(contact?.email ?: "") }
    var address by remember { mutableStateOf(contact?.address ?: "") }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") }
        )
        TextField(
            value = surname,
            onValueChange = { surname = it },
            label = { Text("Surname") }
        )
        TextField(
            value = phoneNumber,
            onValueChange = { phoneNumber = it },
            label = { Text("Phone Number") }
        )
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") }
        )
        TextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            val updatedContact = Contact(contactId, name, surname, phoneNumber, email, address)
            saveContact(context, updatedContact)
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}

fun findContactById(context: Context, contactId: Int): Contact? {
    val contacts = parseCsvData(context)
    return contacts.find { it.id == contactId }
}



@Composable
fun ContactCard(
    contact: Contact,
    onEdit: (Int) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier,
    loadImage: Boolean,
    navController: NavController,
    context: Context = LocalContext.current
) {

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            if (loadImage) {
                // Only load the image if loadImage is true
                Image(
                    painter = rememberAsyncImagePainter(
                        ImageRequest.Builder(LocalContext.current)
                            .data(data = "https://api.multiavatar.com/${URLEncoder.encode("${contact.name} ${contact.surname}", "UTF-8")}.png")
                            .apply(block = fun ImageRequest.Builder.() {
                                crossfade(true)
                            }).build()
                    ),
                    contentDescription = "Movie Image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp) // Set your desired height
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(text = "${contact.name} ${contact.surname}", style = MaterialTheme.typography.titleLarge)
                    Text(text = contact.phoneNumber, style = MaterialTheme.typography.bodyMedium)
                    contact.email?.let { email ->
                        Text(text = email, style = MaterialTheme.typography.bodySmall)
                    }
                    contact.address?.let { address ->
                        Text(text = address, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = { navController.navigate("edit/${contact.id}") }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Edit")
                    }
                    IconButton(onClick = { /* TODO: Implement delete functionality */ }) {
                        Icon(Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
            // Placeholder for profile image
            // Image(...)
        }
    }
}

fun buildAvatarUrl(name: String, surname: String): String {
    val fullName = "$name $surname"
    // Assume that fullName needs to be URL encoded to be used in the API call
    val encodedName = URLEncoder.encode(fullName, "UTF-8")
    return "https://api.multiavatar.com/$encodedName"
}


