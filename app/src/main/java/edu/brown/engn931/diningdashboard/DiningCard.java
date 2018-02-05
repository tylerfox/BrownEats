package edu.brown.engn931.diningdashboard;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Paint;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.curioustechizen.ago.RelativeTimeTextView;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import it.gmariotti.cardslib.library.cards.material.MaterialLargeImageCard;

public class DiningCard extends MaterialLargeImageCard {

    private String mTitleHeader;
    private Context c;
    String hoursText;
    String capacityText;
    boolean isOpen = true;
    ArrayList<CountPoint> points;
    CountPoint curPoint;
    double m, b, meter;

    public DiningCard(Context c, int drawableId, String title) {
        super(c);
        this.c = c;
        this.mDrawableIdCardThumbnail = drawableId;
        setTextOverImage(title);
        this.mTitleHeader = title;
        this.hoursText = MainActivity.hoursMap.get(title);
        this.capacityText = MainActivity.capacityMap.get(title);
        points = MainActivity.countMap.get(mTitleHeader);
        if (points != null) {
            curPoint = points.get(0);
        }
        init();
        build();
    }

    @Override
    public void build() {
        if (this.mCardThumbnail == null) {
            this.mCardThumbnail = new DiningCardThumbnail(this.mContext);
            if (this.mExternalCardThumbnail != null) {
                this.mCardThumbnail.setExternalUsage(true);
                ((DiningCardThumbnail) this.mCardThumbnail).setExternalCardThumbnail(this.mExternalCardThumbnail);
            } else if (this.mDrawableIdCardThumbnail != 0) {
                this.mCardThumbnail.setDrawableResource(this.mDrawableIdCardThumbnail);
            } else if (this.mUrlCardThumbnail != null) {
                this.mCardThumbnail.setUrlResource(this.mUrlCardThumbnail);
            }

            this.addCardThumbnail(this.mCardThumbnail);
        }

        ((DiningCardThumbnail) this.mCardThumbnail).setTextOverImage(this.mTextOverImage);
        ((DiningCardThumbnail) this.mCardThumbnail).setTextOverImageResId(this.mTextOverImageResId);
    }

    private void init() {
        setTextOverImage(mTitleHeader);

        //Add ClickListener
        /*setOnClickListener(new Card.OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                viewInfo();
            }
        });*/
    }

    public double setMeter(int count) {
        double meter;
        switch (mTitleHeader) {
            case "Andrews Commons":
                m = .6256;
                b = 0;
                break;
            case "Blue Room":
                m = 0.6764;
                b = .0115;
                break;
            case "Josiah's":
                m = 0.7424;
                b = 0;
                break;
            case "V-Dub":
                m = .8223;
                b = 0;
                break;
            case "Ratty":
                m = .16;
                b = 0;
                break;
            default:
                m = 0;
                b = 0;
        }
        meter = m * (double) count + b;
        meter = (int) (Math.rint(meter / 10) * 10);
        if (meter < 0) {
            meter = 0;
        } else if (meter > 100) {
            meter = 100;
        }
        return meter;
    }

