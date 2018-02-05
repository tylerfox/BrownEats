package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

public class HoursTask extends AsyncTask<Void, Void, HashMap<String, String>> {
    Context c;
    String clientId = "d4d562cc-42c3-42ba-aa44-4f045ae77112";
    int networkState;

    public HoursTask(Context c) {
        this.c = c;
    }

    @Override
    protected HashMap<String, String> doInBackground(Void... params) {
        if (networkState == 1 || networkState == 2) {
            String rattyJSON = GET("https://api.students.brown.edu/dining/hours?client_id=" + clientId + "&eatery=ratty");
            HashMap<String, String> hoursMap = new HashMap<>();
            String vdubJSON = GET("https://api.students.brown.edu/dining/hours?client_id=" + clientId + "&eatery=vdub");
            hoursMap.put("Ratty", getHoursStatus("ratty", rattyJSON));
            hoursMap.put("V-Dub", getHoursStatus("vdub", vdubJSON));
            hoursMap.put("Andrews Commons", getOtherHours("Andrews Commons"));
            hoursMap.put("Blue Room", getOtherHours("Blue Room"));
            hoursMap.put("Josiah's", getOtherHours("Josiah's"));
            return hoursMap;
        } else {
            return null;
        }
    }

    @Override
    protected void onPreExecute() {
        networkState = MainActivity.getNetworkState(c);
    }

