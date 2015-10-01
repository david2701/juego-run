package com.example.dingtao.run;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import javax.xml.datatype.Duration;


public class MainActivity extends ActionBarActivity implements UpdateableView {
    Button startButton,pauseButton,saveButton;
    TextView speed,distance,averageSpeed;
    Model model;
    GoogleMap map;
    Polyline line;
    Chronometer time;
    CameraPosition lastCameraPosition;
    Long timebase;
    MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && !lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this,R.string.quit_notice_message, Toast.LENGTH_LONG).show();

            finish();
            return;
        }

        model = Model.Model(this);
        startButton = (Button) findViewById(R.id.Start);
        saveButton = (Button) findViewById(R.id.Save);
        speed = (TextView) findViewById(R.id.speed);
        distance = (TextView) findViewById(R.id.distance);
        averageSpeed = (TextView) findViewById(R.id.average_speed);

        //TODO:pauseButton = (Button) findViewById(R.id.Pause);
        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.Map);

        map = mapFragment.getMap();
        map.setMyLocationEnabled(true);

        time = (Chronometer) findViewById(R.id.time);
        if (savedInstanceState != null){
            time.setBase(savedInstanceState.getLong("timeBase"));
        }

        if(!(Thread.getDefaultUncaughtExceptionHandler() instanceof CustomExceptionHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new CustomExceptionHandler(this));
        }

    }

    @Override
    protected void onStart(){
        super.onStart();
        model.AddView(this);
        if (model.started) {
            Update(Model.TRACK_UPDATED);
            startButton.setText(R.string.Stop);
        }
    }

    @Override
    protected void onStop(){
        super.onStop();
        model.RemoveView(this);
        mapFragment.setRetainInstance(true);
        lastCameraPosition = map.getCameraPosition();

    }

    @Override
    protected void onSaveInstanceState(Bundle bundle){
        bundle.putLong("timeBase",time.getBase());
        super.onSaveInstanceState(bundle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void Start(View view){
        model.Start();
        if (model.started) {
            startButton.setText(R.string.Stop);
            time.setBase(SystemClock.elapsedRealtime());
            time.start();
            saveButton.setEnabled(false);
        }
        else {
            startButton.setText(R.string.Start);
            time.stop();
            timebase = null;
            saveButton.setEnabled(true);
            saveButton.setClickable(true);
            //TODO:pauseButton.setText(R.string.Pause);
        }
    }

    //TODO: PAUSE BUTTON
    /* public void Pause(MenuItem item){
        model.Pause();
        if (model.paused) {
            pauseButton.setText(R.string.Pause);
        }
        else {
            pauseButton.setText(R.string.Unpause);
        }
    } */

    public void Settings(MenuItem item){
        Intent intent = new Intent(this,SettingsActivity.class);
        startActivity(intent);
    }

    public void Runs(MenuItem item){
        Intent intent = new Intent(this,RunsActivity.class);
        startActivity(intent);
    }

    public void Save(View view){
        model.Save();
    }

    @Override
    public void Update(String msg) {
        if (msg == Model.TRACK_UPDATED){
            PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            for (LocationJSON locationJSON : model.run.tracks){
                options.add(new LatLng(locationJSON.latitude,locationJSON.longitude));
            }
            if (line != null){
                map.clear();
            }
            line = map.addPolyline(options);

            speed.setText("Speed: " + model.run.CurrentSpeedToKmPH());
            distance.setText("Distance: " + model.run.DistanceToKm());
            averageSpeed.setText("Average Speed: " + model.run.SpeedToKmPH());
        }
    }

    public void MoveCamera(CameraUpdate cameraUpdate){
        map.moveCamera(cameraUpdate);
    }
}