    @Override
    public void setupInnerViewElements(ViewGroup parent, View view) {

        ProgressBar bar = (ProgressBar) view.findViewById(R.id.bar);
        RelativeTimeTextView timestampTextview = (RelativeTimeTextView) view.findViewById(R.id.timestamp);
        TextView capacityTextView = (TextView) view.findViewById(R.id.capacityText);
        TextView hoursTextView = (TextView) view.findViewById(R.id.hours);
        TextView percentTextview = (TextView) view.findViewById(R.id.percent);
        TextView capacityLabel = (TextView) view.findViewById(R.id.capacityLabel);
        Button viewMenu = (Button) view.findViewById(R.id.viewMenu);
        Button viewHistory = (Button) view.findViewById(R.id.viewHistory);

        if (curPoint == null) {
            bar.setVisibility(View.GONE);
            percentTextview.setVisibility(View.GONE);
            timestampTextview.setVisibility(View.GONE);
            viewMenu.setVisibility(View.GONE);
            viewHistory.setVisibility(View.GONE);
            capacityLabel.setVisibility(View.VISIBLE);
            capacityLabel.setText("Could not retrieve capacity data");
            return;
        }

        meter = setMeter(curPoint.count);
        capacityText = "Clients Connected: " + curPoint.count;

        viewMenu.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewInfo();
            }
        });

        viewHistory.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                viewHistory();
            }
        });

        if (hoursText != null) {
            hoursTextView.setText(hoursText);
            if (!hoursText.startsWith("Closed")) {
                if (hoursText.startsWith("Could")) {
                    hoursTextView.setTextColor(c.getResources().getColor(R.color.md_grey_500));
                } else {
                    hoursTextView.setTextColor(c.getResources().getColor(R.color.md_green_600));
                }
                isOpen = true;
            } else {
                hoursTextView.setTextColor(c.getResources().getColor(R.color.red_primary));
                isOpen = false;
            }
        } else {
            hoursTextView.setText("Hours Info Unavailable");
        }
        if (isOpen) {
            bar.setProgress((int) meter);
            bar.setVisibility(View.VISIBLE);
            percentTextview.setVisibility(View.VISIBLE);
            timestampTextview.setVisibility(View.VISIBLE);
            capacityLabel.setVisibility(View.VISIBLE);
            viewMenu.setVisibility(View.VISIBLE);
            viewHistory.setVisibility(View.VISIBLE);
        } else {
            bar.setVisibility(View.GONE);
            percentTextview.setVisibility(View.GONE);
            timestampTextview.setVisibility(View.GONE);
            capacityLabel.setVisibility(View.GONE);
            viewMenu.setVisibility(View.GONE);
            viewHistory.setVisibility(View.GONE);
        }

        capacityTextView.setText(capacityText);

        if (MainActivity.showClientsConnected) {
            capacityTextView.setVisibility(View.VISIBLE);
        } else {
            capacityTextView.setVisibility(View.GONE);
        }

        percentTextview.setText((int) meter + "%");
        timestampTextview.setReferenceTime(curPoint.timeStamp);
    }

    public void viewInfo() {
        if (mTitleHeader.equals("Andrews Commons")) {
            ((MainActivity)c).loadCustomTab("http://www.brown.edu/Student_Services/Food_Services/eateries/andrews.php", mTitleHeader);
        } else if (mTitleHeader.equals("Blue Room")) {
            ((MainActivity)c).loadCustomTab("http://www.brown.edu/Student_Services/Food_Services/eateries/blueroom.php", mTitleHeader);
        } else if (mTitleHeader.equals("Josiah's")) {
            ((MainActivity)c).loadCustomTab("http://www.brown.edu/Student_Services/Food_Services/eateries/josiahs.php", mTitleHeader);
        } else {
            Intent intent = new Intent(c, DiningInfoActivity.class);
            intent.putExtra("edu.brown.engn931.diningdashboard.title", mTitleHeader);
            c.startActivity(intent);
        }
    }

    public void viewHistory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.historyDialogStyle);
        LayoutInflater inflater = ((MainActivity) c).getLayoutInflater();
        View historyView = inflater.inflate(R.layout.history_layout, null);
        builder.setView(historyView);
        builder.setTitle(mTitleHeader + " History");
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User cancelled the dialog
            }
        });
        AlertDialog dialog = builder.create();
        setupGraph(historyView);
        dialog.show();
    }

    public void setupGraph(View historyView) {

        GraphView graph = (GraphView) historyView.findViewById(R.id.historyChart);
        DataPoint[] data = new DataPoint[points.size()];

        Calendar c = Calendar.getInstance();
        String[] times = new String[points.size()];
        for (int i = 0; i < points.size(); i++) {
            CountPoint cur = points.get(points.size() - i - 1);
            c.setTime(new Date(cur.timeStamp));
            times[i] = to12HourTime(c.get(Calendar.HOUR_OF_DAY) + ":" + fixTime(Integer.toString(c.get(Calendar.MINUTE))));
            int capacity = (int) (Math.rint((cur.count * m + b) / 10)) * 10;
            if (i == points.size() - 1 && data[i - 1].getY() == capacity) {
                data[i] = (new DataPoint(i, (float) capacity + .01));
            } else {
                data[i] = (new DataPoint(i, (float) capacity));
            }
        }

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(data);
        series.setColor(getContext().getResources().getColor(R.color.md_light_blue_500));
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(7);
        series.setThickness(7);

        graph.getViewport().setMaxY(100);
        graph.getViewport().setMinY(0);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.addSeries(series);
        GridLabelRenderer gl = graph.getGridLabelRenderer();
        StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
        staticLabelsFormatter.setHorizontalLabels(times);
        staticLabelsFormatter.setVerticalLabels(new String[]{"0%", "20%", "40%", "60%", "80%", "100%"});
        gl.setLabelFormatter(staticLabelsFormatter);
        gl.setHorizontalLabelsColor(getContext().getResources().getColor(R.color.md_grey_600));
        gl.setVerticalLabelsColor(getContext().getResources().getColor(R.color.md_grey_600));
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
            return new SimpleDateFormat("K:mm").format(dateObj)
                    .toLowerCase();
        } catch (final ParseException e) {
            Log.d("DINING_ERROR", "Error parsing time");
        }
        return time;
    }
}
