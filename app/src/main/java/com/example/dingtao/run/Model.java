package com.example.dingtao.run;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.sql.Driver;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Dingtao on 6/11/2015.
 */
public class Model implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,LocationListener{
    public static String TRACK_UPDATED = "TRACK UPDATED";
    public static String RUNS_UPDATED = "RUNS UPDATED";
    public boolean started,paused;
    public static int min_time,max_time;
    public Run run;
    public List<Run> runs;

    private GoogleApiClient mGoogleApiClient;

    SharedPreferences SP;
    private static Model instance;
    private static Boolean init = false;
    private Boolean connected = false;
    public MainActivity main;
    private List<UpdateableView> views;
    private String saveFile = "save.txt";

    public static Model Model(MainActivity context){
        if (init) {
            instance.main = context;
            return instance;
        }
        init = true;
        instance = new Model(context);
        instance.ReloadPreferences();
        return instance;
    }

    private Model(MainActivity context){
        main = context;
        SP = PreferenceManager.getDefaultSharedPreferences(context);
        views = new ArrayList<UpdateableView>();
        runs = new ArrayList<Run>();
        LoadFromFile();
        mGoogleApiClient = new GoogleApiClient.Builder(main).addApi(LocationServices.API).addConnectionCallbacks(this).addOnConnectionFailedListener(this).build();
        mGoogleApiClient.connect();
    }

    public void ReloadPreferences(){
        min_time = Integer.valueOf(SP.getString("min_time", "3000"));
        max_time = Integer.valueOf(SP.getString("max_time", "45000"));
        if (started){
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,createLocationRequest(),this);
        }
    }

    public static Model Get(){
        if (init) {
            return instance;
        }
        return null;
    }

    public void Start(){
        if (started){
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            run.ForceAddLocation(location);
            started = false;
            paused = false;
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient,this);
            main.Update(TRACK_UPDATED);
        }else{
            if (!connected){
                mGoogleApiClient.connect();
                try{wait(500);}catch(InterruptedException e){}
            }
            started = true;
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,createLocationRequest(),this);
            Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            run = new Run(lastLocation);
            main.Update(TRACK_UPDATED);
        }
    }

    public void AddLocation(Location location){

        if (run.AddLocation(location)){
            Update(TRACK_UPDATED);
        }
    }

    public void Save(){
        if (started) return;
        if (run == null || run.tracks.isEmpty()){
            DialogManager.NoInput(main);
            return;
        }
        DialogManager.NameRun(main, run);
    }

    public void RemoveRun(int rid){
        runs.remove(rid);
        Update(RUNS_UPDATED);
        WriteToFile();

    }

    public void WriteToFile(){
        JSONArray obj = new JSONArray();
        try {
            for (Run run : runs) {
                obj.put(run.ToJSONObject());
            }
        }catch (JSONException exception){}

        FileOutputStream os;
        try {
            os = main.openFileOutput(saveFile, Context.MODE_PRIVATE);
            os.write(obj.toString().getBytes());
        }catch(FileNotFoundException e){
        }catch(IOException e){}
    }

    public void LoadFromFile(){
        try {
            FileInputStream fis = main.openFileInput(saveFile);
            InputStreamReader isr = new InputStreamReader(fis);
            BufferedReader br = new BufferedReader(isr);
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            isr.close();

            JSONArray jsonRuns = new JSONArray(sb.toString());
            for (int i = 0; i< jsonRuns.length(); i++){
                JSONObject jsonRun = jsonRuns.getJSONObject(i);
                Run run = new Run(jsonRun);

                runs.add(run);
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        } catch (JSONException e){
            //Log.e("test","exception",e);
            runs.clear();
        }
    }

    public void Pause(){
        if (!started) return;

        if (paused){
            paused = false;
            Intent intent = new Intent(main,TrackingService.class);
            main.stopService(intent);
        }else{
            paused = true;
            Intent intent = new Intent(main,TrackingService.class);
            main.startService(intent);
        }
    }

    public boolean IsConnected(){
        return connected;
    }

    public void AddView(UpdateableView view){
        if (views.contains(view)) return;
        views.add(view);
    }

    public void RemoveView(UpdateableView view){
        if (!views.contains(view)) return;
        views.remove(view);
    }

    public void Update(String msg){
        for (UpdateableView view : views){
            view.Update(msg);
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        connected = true;
        Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        main.MoveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(),location.getLongitude()),15));
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        DialogManager.QuitNotice(main);
    }

    protected LocationRequest createLocationRequest(){
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(min_time);
        locationRequest.setFastestInterval(0);
        locationRequest.setMaxWaitTime(max_time);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return locationRequest;

    }

    @Override
    public void onLocationChanged(Location location) {
        //Log.i("Location Changed", location.toString());
        AddLocation(location);
    }

    public void ClearRuns(){
        runs.clear();
        try {
            OutputStream os = main.openFileOutput(saveFile, Context.MODE_PRIVATE);
            os.write("".getBytes());
        }catch(FileNotFoundException e){
        }catch(IOException e){}
    }

}
