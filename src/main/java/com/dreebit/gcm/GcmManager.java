package main.java.com.dreebit.gcm;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;

/**
 * Created by Toni on 30.01.14.
 */
public class GcmManager {

    private static final String TAG = "com.dreebit.gcm.GcmManager";

    public static final String EXTRA_MESSAGE = "message";
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    private static GcmManager INSTANCE = null;

    private final String mSenderId;

    private final Context mContext;

    private GoogleCloudMessaging mGcm;

    private GcmFeedbackListener mFeedbackListener;

    private String mRegId;

    public static GcmManager sharedInstance() {
        return INSTANCE;
    }

    public static void initialize(Context context, String senderId, GcmFeedbackListener feedbackListener) {
        INSTANCE = new GcmManager(context, senderId, feedbackListener );
    }

    public GcmManager(Context context, String senderId, GcmFeedbackListener feedbackListener) {
        this.mSenderId = senderId;
        this.mContext = context;

        this.setFeedbackListener(feedbackListener);

        this.startService();
    }

    private void startService() {

        if (this.checkPlayServices()){
            mGcm = GoogleCloudMessaging.getInstance(mContext);
            String regid = getRegistrationId(mContext);

            if (regid.isEmpty()) {
                registerInBackground();
            }
        }

    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs;
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    public boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(mContext);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                if (mFeedbackListener != null){
                    mFeedbackListener.onRecoverableError(resultCode);
                }

            } else {
                Log.i(TAG, "This device is not supported.");
            }
            return false;
        }
        return true;
    }


    /**
     * Registers the application with GCM servers asynchronously.
     * <p>
     * Stores the registration ID and app versionCode in the application's
     * shared preferences.
     */
    private void registerInBackground() {

        new AsyncTask<String, Void, String>() {

            @Override
            protected void onPreExecute()
            {
                mFeedbackListener.onDeviceWillRegister();
            }

            @Override
            protected String doInBackground(String... params) {
                String msg = "";
                try {
                    if (mGcm == null) {
                        mGcm = GoogleCloudMessaging.getInstance(mContext);
                    }
                    mRegId = mGcm.register(mSenderId);

                    Log.v(TAG, "Device registered, registration ID=" + mRegId);
                    msg = mRegId;

                    GcmManager.this.storeRegistrationId(mContext, mRegId);
                    mFeedbackListener.onDeviceRegistered(mRegId);


                } catch (IOException ex) {
                    msg = ex.getMessage();
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String res)
            {
                mFeedbackListener.onDeviceDidRegister(res);
                if(isCancelled()){
                    return;
                }
            }

        }.execute(null, null, null);

    }


    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }


    public void setFeedbackListener(GcmFeedbackListener feedbackListener){
        mFeedbackListener = feedbackListener;
    }

    public GcmFeedbackListener getFeedbackListener(){
        return mFeedbackListener;
    }
}
