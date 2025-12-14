package com.rkonline.android;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    protected ScrollView scrollView;
    protected TextView balance;
//    protected CardView single;
//    protected CardView jodi;
//    protected CardView singlepatti;
//    protected CardView doublepatti;
//    protected CardView tripepatti;
//    protected CardView halfsangam;
//    protected CardView fullsangam;
    protected CardView ghantab;
    protected latonormal hometext;
//    protected CardView crossing;
    protected CardView exit;
    protected CardView logout;
    protected CardView refresh;
    protected TextView supportno;
    protected CardView support;
    RecyclerView recyclerview;
    RecyclerView recyclerviewMarket;
    SharedPreferences preferences;
    Button lang_img;
    FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        db = FirebaseFirestore.getInstance();
        lang_img =(Button) findViewById(R.id.lang_switch);
        lang_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.openOptionsMenu();
            }
        });


        initViews();
        support.setOnClickListener(v -> openWhatsApp());

        exit.setOnClickListener(v -> {
            Process.killProcess(Process.myPid());
            System.exit(1);
        });

        logout.setOnClickListener(v -> {
            preferences.edit().clear().apply();
            Intent in = new Intent(getApplicationContext(), login.class);
            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(in);
            finish();
        });

        refresh.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
            apicall();
        });

        preferences = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        apicall();

        if (preferences.getString("wallet", null) != null) {
            balance.setText(preferences.getString("wallet", null));
        } else {
            balance.setText("Loading");
        }

        if (preferences.getString("homeline", null) != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                hometext.setText(Html.fromHtml(preferences.getString("homeline", null), Html.FROM_HTML_MODE_COMPACT));
            } else {
                hometext.setText(Html.fromHtml(preferences.getString("homeline", null)));
            }
        } else {
            hometext.setText("Loading...");
        }


//        single.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "single").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        jodi.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "jodi").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        crossing.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "crossing").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        singlepatti.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "singlepatti").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        doublepatti.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "doublepatti").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        tripepatti.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "tripepatti").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));

        /*ghantab =(CardView)findViewById(R.id.ghanta_bajar);
        ghantab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(MainActivity.this, GhantaListActivity.class);
                startActivity(i);
            }
        });*/

