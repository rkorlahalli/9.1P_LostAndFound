# 9.1P Lost and Found Map App

## Overview

This project is an Android Studio application using **Java**, **XML**, and **SQLite**. The app allows users to create and manage lost and found adverts through a mobile interface with local data persistence and geo-location features.

Users can:

- Create a new lost or found advert
- View all saved lost and found items
- Search and filter items by category
- Upload an image for each advert
- Save a location for each advert
- Use the current device location
- View lost and found items on a map
- Search for items within a selected radius
- Remove adverts once the item has been resolved

The application stores advert data locally on the device using SQLite, so the data remains even after the app is closed or the device is restarted.

---

## Features

- Create a new advert with:
  - Lost or Found post type
  - Item name
  - Phone number
  - Description
  - Date
  - Location
  - Category
  - Image upload
  - Timestamp
  - Latitude and longitude
- View all saved lost and found adverts in a list
- Search adverts by item details
- Filter adverts by category
- View full details of a selected advert
- Remove an advert after the item has been found or returned
- Store advert data locally using SQLite
- Display an image preview
- Add a date/time timestamp to each post
- Input validation
- Enter a location using the location text field
- Get the user’s current location using the **Get Current Location** button
- Show all saved lost and found items on a map
- Display map markers for saved items
- Search for nearby items using a radius in kilometres

---

## Advert Details Stored

Each lost or found advert includes the following fields:

- Post type
- Item name
- Phone number
- Description
- Date
- Location
- Category
- Image URI
- Timestamp
- Latitude
- Longitude

### Example Categories

- Electronics
- Pets
- Wallets
- Keys
- Bags
- Other

---

## Project Structure

- `MainActivity.java` — displays the home screen and navigates to the main app features
- `activity_main.xml` — defines the main home screen layout
- `CreateAdvertActivity.java` — handles creating a new lost or found advert
- `activity_create_advert.xml` — layout for the create advert screen
- `ItemListActivity.java` — displays all saved lost and found adverts
- `activity_item_list.xml` — layout for the advert list screen
- `ItemDetailActivity.java` — displays the full details of a selected advert and allows removal
- `activity_item_detail.xml` — layout for the item detail screen
- `MapActivity.java` — displays saved lost and found items on a map and supports radius-based searching
- `activity_map.xml` — layout for the map screen and radius search controls
- `LostFoundAdapter.java` — connects lost and found item data to the RecyclerView
- `item_lost_found.xml` — layout for a single advert item in the list
- `LostFoundItem.java` — model class representing one lost or found advert, including latitude and longitude
- `DatabaseHelper.java` — creates and manages the SQLite database
- `ic_image_placeholder.xml` — placeholder image used before an advert image is uploaded
- `ic_launcher_foreground.xml` — foreground resource for the app launcher icon
- `strings.xml` — stores text values used in the interface
- `colors.xml` — stores colour values used by the app
- `themes.xml` — defines the app theme and styling

---

## How the App Works

The user opens the application and is presented with the **Home** screen. From here, the user can choose to create a new advert, view all lost and found items, or show items on a map.

When creating a new advert, the user enters:

- whether the item is Lost or Found
- item name
- phone number
- description
- date
- location
- category
- image

The user can also press **Get Current Location** to capture their current latitude and longitude. This allows the advert to be displayed on the map and included in radius-based search results.

When the advert is saved, the app stores the item details, image URI, timestamp, location text, latitude, and longitude in the SQLite database.

The user can open the **Item List** screen to view all saved lost and found adverts. The list can be searched and filtered by category to help users find relevant items more easily.

When an advert is selected from the list, the app opens the **Item Detail** screen. This screen displays the full information for the selected advert, including the uploaded image and timestamp. The user can remove the advert once the item has been found.

The user can also open the **Map** screen using the **Show On Map** button. This screen displays saved lost and found items as map markers. The radius search feature allows the user to show only items within a selected distance from their current location.

All advert data is stored locally on the device, allowing the app to keep data between sessions.

---

## Geo Features

This updated version adds geo-location functionality to the original Lost and Found app.

The geo features include:

- A location field for each advert
- A **Get Current Location** button
- Latitude and longitude storage in SQLite
- A **Show On Map** button on the home screen
- A map screen that displays saved adverts
- Markers showing where lost and found items were reported
- Radius-based search to show only items within a selected distance

These features make the app more useful because users can find lost and found items based on physical location, not only by text or category.

---

## Technologies Used

- Java
- XML
- SQLite
- SQLiteOpenHelper
- RecyclerView
- Intent-based navigation
- Image Picker
- Date Picker
- Location services
- WebView map display
- Radius-based distance calculation
- Material Design Components

---

## How to Run the Project

1. Open the project in Android Studio
2. Allow Gradle to sync completely
3. Build the project
4. Run the app on an emulator or Android device
5. Allow location permission when prompted
6. Use the emulator location controls or a real Android device to test current location features

---

## Author

Rohan Korlahalli
