package com.example.dingtao.run;

import android.location.Location;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Dingtao on 6/16/2015.
 */
public class LocationJSON {
    public double latitude;
    public double longitude;
    public double altitude;
    public double accuracy;
    public double bearing;
    public String provider;
    public long time;
    public double speed;

    public LocationJSON(Location location){
        accuracy = location.getAccuracy();
        latitude = location.getLatitude();
        longitude = location.getLongitude();
        altitude = location.getAltitude();
        bearing = location.getBearing();
        provider = location.getProvider();
        time = location.getTime();
        speed = location.getSpeed();

    }

    public Location ToLocation(){
        Location location = new Location(provider);
        location.setAccuracy((float)accuracy);
        location.setLatitude(latitude);
        location.setLongitude(longitude);
        location.setAltitude(altitude);
        location.setBearing((float)bearing);
        location.setTime(time);
        location.setSpeed((float)speed);

        return location;
    }

    public LocationJSON(JSONObject location) throws JSONException{
        accuracy = location.getDouble("accuracy");
        latitude = location.getDouble("latitude");
        longitude = location.getDouble("longitude");
        altitude = location.getDouble("altitude");
        bearing = location.getDouble("bearing");
        provider = location.getString("provider");
        time = location.getLong("time");
        speed = location.getDouble("speed");
    }

    public JSONObject ToJSONObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("accuracy",accuracy);
        jsonObject.put("latitude", latitude);
        jsonObject.put("longitude", longitude);
        jsonObject.put("altitude", altitude);
        jsonObject.put("bearing", bearing);
        jsonObject.put("provider", provider);
        jsonObject.put("time", time);
        jsonObject.put("speed", speed);

        return jsonObject;
    }

    protected boolean IsBetterLocation(LocationJSON location) {

        // Check whether the new location fix is newer or older
        long timeDelta = location.time - time;
        boolean isSignificantlyNewer = timeDelta > Model.max_time;
        boolean isNewer = timeDelta > 0;
        //

        if (isSignificantlyNewer) {
            //Log.i("Location Changed", "isSignificantlyNewer");
            return true;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (accuracy-location.accuracy);

        boolean isMoreAccurate = accuracyDelta <= 0;
//        Log.i("isSlightlyLessAccurate",String.valueOf(location.accuracy/accuracy));
//        Log.i("Accuracy Delta", String.valueOf(accuracyDelta));
//        Log.i("New and Old Accuracy", String.valueOf(location.accuracy) + " " +String.valueOf(accuracy));


        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate ) {
            //Log.i("Location Changed", "isMoreAccuate");
            return true;
        }
        return false;
    }

}
