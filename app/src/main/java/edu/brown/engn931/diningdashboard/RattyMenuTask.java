package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class RattyMenuTask extends AsyncTask<Void, Void, String> {
    Context c;
    String clientId = "d4d562cc-42c3-42ba-aa44-4f045ae77112";
    LinearLayout layout;

    public RattyMenuTask(Context c) {
        this.c = c;
    }

    @Override
    protected String doInBackground(Void... params) {
        return GET("https://api.students.brown.edu/dining/menu?client_id=" + clientId + "&eatery=ratty");
    }

    @Override
    protected void onPostExecute(String json) {
        layout = ((DiningInfoActivity) c).infoLayout;
        try {
            JSONObject menu = new JSONObject(json).getJSONArray("menus").getJSONObject(0);
            addMealType(menu);
            addSection(menu, "bistro");
            addSection(menu, "chef's corner");
            addSection(menu, "daily sidebars");
            addSection(menu, "grill");
            addSection(menu, "roots & shoots");
        } catch (JSONException e) {
            Toast.makeText(c, "Error retreiving menu.", Toast.LENGTH_LONG).show();
        }
    }

    public void addMealType(JSONObject menu) {
        try {
            String meal =  menu.get("meal").toString();
            TextView textView = new TextView(c);
            textView.setText(" Current meal: " + meal);
            textView.setTextSize(16);
            textView.setPadding(0,0,0,10);
            layout.addView(textView);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void addSection(JSONObject menu, String sectionName) {
        try {
            JSONArray arr = menu.getJSONArray(sectionName);
            layout.addView(createHeader(sectionName));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < arr.length(); i++) {
                sb.append("\t\u2022 ").append(arr.get(i)).append("\n");
            }
            layout.addView(createText(sb.toString()));
        } catch (JSONException e) {
        }
    }

    public TextView createHeader(String text) {
        TextView textView = (TextView) ((DiningInfoActivity)c).getLayoutInflater().inflate(R.layout.menu_header, null);
        textView.setText(text);
        return textView;
    }

    public TextView createText(String text) {
        TextView textView = new TextView(c);
        textView.setText(text);
        textView.setPadding(0, 0, 16, 5);
        textView.setTextSize(17);
        return textView;
    }

    public String GET(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            URL u = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
            String line = "";
            while (null != (line = br.readLine())) {
                sb.append(line);
            }
        } catch (MalformedURLException e) {
            Toast.makeText(c, "Bad Capacity URL", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d("DINING_ERROR", "Capacity IO error");
        }
        return sb.toString();
    }
}