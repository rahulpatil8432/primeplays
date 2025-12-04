/*
package com.rkonline.android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.JsonArrayRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.rkonline.android.GhantaAdapter;
import com.rkonline.android.GhantaModel;

public class GhantaListActivity extends AppCompatActivity {

//	latobold timetext;
    ListView listViewg;
    ArrayList<GhantaModel> ghantaModelList;
	GhantaAdapter hlo;
    public final String API_URL = "https://rkonlinematka.in/api/ghantashow.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ghanta_list2);
        listViewg =(ListView) findViewById(R.id.listViewGhanta);
        ghantaModelList = new ArrayList<>();
        loadData();
//		timetext = (latobold) findViewById(R.id.time_market_one) ;
		hlo = new GhantaAdapter(this, ghantaModelList);
		listViewg.setAdapter(hlo);
                ((ArrayAdapter)listViewg.getAdapter()).notifyDataSetChanged();

				listViewg.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
//						String time = listViewg.getItemAtPosition(i).toString().trim();
						String time = String.valueOf((TextView)adapterView.findViewById(R.id.time_market_one));

//						String time = ((latobold)adapterView.findViewById(R.id.time_market_one)).getText().toString().trim();
						Intent intent = new Intent(GhantaListActivity.this,GhantaMenuActivity.class);
						intent.putExtra("time",time);

						startActivity(intent);


					}
				});

    }

    private void loadData() {
        RequestQueue mreq = Volley.newRequestQueue(this);

        JsonArrayRequest stringRequest = new JsonArrayRequest(Request.Method.GET, API_URL, null,
				new Response.Listener<JSONArray>() {
					@Override
					public void onResponse(JSONArray response) {
						for (int i = 0; i < response.length(); i++) {
						try {
							JSONObject obj = response.getJSONObject(i);
								//String result = obj.getString("results");

								String times = obj.getString("times");
								String status = obj.getString("status");
								ghantaModelList.add(new GhantaModel(times, status));
								((ArrayAdapter) listViewg.getAdapter()).notifyDataSetChanged();

					} catch (JSONException e) {
						e.printStackTrace();
					}
						}
					}
				}, new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError err) {

					}
				});
                              mreq.add(stringRequest);
    }


}
*/
