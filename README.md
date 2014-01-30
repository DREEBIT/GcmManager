# GcmManager

This is a helper singleton for the Google Cloud Messages API. Based on Google Play Services 4 for Android.

The code for device registration is taken from the Google Developer Guide "[Implementing GCM Client](http://developer.android.com/google/gcm/client.html "Implementing GCM Client")".

## Usage

Simply initialize GcmManager in any class (e.g. your application class). Set the FeedbackListener to perform callback

```java
import main.java.com.dreebit.gcm.GcmFeedbackListener;
import main.java.com.dreebit.gcm.GcmManager;

public class YourApplication extends Application implements GcmFeedbackListener{

	private NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;

    @Override
    public void onCreate() {

        super.onCreate();
        GcmManager.initialize(getApplicationContext(), "YOUR_SENDER_ID", this);

    }

	@Override
    public void onRecoverableError(int resultCode) {

        Toast.makeText(getApplicationContext(), GooglePlayServicesUtil.getErrorString(resultCode), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onDeviceWillRegister() {
    	// Do some stuff before registration
    }

    @Override
    public void onDeviceDidRegister(String result) {
    	// Do some stuff after registration
    }

    @Override
    public void onDeviceRegistered(String regId) {

        Log.v(TAG, "Device registered: " +regId);

    }

    @Override
    public void onReceiveMessage(String message) {

        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, DashboardActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.vsm_logo)
                        .setContentTitle("Notification")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(message))
                        .setContentText(message);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(1, mBuilder.build());

    }
}
```

## Sources

- Google Developer Guide "[Implementing GCM Client](http://developer.android.com/google/gcm/client.html "Implementing GCM Client")"

## License

GcmManager is available under the MIT license. See the LICENSE file for more info.


