package com.example.dingtao.run;

import android.graphics.AvoidXfermode;
import android.location.Location;
import android.util.Log;
import android.widget.Toast;


import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dingtao on 6/14/2015.
 */
public class Run {
    public List<LocationJSON> tracks;
    public String name;
    public double distance;
    public double averageSpeed,topSpeed;
    public long duration;
    public long begin;
    public Location mostAccurateSoFar;

    public void ForceAddLocation(Location location){
        LocationJSON locationJSON = new LocationJSON(location);
        LocationJSON lastLocation = tracks.get(tracks.size()-1);
        tracks.add(locationJSON);
        distance += location.distanceTo(lastLocation.ToLocation());
        duration = tracks.get(tracks.size()-1).time - tracks.get(0).time;
        averageSpeed = distance / (duration / 1000);
        mostAccurateSoFar = null;
        Toast.makeText(Model.Get().main,
                "Old accuracy: " + lastLocation.accuracy + ", New accuracy: " + locationJSON.accuracy, Toast.LENGTH_LONG)
                .show();
    }

    public boolean AddLocation(Location location){
        LocationJSON locationJSON = new LocationJSON(location);
        LocationJSON lastLocation = tracks.get(tracks.size()-1);
        if (mostAccurateSoFar == null || location.getAccuracy() > mostAccurateSoFar.getAccuracy()){
            mostAccurateSoFar = location;
        }
        if (location.getTime() - lastLocation.time > Model.Get().max_time ||  mostAccurateSoFar.getAccuracy() > lastLocation.accuracy) {
            ForceAddLocation(mostAccurateSoFar);
            return true;
        }
        return false;
    }

    public JSONObject ToJSONObject() throws JSONException{
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name",name);
        jsonObject.put("distance",distance);
        jsonObject.put("averageSpeed",averageSpeed);
        jsonObject.put("duration",duration);
        jsonObject.put("begin",begin);

        JSONArray jsonArray = new JSONArray();
        for (LocationJSON locationJSON : tracks){
            jsonArray.put(locationJSON.ToJSONObject());
        }
        jsonObject.put("tracks", jsonArray);
        return jsonObject;
    }

    public Run(Location location){
        name = null;
        distance = 0;
        averageSpeed = location.getSpeed();
        begin = location.getTime();
        duration = 0;
        topSpeed = 0;
        tracks = new ArrayList<LocationJSON>();
        tracks.add(new LocationJSON(location));
    }

    public Run(JSONObject jsonObject)throws JSONException{
        tracks = new ArrayList<LocationJSON>();
        name = jsonObject.getString("name");
        distance = jsonObject.getDouble("distance");
        averageSpeed = jsonObject.getDouble("averageSpeed");
        duration = jsonObject.getLong("duration");
        begin = jsonObject.getLong("begin");
        JSONArray jsonArray = jsonObject.getJSONArray("tracks");
        for (int i = 0; i < jsonArray.length(); i++) {
            tracks.add(new LocationJSON(jsonArray.getJSONObject(i)));
        }
    }

    public String DistanceToKm(){
        return String.format("%1$,.2f",(distance / 1000)) + "km";
    }

    public String DurationToTime(){
        long second = (duration / 1000) % 60;
        long minute = (duration / (1000 * 60)) % 60;
        long hour = (duration / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hour, minute, second);
    }

    public String StartedToTime(){
        Date date = new Date(begin);
        DateFormat formatter = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
        return formatter.format(date);
    }

    public String SpeedToKmPH(){
        return String.format("%1$,.2f",averageSpeed/1000*3600) + "km/h";
    }

    public String CurrentSpeedToKmPH(){ return String.format("%1$,.2f",tracks.get(tracks.size()-1).speed /1000*3600) + "km/h"; }

}
