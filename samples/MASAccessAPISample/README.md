# Access API Sample App

**Required:**
* Latest Android Studio Version
* Device with Passcode and/or Fingerprint locks enabled

## Getting Started
1. Open the project 'MASAccessAPISample' in Android Studio
2. Go to your servers policy manager or Mobile Developer Console if you have one, and create an app and download export the msso_config (https://you_server_name:8443/oauth/manager) [Visit mas.ca.com and navigate to the iOS Guides under docs for more info]
3. Copy the entire contents of the exported msso_config into src/main/assets/msso_config.json
4. Import /utils/tradePolicy.xml into your policy manager as an endpoint with the resolution path '/trade'
5. Build and Deploy the app to a device
