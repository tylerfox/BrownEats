package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.customtabs.CustomTabsSession;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.codec.binary.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;


public class CapacityTask extends AsyncTask<Void, Void, Boolean> {
    Context c;
    int networkState;

    public CapacityTask(Context c) {
        this.c = c;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        if (networkState == 1 || networkState == 2) {
            String rattyJSON = GET2(c.getString(R.string.rattyUrl));
            String josJSON = GET2(c.getString(R.string.josUrl));
            String blueroomJSON = GET2(c.getString(R.string.blueroomUrl));
            String andrewsJSON = GET2(c.getString(R.string.andrewsUrl));
            String vdubJSON = GET2(c.getString(R.string.vdubUrl));
            setCapacity("Ratty", rattyJSON);
            setCapacity("V-Dub", vdubJSON);
            setCapacity("Andrews Commons", andrewsJSON);
            setCapacity("Blue Room", blueroomJSON);
            setCapacity("Josiah's", josJSON);
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onPreExecute() {
        networkState = MainActivity.getNetworkState(c);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        if (success) {
            if (!MainActivity.taskFinished) {
                MainActivity.taskFinished = true;
            } else {
                if (!MainActivity.cardsLoaded) {
                    if (((MainActivity) c).fragment instanceof HomeFragment) {
                        ((HomeFragment) ((MainActivity) c).fragment).displayCards();
                    }
                }
            }
        }
    }

    public void setCapacity(String eatery, String json) {
        try {
            JSONArray arr = new JSONArray(json);
            ArrayList<CountPoint> toStore = new ArrayList<>();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject cur = arr.getJSONObject(i);
                long time = Long.parseLong(cur.get("timestamp").toString()) * 1000;
                int count = Integer.parseInt(cur.get("count").toString());
                toStore.add(new CountPoint(time, count));
            }
            MainActivity.countMap.put(eatery, toStore);
        } catch (JSONException e) {
            Log.d("DINING_ERROR", "Capacity JSON Error");
        }
    }

    /*public String GET(String url) {
        StringBuilder sb = new StringBuilder();
        try {
            URL u = new URL(url);
            BufferedReader br = new BufferedReader(new InputStreamReader(u.openStream()));
            String line = "";
            while (null != (line = br.readLine())) {
                sb.append(line);
            }
        } catch (MalformedURLException e) {
            Log.d("DINING_ERROR", "Capacity Malformed URL: \t" + e.getMessage());
        } catch (IOException e) {
            Log.d("DINING_ERROR", "Capacity IO error: \t" + e.getMessage());
        }
        return sb.toString();
    }*/

    public String GET2(String urlString) {
        StringBuilder sb = new StringBuilder();
        try {
            URL url = new URL(urlString);
            URLConnection uc = url.openConnection();
            String authString = "Bp4rczKMjY8JjFYUzPDb" + ":" + "7ho5n78ZdLSysCOthDy5u76IMDOlqZVl66KbOyJF";
            byte[] authEncBytes = Base64.encodeBase64(authString.getBytes());
            String authStringEnc = new String(authEncBytes);
            uc.setRequestProperty("Authorization", "Basic " + authStringEnc);
            InputStream in = uc.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            String line;
            while (null != (line = br.readLine())) {
                sb.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
}