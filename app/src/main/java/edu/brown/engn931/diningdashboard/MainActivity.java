package edu.brown.engn931.diningdashboard;

import android.accounts.Account;
import android.app.PendingIntent;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Handler;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    Fragment fragment;
    //Drawer result;
    static HashMap<String, String> hoursMap = new HashMap<String, String>();
    static HashMap<String, String> capacityMap = new HashMap<String, String>();
    static HashMap<String, ArrayList<CountPoint>> countMap = new HashMap<String, ArrayList<CountPoint>>();
    static HashMap<String, Long> timestampMap = new HashMap<String, Long>();
    Context c;
    static boolean taskFinished = false;
    static boolean cardsLoaded;
    static boolean showClientsConnected = false;
    int curPosition;

    CustomTabsServiceConnection mServiceConnection;
    CustomTabsClient mClient;
    CustomTabsSession mCustomTabsSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the AccountHeader
        /*AccountHeader.Result headerResult = new AccountHeader()
                .withActivity(this)
                .withHeaderBackground(R.drawable.bk_drawer)
                .addProfiles(
                        new ProfileDrawerItem().withIcon(getResources().getDrawable(R.drawable.ic_brownlogo))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();*/

        /*AccountHeader headerResult = new AccountHeaderBuilder()
                .withActivity(this)
                .withHeaderBackground(R.drawable.bk_drawer)
                .addProfiles(
                        new ProfileDrawerItem().withIcon(getResources().getDrawable(R.drawable.ic_brownlogo))
                )
                .withOnAccountHeaderListener(new AccountHeader.OnAccountHeaderListener() {
                    @Override
                    public boolean onProfileChanged(View view, IProfile profile, boolean currentProfile) {
                        return false;
                    }
                })
                .build();*/

        /*Drawer.Result result = new Drawer()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Eateries"),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("My Account")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        displayView(position);
                        curPosition = position;
                    }
                })
                .build();*/

        /*result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Eateries"),
                        new DividerDrawerItem(),
                        new SecondaryDrawerItem().withName("My Account")
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(AdapterView<?> parent, View view, int position, long id, IDrawerItem drawerItem) {
                        // do something with the clicked item :D
                        displayView(position);
                        curPosition = position;
                        return true;
                    }
                })
                .build();*/

        if (savedInstanceState != null) {
            curPosition = savedInstanceState.getInt("curFrag", 0);
            displayView(curPosition);
            //result.setSelection(curPosition);
        } else {
            curPosition = 0;
            displayView(curPosition);
            //result.setSelection(curPosition);
        }
        c = this;
        /*if (getNetworkState(c) <= 0) {
            noConnection();
        } else {
            loadContent();
        }*/
    }

    @Override
    protected void onResume() {
        if (getNetworkState(c) <= 0) {
            noConnection();
        } else {
            loadContent();
        }
        super.onResume();
    }

    public void loadContent() {
        bindCustomTabsService("com.android.chrome");
        if (mClient == null) {
            bindCustomTabsService("com.chrome.beta");
        }
        if (mClient == null) {
            bindCustomTabsService("com.chrome.dev");
        }
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                new HoursTask(c).execute();
                new CapacityTask(c).execute();
                if (mClient != null) {
                    mClient.warmup(0);
                    CustomTabsSession session = getSession();
                    session.mayLaunchUrl(Uri.parse("https://get.cbord.com/brown"), null, null);
                    //session.mayLaunchUrl(Uri.parse("http://www.brown.edu/Student_Services/Food_Services/eateries/andrews.php"), null, null);
                    //session.mayLaunchUrl(Uri.parse("http://www.brown.edu/Student_Services/Food_Services/eateries/blueroom.php"), null, null);
                    //session.mayLaunchUrl(Uri.parse("http://www.brown.edu/Student_Services/Food_Services/eateries/josiahs.php"), null, null);
                }
            }
        }, 50);
    }

    public void noConnection() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(c);
        dialog.setTitle("No Network Detected");
        dialog.setMessage("You need a network connection to use this application.");
        dialog.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (getNetworkState(getApplicationContext()) <= 0) {
                    noConnection();
                } else {
                    loadContent();
                }
            }
        });
        dialog.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
            }
        });
        dialog.show();
    }

    /**
     * Diplaying fragment view for selected nav drawer list item
     */
    private void displayView(int position) {
        // update the main content by replacing fragments
        switch (position) {
            case 0:
                fragment = new HomeFragment();
                //result.closeDrawer();
                break;
            case 2:
                fragment = null;//new AccountFragment();
                //result.closeDrawer();
                break;
            default:
                break;
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.frame_container, fragment).commit();
        } else {
            // error in creating fragment
            Log.e("MainActivity", "Error in creating fragment");
        }
    }

    public void loadCustomTab(String url, String eatery) {
        CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession());
        builder.setToolbarColor(getResources().getColor(R.color.red_primary)).setShowTitle(true);
        builder.setStartAnimations(this, R.anim.slide_in_right, R.anim.slide_out_left);
        builder.setExitAnimations(this, R.anim.slide_in_left, R.anim.slide_out_right);
        builder.setCloseButtonIcon(
                BitmapFactory.decodeResource(getResources(), R.drawable.ic_arrow_back));
        if (eatery != null) {
            prepareActionButton(builder, eatery);
        }
        CustomTabsIntent customTabsIntent = builder.build();
        CustomTabsHelper.addKeepAliveExtra(this, customTabsIntent.intent);
        try {
            customTabsIntent.launchUrl(this, Uri.parse(url));
        } catch (ActivityNotFoundException e) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(this);
            dialog.setTitle("No Browser Installed");
            dialog.setMessage("You need to install web browser such as Google Chrome for this feature to work.");
            dialog.setPositiveButton("Get Chrome", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final String appPackageName = "com.chrome.beta";
                    try {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }
            });
            dialog.setNegativeButton("Cancel", null);
            dialog.show();
        }
    }

    private void prepareActionButton(CustomTabsIntent.Builder builder, String eatery) {
        // An example intent that sends an email.
        StringBuilder sb = new StringBuilder("Let's go to ");
        switch (eatery) {
            case "Andrews Commons":
                sb.append("Andrews Commons");
                break;
            case "Blue Room":
                sb.append("the Blue Room");
                break;
            case "Josiah's":
                sb.append("Jo's");
                break;
            default:
                sb.append("eat");
        }

        Intent actionIntent = new Intent();
        actionIntent.setAction(Intent.ACTION_SEND);
        actionIntent.putExtra(Intent.EXTRA_TEXT, sb.append(".").toString());
        actionIntent.setType("text/plain");
        PendingIntent pi = PendingIntent.getActivity(getApplicationContext(), 0, actionIntent, 0);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_share);
        builder.setActionButton(icon, "Share", pi);
    }

    private void bindCustomTabsService(String mPackageNameToBind) {
        if (mClient != null) {
            return;
        }
        mServiceConnection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName name, CustomTabsClient client) {
                mClient = client;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };
        boolean ok = CustomTabsClient.bindCustomTabsService(this, mPackageNameToBind, mServiceConnection);
        if (!ok) {
            mServiceConnection = null;
        }
    }

    public CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                }
            });
        }
        return mCustomTabsSession;
    }

    private void unbindCustomTabsService() {
        if (mServiceConnection == null) return;
        unbindService(mServiceConnection);
        mClient = null;
        mCustomTabsSession = null;
        mServiceConnection = null;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unbindCustomTabsService();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unbindCustomTabsService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("curFrag", curPosition);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_client_toggle) {
            if (showClientsConnected) {
                showClientsConnected = false;
            } else {
                showClientsConnected = true;
            }
            if (fragment instanceof HomeFragment) {
                ((HomeFragment) fragment).displayCards();
            }
            return true;
        } else if (id == R.id.action_account) {
            loadCustomTab("https://get.cbord.com/brown", null);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Checks the network state, returning an integer based on the type of connection
     *
     * @param c - the context
     * @return an int, 0 if not connected, 1 if on wifi, 2 if on mobile data, or -1 if indeterminate
     */
    public static int getNetworkState(Context c) {
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (activeNetwork == null || !activeNetwork.isConnectedOrConnecting()) {
            return 0;
        }
        int networkType = activeNetwork.getType();
        if (networkType == ConnectivityManager.TYPE_WIFI) {
            return 1;
        } else if (networkType == ConnectivityManager.TYPE_MOBILE) {
            return 2;
        } else {
            return -1;
        }
    }

    @Override
    public void onBackPressed() {
        if (fragment instanceof AccountFragment) {
            ((AccountFragment) fragment).webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
