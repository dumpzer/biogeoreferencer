package com.luis.bioref;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InfoBDiversidade extends Activity {

    TextView tvReino;
    TextView tvFilo;
    TextView tvClasse;
    TextView tvOrdem;
    TextView tvFamilia;
    TextView tvSFamilia;
    TextView tvGenero;
    TextView tvNComum;
    ImageView ivBdiversidade;

    String id_caracteristicas;
    String id_imagem;
    TextView showLat, showLng;
    // Progress Dialog
    private ProgressDialog pDialog;

    // JSON parser class
    JSONParser jsonParser = new JSONParser();

    // single product url
    private static final String url_bdiversidade_details = "http://192.168.1.5/android_connect/get_bdiversidade_details.php";
    private static String url_add_bdiversidade = "http://192.168.1.5/android_connect/marcar_elemento.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_CARAC = "caracteristicas";
    private static final String TAG_PID = "id_caracteristicas";
    private static final String TAG_IDI = "id_imagem";
    private static final String TAG_REINO = "reino";
    private static final String TAG_FILO = "filo";
    private static final String TAG_CLASSE = "classe";
    private static final String TAG_ORDEM = "ordem";
    private static final String TAG_FAMILIA = "familia";
    private static final String TAG_SUBFAMILIA = "subfamilia";
    private static final String TAG_GENERO = "genero";
    private static final String TAG_NCOMUM = "nome_comum";
    private static final String TAG_IMAGEM = "imagem";

    int pos;
    GPSTracker gps;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.bdiversidade_details );

        gps = new GPSTracker(InfoBDiversidade.this);

        if (gps.canGetLocation()) {
            //TextView showLat, showLng;
            showLat = (TextView) findViewById(R.id.tvLat);
            showLng = (TextView) findViewById(R.id.tvLng);

            double latitude = gps.getLatitude();
            String lat = Double.toString(latitude);
            showLat.setText("Latitude: " + lat + "   ");
            double longitude = gps.getLongitude();
            String lng = Double.toString(longitude);
            showLng.setText("Longitude: " + lng);

            //double longitude = gps.getLongitude();
            // showLat.setText(String.valueOf((gps.getLatitude())));
            //showLng.setText(String.valueOf((gps.getLongitude())));
            // \n is for new line
            // Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }

        // save button
        //       btnSave = (Button) findViewById(R.id.btnSave);
        Button btnCreateAnimal = (Button) findViewById(R.id.btnMarcar);

        // button click event
        btnCreateAnimal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // creating new product in background thread

                new CreateNewBdiversidade().execute();

            }


        });

        // getting product details from intent
        Intent i = getIntent();

        // getting product id (pid) from intent
        id_caracteristicas = i.getStringExtra(TAG_PID);
        id_imagem = i.getStringExtra(TAG_IDI);
        // Getting complete product details in background thread
        new GetBioDetails().execute();

        Button btnPictures = (Button)findViewById(R.id.btnPictures);

        btnPictures.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent in = new Intent(getApplicationContext(),
                        UserPicture.class);
                v.getContext().startActivity(in);
            }
        });

    }

    class GetBioDetails extends AsyncTask<String, String, String> {

        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InfoBDiversidade.this);
            pDialog.setMessage("Loading element details. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }

        /**
         * Getting product details in background thread
         */
        @Override
        protected String doInBackground(String... params) {

            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Check for success tag
                    int success;
                    try {
                        // Building Parameters
                        List<NameValuePair> params = new ArrayList<NameValuePair>();
                        params.add(new BasicNameValuePair("id_caracteristicas", id_caracteristicas));
                        // params.add(new BasicNameValuePair("id_imagem", id_imagem));
                        // getting product details by making HTTP request
                        // Note that product details url will use GET request
                        JSONObject json = jsonParser.makeHttpRequest(
                                url_bdiversidade_details, "GET", params);

                        // check your log for json response
                        Log.d("Single Product Details", json.toString());

                        // json success tag
                        success = json.getInt(TAG_SUCCESS);
                        if (success == 1) {
                            // successfully received product details
                            JSONArray productObj = json
                                    .getJSONArray(TAG_CARAC); // JSON Array

                            // get first product object from JSON Array
                            JSONObject product = productObj.getJSONObject(0);

                            // product with this pid found
                            // Edit Text
                            tvNComum = (TextView) findViewById(R.id.tvNComum);
                            //ivBdiversidade = (ImageView) findViewById(R.id.ivBdiversidade);
                            tvReino = (TextView) findViewById(R.id.tvReino);
                            tvFilo = (TextView) findViewById(R.id.tvFilo);
                            tvClasse = (TextView) findViewById(R.id.tvClasse);
                            tvOrdem = (TextView) findViewById(R.id.tvOrdem);
                            tvFamilia = (TextView) findViewById(R.id.tvFamilia);
                            tvSFamilia = (TextView) findViewById(R.id.tvSFamilia);
                            tvGenero = (TextView) findViewById(R.id.tvGenero);

                            String imageStr = product.getString(TAG_IMAGEM);
                            byte[] decodedString = Base64.decode(imageStr, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            ImageView image = (ImageView) findViewById(R.id.ivBdiversidade);
                            image.setImageBitmap(decodedByte);
                            //System.out.print(image);

                            // display product data in EditText
                            tvNComum.setText(product.getString(TAG_NCOMUM));
                            tvReino.setText(product.getString(TAG_REINO));
                            tvFilo.setText(product.getString(TAG_FILO));
                            tvClasse.setText(product.getString(TAG_CLASSE));
                            tvOrdem.setText(product.getString(TAG_ORDEM));
                            tvFamilia.setText(product.getString(TAG_FAMILIA));
                            tvSFamilia.setText(product.getString(TAG_SUBFAMILIA));
                            tvGenero.setText(product.getString(TAG_GENERO));


                        } else {
                            // product with pid not found
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });

            return null;
        }

        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once got all details
            pDialog.dismiss();
        }
    }

    class CreateNewBdiversidade extends AsyncTask<String, String, String> {
        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(InfoBDiversidade.this);
            pDialog.setMessage("A Adicionar Elemento..");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            pDialog.show();
        }


        @Override
        protected String doInBackground(String... args) {
            Intent i = getIntent();
            String id_caracteristicas2 = i.getStringExtra(TAG_PID);
            double latitude2 = gps.getLatitude();
            String lati = String.valueOf(latitude2);
            double longitude2 = gps.getLongitude();
            String lngi = String.valueOf(longitude2);

            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("id_caracteristicas", id_caracteristicas2));
            params.add(new BasicNameValuePair("latitude", lati));
            params.add(new BasicNameValuePair("longitude", lngi));
            // getting JSON Object
            // Note that create product url accepts POST method
            JSONObject json = jsonParser.makeHttpRequest(url_add_bdiversidade, "POST", params);


            // check log cat fro response
            Log.d("Create Response", json.toString());

            // check for success tag
            try {
                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {
                    // successfully created product
                    Intent in = new Intent(getApplicationContext(), PictureActivity.class);
                    startActivity(in);
                    // closing this screen
                    finish();
                } else {

                    // failed to create product
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String file_url) {
            // dismiss the dialog once done
            pDialog.dismiss();
        }
    }


}
