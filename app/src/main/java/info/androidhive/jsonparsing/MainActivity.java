package info.androidhive.jsonparsing;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    private String TAG = MainActivity.class.getSimpleName();

    private ProgressDialog pDialog;
    private ListView lv;

    // URL to get contacts JSON
    private static String url = "http://api.androidhive.info/contacts/";

    private static String dataUrl = "https://www.stryd.com/b/interview/data";

    LineGraphSeries<DataPoint> series;

    ArrayList<HashMap<String, String>> contactList;

    ArrayList<Integer> powerList;

    ArrayList<Integer> heartList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        contactList = new ArrayList<>();
        powerList = new ArrayList<Integer>();
        heartList = new ArrayList<Integer>();

        //lv = (ListView) findViewById(R.id.list);

        new GetContacts().execute();
    }

    /**
     * Async task class to get json by making HTTP call
     */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(MainActivity.this);
            pDialog.setMessage("Grabbing Styrd backend data...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            HttpHandler sh = new HttpHandler();

            // Making a request to url and getting response
            String jsonStr = sh.makeServiceCall(url);

            Log.e(TAG, "Response from url: " + jsonStr);

            String dataJsonStr = sh.makeServiceCall(dataUrl);

            if (dataJsonStr != null) {
                try {
                    JSONObject jsonObj = new JSONObject(dataJsonStr);

                    JSONArray powerArray = jsonObj.getJSONArray("total_power_list");

                    //final int thename = powerArray.getInt(0);

                    for (int i = 0; i < powerArray.length(); i++) {
                        powerList.add(powerArray.getInt(i));
                    }

                    JSONArray heartArray = jsonObj.getJSONArray("heart_rate_list");

                    for (int i = 0; i < heartArray.length(); i++) {
                        heartList.add(heartArray.getInt(i));
                    }

                    /*runOnUiThread(new Runnable() {
                        public void run()
                        {
                            Toast.makeText(getApplicationContext(), thename, Toast.LENGTH_SHORT).show();
                        }
                    });*/


                    //Toast.makeText(getApplicationContext(), "the first number is " + num, Toast.LENGTH_LONG);

                } catch (final JSONException e) {
                    Log.e(TAG, "Json parsing error: " + e.getMessage());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getApplicationContext(),
                                    "Json parsing error: " + e.getMessage(),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();

            DataPoint[] points = new DataPoint[powerList.size()];
            for (int i = 0; i < points.length; i++) {
                points[i] = new DataPoint(heartList.get(i), powerList.get(i));

            }
            GraphView graph = (GraphView) findViewById(R.id.graph);
            LineGraphSeries<DataPoint> series = new LineGraphSeries<>(points);
            graph.getViewport().setYAxisBoundsManual(true);
            graph.getViewport().setMinY(10);
            graph.getViewport().setMaxY(20);

            graph.getViewport().setXAxisBoundsManual(true);
            graph.getViewport().setMinX(10);
            graph.getViewport().setMaxX(20);

            // enable scaling and scrolling
            graph.getViewport().setScalable(true);
            graph.getViewport().setScalableY(true);

            graph.addSeries(series);

            graph.setTitle("Power vs Heart Rate");
            /**
             * Updating parsed JSON data into ListView
             * */
        }

    }
}