//        halfsangam.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "halfsangam").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
//
//        fullsangam.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, bazar.class).putExtra("game", "fullsangam").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));


        Typeface face = Typeface.createFromAsset(getAssets(), "Oxygen-Bold.ttf");


        PrimaryDrawerItem home = new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.house).withIdentifier(999).withTypeface(face);
        PrimaryDrawerItem account = new PrimaryDrawerItem().withName("My Profile").withIcon(R.drawable.user_icon).withIdentifier(1).withTypeface(face);
        PrimaryDrawerItem charts = new PrimaryDrawerItem().withName("Charts").withIdentifier(101).withIcon(R.drawable.chart_icon).withTypeface(face);
        PrimaryDrawerItem rate = new PrimaryDrawerItem().withName("Game Rate").withIdentifier(2).withIcon(R.drawable.rupee_icon).withTypeface(face);
        PrimaryDrawerItem earn = new PrimaryDrawerItem().withName("Refer and Earn").withIcon(R.drawable.refer_icon).withIdentifier(21).withTypeface(face);
        PrimaryDrawerItem notice = new PrimaryDrawerItem().withName("Notice").withIcon(R.drawable.info_icon).withIdentifier(3).withTypeface(face);
        PrimaryDrawerItem deposit = new PrimaryDrawerItem().withName("Deposit").withIcon(R.drawable.rupee_icon).withIdentifier(4).withTypeface(face);
        PrimaryDrawerItem withdraw = new PrimaryDrawerItem().withName("Withdrawal").withIcon(R.drawable.rupee_icon).withIdentifier(41).withTypeface(face);
        PrimaryDrawerItem ledger = new PrimaryDrawerItem().withName("Game Ledger").withIcon(R.drawable.two_arraw).withIdentifier(6).withTypeface(face);
        PrimaryDrawerItem transaction = new PrimaryDrawerItem().withName("Balance Enquiry").withIcon(R.drawable.wallet_icon).withIdentifier(8).withTypeface(face);
        PrimaryDrawerItem played = new PrimaryDrawerItem().withName("Played Match").withIcon(R.drawable.play_icon).withIdentifier(9).withTypeface(face);
        PrimaryDrawerItem howto = new PrimaryDrawerItem().withName("How to Play").withIcon(R.drawable.question).withIdentifier(10).withTypeface(face);
        PrimaryDrawerItem share = new PrimaryDrawerItem().withName("Share").withIcon(R.drawable.share_icon).withIdentifier(11).withTypeface(face);
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withName("Logout").withIcon(R.drawable.logout_icon).withIdentifier(7).withTypeface(face);


        final Drawer drawer = new DrawerBuilder()
                .withHeaderDivider(true)
                .withActivity(this)
                .withSliderBackgroundColor(getResources().getColor(android.R.color.white))
                .withTranslucentStatusBar(true)
                .withHeader(R.layout.header)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        home, played, charts, ledger, earn, account, rate, notice, deposit, withdraw, howto, transaction, share, logout
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        if (drawerItem.equals(1)) {
                            startActivity(new Intent(MainActivity.this, profile.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(101)) {
                            startActivity(new Intent(MainActivity.this, chart_menu.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(2)) {
                            startActivity(new Intent(MainActivity.this, rate.class).putExtra("header","Game Rate").setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(21)) {
                            startActivity(new Intent(MainActivity.this, earn.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(3)) {
                            startActivity(new Intent(MainActivity.this, notice.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(4)) {
                            startActivity(new Intent(MainActivity.this, deposit_money.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(41)) {
                            startActivity(new Intent(MainActivity.this, withdraw_money.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                        }
                        if (drawerItem.equals(10)) {
                            startActivity(new Intent(MainActivity.this, howto.class));
                        }
                        if (drawerItem.equals(11)) {

                            Intent sendIntent = new Intent();
                            sendIntent.setAction(Intent.ACTION_SEND);
                            sendIntent.putExtra(Intent.EXTRA_TEXT,
                                    "Download "+getString(R.string.app_name)+" and earn money at home, Download link - " + constant.link);
                            sendIntent.setType("text/plain");
                            startActivity(sendIntent);
                        }
                        if (drawerItem.equals(7)) {
                            preferences.edit().clear().apply();
                            Intent in = new Intent(getApplicationContext(), login.class);
                            in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(in);
                            finish();
                        }
                        if (drawerItem.equals(6)) {
                            startActivity(new Intent(MainActivity.this, ledger.class));
                        }
                        if (drawerItem.equals(8)) {
                            startActivity(new Intent(MainActivity.this, transactions.class));
                        }
                        if (drawerItem.equals(9)) {
                            startActivity(new Intent(MainActivity.this, played.class));
                        }

                        return false;
                    }
                })
                .build();


        findViewById(R.id.back).setOnClickListener(v -> {
            if (drawer.isDrawerOpen()) {
                drawer.closeDrawer();
            } else {
                drawer.openDrawer();
            }
        });

    }

    private void apicall() {
        String mobile = preferences.getString("mobile", null);
//        db.collection("users").document(mobile).get().addOnSuccessListener(documentSnapshot -> {
//            try {
//                Map<String, Object> userMap = documentSnapshot.getData();
//
//                JSONObject jsonObject1 = new JSONObject(userMap);
//                if (jsonObject1.optString("active").equals("0")) {
//                    Toast.makeText(MainActivity.this, "Your account temporarily disabled by admin", Toast.LENGTH_SHORT).show();
//
//                    preferences.edit().clear().apply();
//                    Intent in = new Intent(getApplicationContext(), login.class);
//                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(in);
//                    finish();
//                }
//                if (!jsonObject1.optString("session").equals(getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("session", null))) {
//                    Toast.makeText(MainActivity.this, "Session expired ! Please login again", Toast.LENGTH_SHORT).show();
//
//                    preferences.edit().clear().apply();
//                    Intent in = new Intent(getApplicationContext(), login.class);
//                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
//                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    startActivity(in);
//                    finish();
//                }
//                balance.setText(jsonObject1.optString("wallet"));
//
//                SharedPreferences.Editor editor = preferences.edit();
//                editor.putString("wallet", jsonObject1.optString("wallet")).apply();
//                }catch (Exception e){
//                e.printStackTrace();
//                Toast.makeText(MainActivity.this, "Something went wrong !", Toast.LENGTH_SHORT).show();
//            }
//        }).addOnFailureListener(documentSnapshot ->{
//            Toast.makeText(MainActivity.this, "Check your internet connection", Toast.LENGTH_SHORT).show();
//        });

        DocumentReference docRef  = db.collection("users").document(mobile);

        ListenerRegistration registration =  docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.w("Firestore", "Listen failed.", error);
                    return;
                }

                if (value != null && value.exists()) {
                    Log.d("Firestore", "Current data: " + value.getData());
                    Map<String, Object> userMap = value.getData();

                    assert userMap != null;
                    JSONObject jsonObject1 = new JSONObject(userMap);
                if (jsonObject1.optString("active").equals("0")) {
                    Toast.makeText(MainActivity.this, "Your account temporarily disabled by admin", Toast.LENGTH_SHORT).show();

                    preferences.edit().clear().apply();
                    Intent in = new Intent(getApplicationContext(), login.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(in);
                    finish();
                }
                if (!jsonObject1.optString("session").equals(getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("session", null))) {
                    Toast.makeText(MainActivity.this, "Session expired ! Please login again", Toast.LENGTH_SHORT).show();

                    preferences.edit().clear().apply();
                    Intent in = new Intent(getApplicationContext(), login.class);
                    in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(in);
                    finish();
                }
                balance.setText(jsonObject1.optString("wallet"));

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("wallet", jsonObject1.optString("wallet")).apply();
                } else {
                    Log.d("Firestore", "Document does not exist.");
                }
            }
        });
        loadResults();
        loadHomeLine();
        loadMarkets();

    }

    private void loadMarkets() {

        final int MARKET_CLOSED = 0;
        final int MARKET_OPEN = 1;
        final int MARKET_YET_TO_OPEN = 2;

        db.collection("markets")
                .get()
                .addOnSuccessListener(query -> {

                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> openTimeArray = new ArrayList<>();
                    ArrayList<String> closeTimeArray = new ArrayList<>();
                    ArrayList<Integer> marketStatus = new ArrayList<>();
                    ArrayList<String> marketResults = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        String marketName = doc.getString("market_name");
                        String openTime = doc.getString("open_time_formatted");
                        String closeTime = doc.getString("close_time_formatted");
                        String aankdo_open = doc.getString("aankdo_open");
                        String aankdo_close = doc.getString("aankdo_close");
                        String jodi = doc.getString("jodi");
                        String figure_open = doc.getString("figure_open");

                        Boolean openNow = doc.getBoolean("isOpenNow");
                        Boolean notOpen = doc.getBoolean("isNotOpened");

                        if (marketName == null) continue;

                        names.add(marketName);
                        openTimeArray.add(openTime);
                        closeTimeArray.add(closeTime);

                        int status;
                        if (Boolean.TRUE.equals(openNow)) {
                            status = MARKET_OPEN;
                            marketResults.add(aankdo_open+"-"+figure_open+"*-***");
                        } else if (Boolean.TRUE.equals(notOpen)) {
                            status = MARKET_YET_TO_OPEN;
                            marketResults.add("***-**-***");
                        } else {
                            status = MARKET_CLOSED;
                            marketResults.add(aankdo_open+"-"+jodi+"-"+aankdo_close);
                        }
                        marketStatus.add(status);
                    }

                    adapter_market rc =
                            new adapter_market(MainActivity.this, names, openTimeArray, closeTimeArray, marketStatus, marketResults);

                    recyclerviewMarket.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerviewMarket.setAdapter(rc);
                    Log.e("sad",names.toString());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Failed to load markets: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    private void loadResults() {
        db.collection("results")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(20)   // load latest 20 results
                .get()
                .addOnSuccessListener(resultSnap -> {

                    ArrayList<String> name = new ArrayList<>();
                    ArrayList<String> result = new ArrayList<>();

                    for (DocumentSnapshot doc : resultSnap) {
                        String market = doc.getString("market");
                        String res = doc.getString("result");

                        if (market != null && res != null) {
                            name.add(market);
                            result.add(res);
                        }
                    }

                    if (name.isEmpty()) {
                        name.add("No Data");
                        result.add("---");
                    }

                    adapter_result rc = new adapter_result(MainActivity.this, name, result);
                    recyclerview.setLayoutManager(new GridLayoutManager(MainActivity.this, 2));
                    recyclerview.setAdapter(rc);
                    rc.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(MainActivity.this, "Failed to load results", Toast.LENGTH_SHORT).show()
                );
    }
    private void loadHomeLine() {

        String username = preferences.getString("name", "User");

        db.collection("app_config").document("homeline")
                .get()
                .addOnSuccessListener(document -> {

                    if (document.exists()) {

                        String html = document.getString("content");

                        if (html == null || html.trim().isEmpty()) {
                            hometext.setText("Welcome to PrimePlays!");
                            return;
                        }
                        html = html.replace("{username}", username);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            hometext.setText(Html.fromHtml(html, Html.FROM_HTML_MODE_COMPACT));
                        } else {
                            hometext.setText(Html.fromHtml(html));
                        }

                    } else {
                        hometext.setText("Welcome to PrimePlays!");
                    }
                })
                .addOnFailureListener(e -> {
                    hometext.setText("Unable to load home message.");
                });
    }

    @Override
    protected void onResume() {
        apicall();
        super.onResume();
    }

    private void openWhatsApp() {
        try {
            String url = constant.whatsapplink;
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            i.setPackage("com.whatsapp");
            startActivity(i);
        } catch (Exception e) {
            Toast.makeText(this, "WhatsApp not installed", Toast.LENGTH_SHORT).show();
        }
    }

    private void initViews() {
        balance = findViewById(R.id.balance);
        hometext = findViewById(R.id.hometext);
//        single = findViewById(R.id.single);
//        jodi = findViewById(R.id.jodi);
//        crossing = findViewById(R.id.crossing);
//        singlepatti = findViewById(R.id.singlepatti);
//        doublepatti = findViewById(R.id.doublepatti);
//        tripepatti = findViewById(R.id.tripepatti);
//        halfsangam = findViewById(R.id.halfsangam);
//        fullsangam = findViewById(R.id.fullsangam);
        exit = findViewById(R.id.exit);
        logout = findViewById(R.id.logout);
        refresh = findViewById(R.id.refresh);
        supportno = findViewById(R.id.supportno);
        support = findViewById(R.id.support);
        scrollView = findViewById(R.id.scrollView);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerviewMarket = findViewById(R.id.recyclerviewMarket);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.lang_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_lang_en) {

            Locale locale = new Locale("en");
            Locale.setDefault(locale);
            Configuration config = new Configuration();
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            return true;

        } else if (id == R.id.menu_lang_hi) {

            Locale locale2 = new Locale("pt");
            Locale.setDefault(locale2);
            Configuration config2 = new Configuration();
            config2.locale = locale2;
            getBaseContext().getResources().updateConfiguration(config2, getBaseContext().getResources().getDisplayMetrics());

            return true;

        } else if (id == R.id.menu_lang_mr) {

            Locale locale3 = new Locale("es");
            Locale.setDefault(locale3);
            Configuration config3 = new Configuration();
            config3.locale = locale3;
            getBaseContext().getResources().updateConfiguration(config3, getBaseContext().getResources().getDisplayMetrics());

            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}