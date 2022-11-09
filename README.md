# <div align="center">APPointment</div>
<div align="center"><img src="https://i.imgur.com/cUydtOy.png" height="480"></div>
<div align="center"><p>An Android app that connects your customers with your business</p></div>

<a name="readme-top"></a>
## Table of contents
- [Features](#features)
    - [Contact business](#contact-business)
    - [Customer profile](#customer-profile)
    - [Notifications](#notifications)
    - [Business opening hours](#business-opening-hours)
- [Requirements](#requirements)
    - [Project](#project)
    - [Firebase](#firebase)
        - [Authentication](#authentication)
        - [Firestore](#firestore)
- [Built with](#built-with)
- [Credits](#credits)
- [Companion app](#companion-app)
## Features

### Contact business
The contact with the business was never been easier. Just choose one of the provided contact methods:
- Calling
- Navigation
- Open Facebook page
- Chat on Facebook Messenger
- Open Instagram page

<sup>All operations handled by external apps through Intents</sup>

### Customer profile
Let your customers create a profile and provide you with contact information.

<sup> Customer must first verify their email in order to proceed

#### Notifications
Customers will receive push notifications with FCM for important updates that you sent.

### Business opening hours
Provide your opening hours to your customers.

<p align="right"><sup>(<a href="#readme-top">back to top</a>)</sup></p>

## Requirements

### Project

To build the project you must provide:

- a google-services.json file in the root project* folder. You can retrieve it from "Project Settings" at Firbase console

- a .jks file in order to sign the release build

- a keystore.properties file with the following structure:

```

storePassword = <Keystore password>

keyPassword = <Signing key password>

keyAlias = <Key alias>

storeFile = <Path to the .jks file>

```

Those fields corresponds to properties at module level **build.gradle**\*\* file.

You need both .jks and keystore.properties files in order to sign the release build.



<sup>\*  *Be sure to check the official Firebase documentation*</sup>

<sup>\** *keystore.properties file's path should also be changed inside build.gradle if the file is not located at project's root*</sup>



- a fb_secrets.xml which will include the following strings:

```xml

<string  name="facebook_app_id_secret"  translatable="false">your_app_id</string>

<string  name="fb_login_protocol_scheme_secret"  translatable="false">fbyour_app_id</string>

<string  name="facebook_client_token_secret"  translatable="false">your_client_token</string>

```

<sup>*Be sure to check the official Facebook Login Android documentation*</sup>

- a googleApi.properties file with the following structure:
```
webClientId = "<your_web_client_id>"
```
You can find your web client ID  from Google API console, navigating to the "Credentials" tab. This key is needed for Google One Tap sign-in.

<p align="right"><sup>(<a href="#readme-top">back to top</a>)</sup></p>

### Firebase

#### Authentication

In order for the app to work without any modifications you must enable the following sign-in methods from Firebase console:

- Email/Password

- Google

- Facebook*



<sup>\*  *Facebook sign-in method must be enabled in order to link user's email or Google account with their Facebook one. It is not used for authentication.*</sup>

#### Firestore

You must provide the following database structure to provide the businesses' opening hours and social information:

<details>
	<summary>Structure</summary>

```

firestore {

	business_info {

		<business_name> {

			opening_hours: array {

				morning: map {

					from: string

					to: String

				}

				afternoon: map {

					from: string

					to: string

				}

				order: number

			}

			social_info: map {

				fbPageName: string

				fbPageUniqueName: string

				fb_page_id: string

				instagram_profile: string

				mapsCoordinates: string

				mapsName: string

				maps_query: string

				phone: string

			}

		}

	}

}

```
<p align="right"><sup>(<a href="#readme-top">back to top</a>)</sup></p>
</details>

<details>
	<summary>Fields description</summary>

|Field|Description|
|-|-|
|<business_name>|Business name|
|opening_hours|An array which holds the week's opening hours|
|morning|A map that holds the morning opening hours. Set it to **null** if business is closed on morning hours.|
|afternoon|A map that holds the morning opening hours. Set it to **null** if business is closed on afternoon hours.|
|from|Opening hour|
|to|Closing hour|
|order|In which order it will appear|
|fbPageName|Page's name|
|fbPageUniqueName|Page's unique name to avoid conflicts|
|fb_page_id|Page's id (you can take it from the url)|
|mapsCoordinates|Latitude and Longitude (separated by ',')|
|mapsName|Business name on Google Maps|
|maps_query|What you would search on Google Maps to find the business tip: include area's location to avoid conflicts|
phone|Business' phone|

</details>
<p align="right"><sup>(<a href="#readme-top">back to top</a>)</sup></p>

## Built with
Here is a brief summary of what technologies have been used for the app:
-	[Firebase](https://github.com/firebase/FirebaseUI-Android) for authentication and storage
-	[Coroutines](https://developer.android.com/kotlin/coroutines)
-	[FCM](https://firebase.google.com/docs/cloud-messaging) for push notifications
-	[Retrofit](https://github.com/square/retrofit) and [Moshi](https://github.com/square/moshi) to communicate wirh the FCM API
-	[Google Sign-In for Android](https://developers.google.com/identity/sign-in/android) for Google One Tap sign-in
-	[Navigation component](https://developer.android.com/guide/navigation/navigation-getting-started)
-	[Coil](https://github.com/coil-kt/coil) to load profile image

## Credits
This software uses the following open source packages:
- [Retrofit](https://github.com/square/retrofit)
- [Moshi](https://github.com/square/moshi)
- [Coil](https://github.com/coil-kt/coil)
- [FirebaseUI for Android](https://github.com/firebase/FirebaseUI-Android)
- [CircleImageView](#https://github.com/hdodenhof/CircleImageView)

## Companion app

The administration app is available on the [APPointment Admin repo](https://github.com/DTselikis/APPointment-Admin)

<p align="right"><sup>(<a href="#readme-top">back to top</a>)</sup></p>