    @Override
    protected void onPostExecute(final HashMap<String, String> hoursMap) {
        if (hoursMap != null) {
            MainActivity.hoursMap = hoursMap;
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
            Toast.makeText(c, "Bad URL", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Log.d("DINING_ERROR", "Hour Menu IO error");
        }
        return sb.toString();
    }

    public String getHoursStatus(String eatery, String json) {
        try {
            JSONObject j = new JSONObject(json);
            JSONArray results = j.getJSONArray("results");
            for (int i = 0; i < results.length(); i++) {
                JSONObject curResults = results.getJSONObject(i);

                String openTime = fixTime(curResults.get("open_hour").toString()) + ":" + fixTime(curResults.get("open_minute").toString());
                String closeTime = fixTime(curResults.get("close_hour").toString()) + ":" + fixTime(curResults.get("close_minute").toString());
                int timeRelation = isNowBetweenDateTime(dateFromHourMin(openTime), dateFromHourMin(closeTime));
                if (timeRelation == 0) {
                    return "Open today until " + to12HourTime(closeTime);
                } else if (timeRelation == -1) {
                    return "Closed. Opens today at " + to12HourTime(openTime);
                }
            }

        } catch (JSONException e) {
        }
        return checkFuture(eatery);
    }

    public String checkFuture(String eatery) {
        Date date = new Date();
        JSONObject future = null;
        boolean foundError;
        int numResults;
        int daysAhead = 1;
        do {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            cal.add(Calendar.DATE, 1);
            date = cal.getTime();
            String request = "https://api.students.brown.edu/dining/hours?client_id=" + clientId + "&eatery="
                    + eatery + "&month=" + Integer.toString(cal.get(Calendar.MONTH) + 1) + "&day=" + cal.get(Calendar.DAY_OF_MONTH);
            //foundError = false;
            try {
                future = new JSONObject(GET(request));
                numResults = Integer.parseInt(future.get("num_results").toString());
            } catch (JSONException e) {
                //foundError = true;
                numResults = 0;
            }
            daysAhead++;
        } while (numResults < 1 && daysAhead < 5);
        try {
            if (future != null) {
                JSONObject futureResult = future.getJSONArray("results").getJSONObject(0);
                String closeTime = futureResult.get("close_hour").toString() + ":" + futureResult.get("open_minute").toString();
                String openTime = futureResult.get("open_hour").toString() + ":" + futureResult.get("open_minute").toString();
                String openMonth = futureResult.get("month").toString();
                String openDay = futureResult.get("day").toString();
                final GregorianCalendar gc = new GregorianCalendar();
                gc.set(Calendar.MONTH, Integer.parseInt(openMonth) - 1);
                gc.set(Calendar.DAY_OF_MONTH, Integer.parseInt(openDay));
                Date openDate = gc.getTime();
                String open;
                if ((openDate.getTime() - new Date().getTime()) > 86400000) {
                    open = openMonth + "/" + openDay;
                } else {
                    open = "tomorrow";
                }
                return new StringBuilder("Closed. Opens ").append(open).append(" at ").append(to12HourTime(openTime)).toString();
            }
            return "Could not get hours info.";
        } catch (JSONException e) {
            //Log.d("DINING_ERROR", "Future hours JSON creation error");
            return "Closed.";
        }
    }

    private String fixTime(String time) {
        if (time.length() == 1) {
            return "0" + time;
        } else {
            return time;
        }
    }

    private String to12HourTime(String time) {
        try {
            final SimpleDateFormat sdf = new SimpleDateFormat("H:mm");
            final Date dateObj = sdf.parse(time);
            return new SimpleDateFormat("K:mma").format(dateObj)
                    .toLowerCase();
        } catch (final ParseException e) {
            Log.d("DINING_ERROR", "Error parsing time");
        }
        return time;
    }

    private Date dateFromHourMin(final String hhmm) {
        if (hhmm.matches("^[0-2][0-9]:[0-5][0-9]$")) {
            final String[] hms = hhmm.split(":");
            final GregorianCalendar gc = new GregorianCalendar();
            gc.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hms[0]));
            gc.set(Calendar.MINUTE, Integer.parseInt(hms[1]));
            gc.set(Calendar.SECOND, 0);
            gc.set(Calendar.MILLISECOND, 0);
            return gc.getTime();
        } else {
            throw new IllegalArgumentException(hhmm
                    + " is not a valid time, expecting HH:MM format");
        }
    }

    static int isNowBetweenDateTime(final Date s, final Date e) {
        final Date now = new Date();
        if (now.before(s)) {
            return -1;
        } else if (now.after(e)) {
            return 1;
        } else {
            return 0;
        }
    }

    public String getOtherHours(String eatery) {
        Date open, close;
        String openTime, closeTime;
        Calendar cal = Calendar.getInstance();
        Calendar c;
        long now, passed, secondsPassed;
        switch (eatery) {
            case "Andrews Commons":
                openTime = "11:00";
                closeTime = "02:00";
                open = dateFromHourMin(openTime);
                close = dateFromHourMin(closeTime);
                c = Calendar.getInstance();
                now = c.getTimeInMillis();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                passed = now - c.getTimeInMillis();
                secondsPassed = passed / 1000;
                if (secondsPassed < 7200) {
                    cal.setTime(open);
                    cal.add(Calendar.DATE, -1);
                    open = cal.getTime();
                } else {
                    cal.setTime(close);
                    cal.add(Calendar.DATE, 1);
                    close = cal.getTime();
                }
                switch (isNowBetweenDateTime(open, close)) {
                    case 0:
                        return "Open until " + to12HourTime(closeTime);
                    case -1:
                        return "Closed. Opens at " + to12HourTime(openTime);
                    case 1:
                        return "Closed. Opens tomorrow at " + to12HourTime(openTime);
                }
                break;
            case "Blue Room":
                cal.setTime(new Date());
                int dow = cal.get(Calendar.DAY_OF_WEEK);
                if (dow >= Calendar.MONDAY && dow <= Calendar.FRIDAY) {
                    openTime = "07:30";
                    closeTime = "21:00";
                } else {
                    openTime = "09:00";
                    closeTime = "17:00";
                }
                open = dateFromHourMin(openTime);
                close = dateFromHourMin(closeTime);
                switch (isNowBetweenDateTime(open, close)) {
                    case 0:
                        return "Open until " + to12HourTime(closeTime);
                    case -1:
                        return "Closed. Opens at " + to12HourTime(openTime);
                    case 1:
                        if (dow == Calendar.FRIDAY) {
                            return "Closed. Opens tomorrow at 9:00am";
                        } else if (dow == Calendar.SUNDAY) {
                            return "Closed. Opens tomorrow at 7:30am";
                        } else {
                            return "Closed. Opens tomorrow at " + to12HourTime(openTime);
                        }
                }
                break;
            case "Josiah's":
                openTime = "18:00";
                closeTime = "02:00";
                open = dateFromHourMin(openTime);
                close = dateFromHourMin(closeTime);
                c = Calendar.getInstance();
                now = c.getTimeInMillis();
                c.set(Calendar.HOUR_OF_DAY, 0);
                c.set(Calendar.MINUTE, 0);
                c.set(Calendar.SECOND, 0);
                c.set(Calendar.MILLISECOND, 0);
                passed = now - c.getTimeInMillis();
                secondsPassed = passed / 1000;
                if (secondsPassed < 7200) {
                    cal.setTime(open);
                    cal.add(Calendar.DATE, -1);
                    open = cal.getTime();
                } else {
                    cal.setTime(close);
                    cal.add(Calendar.DATE, 1);
                    close = cal.getTime();
                }
                switch (isNowBetweenDateTime(open, close)) {
                    case 0:
                        return "Open until " + to12HourTime(closeTime);
                    case -1:
                        return "Closed. Opens at " + to12HourTime(openTime);
                    case 1:
                        return "Closed. Opens tomorrow at " + to12HourTime(openTime);
                }
                break;
        }
        return "Hours Info Unavailable";
    }
}