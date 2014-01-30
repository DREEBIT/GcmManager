package main.java.com.dreebit.gcm;

/**
 * Created by Toni on 30.01.14.
 */
public interface GcmFeedbackListener {

    public void onRecoverableError(int resultCode);

    public void onDeviceWillRegister();

    public void onDeviceDidRegister(String result);

    public void onDeviceRegistered(String regId);

    public void onReceiveMessage(String message);

}
