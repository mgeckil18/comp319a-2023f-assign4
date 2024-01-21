# MyContacts

## Overview
MyContacts is an Android application that allows users to manage their contact list. Users can view, create, edit, and delete contact information such as names, phone numbers, email addresses, and physical addresses. The app integrates with an external API to fetch and display avatars for each contact.

## Features
- List contacts in a scrollable view.
- Display avatars fetched from Multiavatar API for the first 10 contacts.
- Add new contacts to the list.
- Edit existing contact information.
- Delete contacts from the list.
- Persist contacts data in a CSV file within the application's storage.

## Installation
To set up the MyContacts app for development:
1. Clone the repository to your local machine.
2. Open the project using Android Studio.
3. Ensure that you have the latest version of the Android SDK installed.
4. Sync the project with the provided `build.gradle` file to install all necessary dependencies.
5. Run the application on an emulator or physical device.

## Dependencies
- Android SDK
- Kotlin
- Jetpack Compose for UI components.
- Coil for image loading and caching.
- OkHttp for network operations (if applicable).

## Usage
Upon launching the app, you'll be greeted with a welcome page that provides an option to view the contacts list. The list view displays contact cards with basic information and avatar images for the first 10 contacts.

### Adding a New Contact
Use the '+' button on the app bar to navigate to the new contact page where you can enter the contact details and save them.

### Editing a Contact
Each contact card has an 'edit' button, which allows you to modify the contact's information.

### Deleting a Contact
Next to the edit button on each contact card, there's a 'delete' button to remove the contact from the list.

## Permissions
The app requires the following permissions:
- `INTERNET`: To fetch contact avatars from the Multiavatar API.

## API Rate Limiting
The Multiavatar API is used to fetch avatars and has a rate limit of 10 calls per minute. The app is designed to only load images for the first 10 contacts to comply with this limit.

## Data Persistence
Contact information is stored in a CSV file located in the internal storage of the app. The app reads and writes to this file to persist contact data across sessions.

## Contribution
Contributions to the MyContacts app are welcome. Please feel free to report issues or open a pull request.


## Contact
For any additional questions about the MyContacts app, please reach out to mgeckil18@ku.edu.tr
