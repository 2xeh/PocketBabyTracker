package com.example.pocketbabytracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

// AA NOTE: This is from the Html to PDF Rocket example

/**
 * <h1>NetworkAvailability</h1> This purpose of this class to check state of
 * network.
 *
 * @author Canopus
 * @version 1.1
 * @since 2014-08-15
 */
public class NetworkAvailability {

    /**
     * Get the network info
     * @param context
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo();
    }



    /**
     * This method is used to just check the status of internet connection
     *
     * @param context
     * @return {@link Boolean} true if state in connected available otherwise
     *         false
     */
    public static boolean chkStatus(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        return connMgr.getActiveNetworkInfo() != null
                && connMgr.getActiveNetworkInfo().isAvailable()
                && connMgr.getActiveNetworkInfo().isConnected();
    }

    /**
     * This method is used to just check the connection of internet connection
     *
     * @param context
     * @return {@link Boolean} true if connection available otherwise false
     */
    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {

            return true;
        }
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (mobileNetwork != null && mobileNetwork.isConnected()) {
            return true;
        }
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnected()) {
            return true;
        }
        return false;
    }

    /**
     * This method is used to just check the connection of internet connection
     *
     * @param context
     * @return {@link Boolean} true if connection available otherwise false
     */
    public static String typeConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileNetwork = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiNetwork != null && wifiNetwork.isConnected()) {
            String Wifi = "WifiConnected";
            return Wifi;
        }else if (mobileNetwork != null && mobileNetwork.isConnected()) {
            String subtype = mobileNetwork.getSubtypeName();
            String mobile = "MobileConnected";
            return mobile;
        }else {


        }

        return "notconneted";
    }
    /**
     * Checking for all possible internet providers
     * **/

    public static boolean isConnectingToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)
                for (int i = 0; i < info.length; i++)
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }

        }
        return false;
    }

    /**
     * Check if there is fast connectivity
     * @param context
     * @return
     */
    public static boolean isConnectedFast(Context context){
        NetworkInfo info = NetworkAvailability.getNetworkInfo(context);
        return (info != null && info.isConnected() && NetworkAvailability.isConnectionFast(info.getType(),info.getSubtype()));
    }

    /**
     * Check if the connection is fast
     * @param type
     * @param subType
     * @return
     */
    public static boolean isConnectionFast(int type, int subType){
        if(type==ConnectivityManager.TYPE_WIFI){
            return true;
        }else if(type==ConnectivityManager.TYPE_MOBILE){
            switch(subType){
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return true; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
			/*
			 * Above API level 7, make sure to set android:targetSdkVersion
			 * to appropriate level to use these
			 */
                case TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    return false;
            }
        }else{
            return false;
        }
    }


}