package com.luis.bioref;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.apache.http.NameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListarBDiversidade extends ListActivity {

    private ProgressDialog pDialog;

    // Creating JSON Parser object
    JSONParser jParser = new JSONParser();
    EditText inputSearch;
    ArrayList<HashMap<String, String>> caracList;
    ListAdapter adapter;
    /*= new SimpleAdapter(ListarBDiversidade.this, caracList,
    R.layout.list_bdiversidade, new String[]{TAG_IDCARAC, TAG_NCOMUM},
    new int[]{R.id.id_caracteristicas, R.id.nome_comum})*/

    // url to get all products list
    private static String url_all_bdiversidade = "http://192.168.1.5/android_connect/get_list_bdiversidade.php";

    // JSON Node names
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_CARACTERISTICAS = "caracteristicas";
    private static final String TAG_IDCARAC = "id_caracteristicas";
    private static final String TAG_NCOMUM = "nome_comum";



    // products JSONArray
    JSONArray caracteristicas = null;

    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.all_bdiversidade );

        EditText inputSearch = (EditText) findViewById(R.id.inputSearch);
        //  String elementos[] = getResources().getStringArray(R.id.nome_comum);
        new LoadAllElements().execute();
        caracList = new ArrayList<HashMap<String, String>>();
        ListView lv = getListView();


        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String id_caracteristicas = ((TextView) view.findViewById(R.id.id_caracteristicas)).getText()
                        .toString();



                // Starting new intent
                Intent in = new Intent(getApplicationContext(),
                        InfoBDiversidade.class);
                // sending pid to next activity
                in.putExtra(TAG_IDCARAC, id_caracteristicas);

                // starting new activity and expecting some response back
                startActivityForResult(in, 100);
            }
        });

        inputSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) {
                // When user changed the Text
                ((SimpleAdapter) ListarBDiversidade.this.adapter).getFilter().filter(cs);
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
                                          int arg3) {
                // TODO Auto-generated method stub

            }

            @Override
            public void afterTextChanged(Editable arg0) {
                // TODO Auto-generated method stub
            }

        });




    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // if result code 100
        if (resultCode == 100) {
            // if result code 100 is received
            // means user edited/deleted product
            // reload this screen again
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }

    }



    class LoadAllElements extends AsyncTask<String, String, String> {




        @Override
        protected String doInBackground(String... args) {
            // TODO Auto-generated method stub
            // Building Parameters
            List<NameValuePair> params = new ArrayList<NameValuePair>();
            // getting JSON string from URL
            JSONObject json = jParser.makeHttpRequest(url_all_bdiversidade, "GET", params);

            // Check your log cat for JSON reponse
            Log.d("Elementos: ", json.toString());

            try {

                int success = json.getInt(TAG_SUCCESS);

                if (success == 1) {

                    caracteristicas = json.getJSONArray(TAG_CARACTERISTICAS);

                    for (int i = 0; i < caracteristicas.length(); i++) {
                        JSONObject c = caracteristicas.getJSONObject(i);

                        String id_caracteristicas = c.getString(TAG_IDCARAC);
                        String nome_comum = c.getString(TAG_NCOMUM);
                        HashMap<String, String> map = new HashMap<String, String>();
                        map.put(TAG_IDCARAC, id_caracteristicas);
                        map.put(TAG_NCOMUM, nome_comum);

                        // adding HashList to ArrayList
                        caracList.add(map);
                    }


                } else {
                    // no products found
                    // Launch Add New product Activity
                    Intent i = new Intent(getApplicationContext(),
                            MapActivity.class);
                    // Closing all previous activities
                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(i);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;

        }


        /**
         * Before starting background thread Show Progress Dialog
         */
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(ListarBDiversidade.this);
            pDialog.setMessage("A carregar elementos. Please wait...");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(false);
            pDialog.show();
        }


        /**
         * After completing background task Dismiss the progress dialog
         * *
         */
        protected void onPostExecute(String file_url) {

            pDialog.dismiss();
            // updating UI from Background Thread
            runOnUiThread(new Runnable() {
                public void run() {


                    adapter = new SimpleAdapter(
                            ListarBDiversidade.this, caracList,
                            R.layout.list_bdiversidade, new String[]{TAG_IDCARAC, TAG_NCOMUM},
                            new int[]{R.id.id_caracteristicas, R.id.nome_comum});

                    // updating listview
                    setListAdapter(adapter);
                    //getListView().setTextFilterEnabled(true);
                }
            });

        }


    }

}
