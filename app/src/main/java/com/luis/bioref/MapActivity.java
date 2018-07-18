package com.luis.bioref;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MapActivity extends Activity {

    private static final String LOG_TAG = "JSON ERROR";
    private static final String SERVICE_URL = "http://192.168.1.5/android_connect/get_marker_details.php";

    protected GoogleMap googleMap;
    JSONParser jParser = new JSONParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.map );

        setUpMapIfNeeded();
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable( getBaseContext() );
        if (status != ConnectionResult.SUCCESS) {

            int requestCode = 10;
            Dialog dialog = GooglePlayServicesUtil.getErrorDialog( status, this, requestCode );
            dialog.show();
        } else {
            SupportMapFragment fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById( R.id.map );
            googleMap = fm.getMap();
            googleMap.setMyLocationEnabled( true );

            LocationManager locationManager = (LocationManager) getSystemService( LOCATION_SERVICE );

            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider( criteria, true );

            // Getting Current Location
            if (ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Location location = locationManager.getLastKnownLocation( provider );

            if (location != null) {
                onLocationChanged(location);
            }

            locationManager.requestLocationUpdates(provider, 20000, 0, (LocationListener) this );
        }

        Bundle info = getIntent().getExtras();
        String username = info.getString("username");
        System.out.println(username);

    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {

            /*case R.id.action_settings:

                return true;
*/
            case R.id.add_elemento:

                makeToast("A redireccionar...");
                addElement();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void addElement() {
        // TODO Auto-generated method stub
        Intent i = new Intent(getApplicationContext(), ListarBDiversidade.class);
        startActivity(i);
    }

    public void makeToast(String message) {

        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

    }

    private void setUpMapIfNeeded() {
        if (googleMap == null) {
            googleMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
            if (googleMap != null) {
                setUpMap();
            }
        }
    }


    private void setUpMap() {

        new Thread(new Runnable() {
            public void run() {
                try {
                    retrieveAndAddMarkers();
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Cannot retrive cities", e);
                    return;
                }
            }
        }).start();
    }

    protected void retrieveAndAddMarkers() throws IOException {
        HttpURLConnection conn = null;
        final StringBuilder json = new StringBuilder();
        try {
            URL url = new URL(SERVICE_URL);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                json.append(buff, 0, read);
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, "ERROR CONNECTING TO SERVICE", e);
            throw new IOException("ERROR CONNECTING TO SERVICE", e);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
        runOnUiThread(new Runnable() {
            public void run() {
                try {
                    createMarkersFromJson(json.toString());
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error processing JSON", e);
                }
            }
        });
    }

    void createMarkersFromJson(String json) throws JSONException {

        JSONArray jsonArray = new JSONArray(json);
        System.out.print(json);
        List<Marker> markers = new ArrayList<Marker>();

        for (int i = 0; i < jsonArray.length(); i++) {

            JSONObject jsonObj = jsonArray.getJSONObject(i);
            System.out.print(jsonObj.getJSONArray("latlng"));


            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(jsonObj.getString("nome_comum"))
                    .snippet(jsonObj.getString("reino"))
                    .position(new LatLng(
                            jsonObj.getJSONArray("latlng").getDouble(0),
                            jsonObj.getJSONArray("latlng").getDouble(1)))
                    .icon( BitmapDescriptorFactory.fromResource(
                            R.drawable.ic_action_place))
            );
            markers.add(marker);
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }


    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stub
        double latitude = location.getLatitude();

        // Getting longitude of the current location
        double longitude = location.getLongitude();

        // Creating a LatLng object for the current location
        LatLng latLng = new LatLng(latitude, longitude);

        // Showing the current location in Google Map
        googleMap.moveCamera( CameraUpdateFactory.newLatLng(latLng));

        // Zoom in the Google Map
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15));
    }




}
