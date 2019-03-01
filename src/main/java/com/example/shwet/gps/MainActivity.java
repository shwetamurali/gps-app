package com.example.shwet.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    URL url;
    URLConnection connection;
    InputStream stream;
    BufferedReader reader;
    String input;
    JSONObject loc;
    Geocoder geocoder;
    List<Address>addresses;

    double longitude, latitude;
    LocationListener locationListener;
    LocationManager locationManager;
    DecimalFormat decimalFormat = new DecimalFormat("0.######");

    TextView Lat, Long, addressText, distance;
    final ArrayList<Location> list = new ArrayList<>();
    String number, city, state,street;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Lat = (TextView) (findViewById(R.id.latitude));
        Long = (TextView) findViewById(R.id.longitude);
        addressText = (TextView) findViewById(R.id.address);
        distance = (TextView) findViewById(R.id.totalDistance);
        geocoder = new Geocoder(this, Locale.getDefault());

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            public void onLocationChanged(Location loc) {
                list.add(loc);
                longitude = loc.getLongitude();
                latitude = loc.getLatitude();
                Lat.setText("Latitude: " + decimalFormat.format(latitude));
                Long.setText("Longitude: " + decimalFormat.format(longitude));
                if (list.size() == 1) {
                    distance.setText ("Total Distance: " + "0.0 miles");
                }
                if (list.size() > 1) {
                    float totalDistance = 0;
                    for (int i = 1; i < list.size(); i++) {
                        Location firstLoc = list.get(i - 1);
                        Location secondLoc = list.get(i);
                        totalDistance += firstLoc.distanceTo(secondLoc);
                    }
                    double totalDist = 0.000621371192 * totalDistance;
                    distance.setText("Total Distance: " + decimalFormat.format(totalDist) + " miles");
                }
                try {
                    addresses = geocoder.getFromLocation(latitude, longitude, 1);
                    Log.d("Address Tag", String.valueOf(addresses.size()));
                    if(addresses.size()==0){
                        addressText.setText("Address Not Found");
                    }
                    else {
                        String address = addresses.get(0).getAddressLine(0);
                        addressText.setText(address);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
                AsyncThread locationThread = new AsyncThread(longitude, latitude);
                locationThread.execute();
            }

            public void onStatusChanged(String s, int i, Bundle bundle) {
            }

            public void onProviderEnabled(String s) {
            }

            public void onProviderDisabled(String s) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);

    }

    public class AsyncThread extends AsyncTask<Void, Void, JSONObject> {

        double lon, lati;

        public AsyncThread(double x, double y) {
            lon = x;
            lati = y;
        }

        protected JSONObject doInBackground(Void... voids) {
            try {
                url = new URL("https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lati + "," + lon + "&key=AIzaSyD5p0uBr6FapZFYG8OmWT-AmSs4A6-qO24");
                try {
                    connection = url.openConnection();
                    stream = connection.getInputStream();
                    reader = new BufferedReader(new InputStreamReader(stream));
                    StringBuffer stringBuffer = new StringBuffer("");
                    String line = null;
                    while ((line = reader.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    input = stringBuffer.toString();
                    loc = new JSONObject(input);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return loc;
        }

        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);
            JSONArray results = null;
            try {
                Log.d("tag", "hello");
                results = loc.getJSONArray("results");
                number = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(0).getString("long_name");
                street = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(1).getString("long_name");
                city = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(2).getString("long_name");
                state = results.getJSONObject(0).getJSONArray("address_components").getJSONObject(5).getString("short_name");

                addressText.setText(results.getJSONObject(0).getString("formatted_address"));

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}