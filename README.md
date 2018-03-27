# PublicTransportApp.Android

## To build project add the following files:
* Credentials.java to the helpers folder
* google_maps_api.xml to the resources folder
* google-services.json to the app folder

### Credentials.java (location: app/src/main/java/com/chrisking/publictransportapp/helpers/)

```
package com.chrisking.publictransportapp.helpers;

public final class Credentials {
    public final static String ClientId = "<ADD_YOUR_CLIENT_ID_HERE>";
    public final static String ClientSecret = "<ADD_YOUR_CLIENT_SECRET_HERE>";
    public final static String FlurryKey = "<ADD_YOUR_FLURRY_KEY_HERE>";
}
```

### Google_maps_api.xml (location: app/src/debug/res/values/)

```
<resources>
    <string name="google_maps_key" templateMergeStrategy="preserve" translatable="false">
        <YOUR_KEY_HERE>
    </string>
</resources>
```

### Google-services.json (location: app/)

```
{
  "project_info": {
    "project_number": "",
    "firebase_url": "",
    "project_id": "",
    "storage_bucket": ""
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "",
        "android_client_info": {
          "package_name": "com.chrisking.publictransportapp"
        }
      },
      "oauth_client": [
        {
          "client_id": "",
          "client_type": 3
        }
      ],
      "api_key": [
        {
          "current_key": ""
        }
      ],
      "services": {
        "analytics_service": {
          "status": 1
        },
        "appinvite_service": {
          "status": 1,
          "other_platform_oauth_client": []
        },
        "ads_service": {
          "status": 2
        }
      }
    }
  ],
  "configuration_version": "1"
}
```
