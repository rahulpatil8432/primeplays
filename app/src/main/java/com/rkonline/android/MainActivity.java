package com.rkonline.android;

import static com.rkonline.android.utils.CommonUtils.getCurrentISTMillis;
import static com.rkonline.android.utils.CommonUtils.getTimeInISTMillis;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.rkonline.android.notification.notification;
import com.rkonline.android.timetable.TimeTableActivity;
import com.rkonline.android.utils.AlertHelper;
import com.rkonline.android.utils.WithdrawUtils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    protected ScrollView scrollView;
    protected TextView balance;
    protected latonormal hometext;
    protected CardView exit;
    protected CardView logout;
    protected CardView refresh;
    protected TextView supportno, call;
    protected CardView support, cardCall, cardAddMoney, cardWithdraw;
    RecyclerView recyclerview;
    RecyclerView recyclerviewMarket;
    SharedPreferences preferences;
    ImageView notificationImage;
    FirebaseFirestore db;
    SwipeRefreshLayout swipeRefresh;

    String url,phone;
    LinearLayout walletPill;


    private static final int NOTIFICATION_PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.setContentView(R.layout.activity_main);
        AppUpdateManager.checkForUpdate(this);
        db = FirebaseFirestore.getInstance();
        notificationImage =   findViewById(R.id.notification);

        notificationImage.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, notification.class));
        });


        initViews();
        support.setOnClickListener(v -> {

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intent.setPackage("com.whatsapp");
                startActivity(intent);
            } catch (Exception e) {
                AlertHelper.showCustomAlert(
                        this,
                        "Sorry!",
                        "WhatsApp is not installed on your device.",
                        0,
                        0
                );
            }
        });

        exit.setOnClickListener(v -> {
            showExitDialog();
        });

        logout.setOnClickListener(v -> {
            onLogoutClick();
        });

        refresh.setOnClickListener(v -> {
            Toast.makeText(MainActivity.this, "Refreshing...", Toast.LENGTH_SHORT).show();
            apicall();
        });

        preferences = getSharedPreferences(constant.prefs, MODE_PRIVATE);
        apicall();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkNotificationPermission();
        }
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
        hometext.setSelected(true);
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showExitDialog();
            }
        });
        hometext.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    hometext.setSelected(false);
                    break;

                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    hometext.setSelected(true);
                    break;
            }
            return true;
        });


        Typeface face = Typeface.createFromAsset(getAssets(), "Oxygen-Bold.ttf");

        PrimaryDrawerItem home = new PrimaryDrawerItem().withName("Home").withIcon(R.drawable.house).withIdentifier(999).withTypeface(face);
        PrimaryDrawerItem account = new PrimaryDrawerItem().withName("My Profile").withIcon(R.drawable.user_icon).withIdentifier(1).withTypeface(face);
        PrimaryDrawerItem charts = new PrimaryDrawerItem().withName("Charts").withIdentifier(101).withIcon(R.drawable.chart_icon).withTypeface(face);
        PrimaryDrawerItem rate = new PrimaryDrawerItem().withName("Game Rate").withIdentifier(2).withIcon(R.drawable.rupee_icon).withTypeface(face);
        PrimaryDrawerItem earn = new PrimaryDrawerItem().withName("Refer and Earn").withIcon(R.drawable.refer_icon).withIdentifier(21).withTypeface(face);
        PrimaryDrawerItem notice = new PrimaryDrawerItem().withName("Notice").withIcon(R.drawable.info_icon).withIdentifier(3).withTypeface(face);
        PrimaryDrawerItem deposit = new PrimaryDrawerItem().withName("Deposit").withIcon(R.drawable.deposit).withIdentifier(4).withTypeface(face);
        PrimaryDrawerItem withdraw = new PrimaryDrawerItem().withName("Withdrawal").withIcon(R.drawable.withdraw).withIdentifier(41).withTypeface(face);
        PrimaryDrawerItem ledger = new PrimaryDrawerItem().withName("Game Ledger").withIcon(R.drawable.two_arraw).withIdentifier(6).withTypeface(face);
        PrimaryDrawerItem transaction = new PrimaryDrawerItem().withName("Passbook").withIcon(R.drawable.wallet_icon).withIdentifier(8).withTypeface(face);
        PrimaryDrawerItem played = new PrimaryDrawerItem().withName("Game History").withIcon(R.drawable.play_icon).withIdentifier(9).withTypeface(face);
        PrimaryDrawerItem howto = new PrimaryDrawerItem().withName("How to Play").withIcon(R.drawable.question).withIdentifier(10).withTypeface(face);
        PrimaryDrawerItem share = new PrimaryDrawerItem().withName("Share").withIcon(R.drawable.share_icon).withIdentifier(11).withTypeface(face);
        PrimaryDrawerItem logout = new PrimaryDrawerItem().withName("Logout").withIcon(R.drawable.logout_icon).withIdentifier(7).withTypeface(face);
        PrimaryDrawerItem timetable = new PrimaryDrawerItem().withName("Time Table").withIcon(R.drawable.time_table).withIdentifier(32).withTypeface(face);


        final Drawer drawer = new DrawerBuilder()
                .withHeaderDivider(true)
                .withActivity(this)
                .withSliderBackgroundColor(getResources().getColor(android.R.color.white))
                .withTranslucentStatusBar(true)
                .withHeader(R.layout.header)
                .withActionBarDrawerToggle(false)
                .addDrawerItems(
                        home, played, transaction, charts, timetable, rate, deposit, withdraw, account, notice,  howto,earn, share, logout
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
                            WithdrawUtils.getTeamsAndConditions(db, preferences, () -> {
                                if (WithdrawUtils.isWithdrawAllowed(preferences)) {
                                    String info = "Withdrawals are allowed only between "
                                            + preferences.getInt("withdrawStartHour", 11) + ":00 and "
                                            + preferences.getInt("withdrawEndHour", 23) + ":00. Please try again during this time.";
                                    AlertHelper.showCustomAlert(MainActivity.this, "Info", info, R.drawable.info_icon, R.color.colorAccent);
                                    return;
                                }
                                Intent intent = new Intent(MainActivity.this, withdraw_money.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            });
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
                           onLogoutClick();
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
                        if (drawerItem.equals(32)) {
                            startActivity(new Intent(MainActivity.this, TimeTableActivity.class));
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
        swipeRefresh.setOnRefreshListener(() -> apicall());

        cardAddMoney.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, deposit_money.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        cardWithdraw.setOnClickListener(v  -> {
            WithdrawUtils.getTeamsAndConditions(db, preferences, () -> {
                if (WithdrawUtils.isWithdrawAllowed(preferences)) {
                    String info = "Withdrawals are allowed only between "
                            + preferences.getInt("withdrawStartHour", 11) + ":00 and "
                            + preferences.getInt("withdrawEndHour", 23) + ":00. Please try again during this time.";
                    AlertHelper.showCustomAlert(MainActivity.this, "Info", info, R.drawable.info_icon, R.color.colorAccent);
                    return;
                }

                Intent intent = new Intent(MainActivity.this, withdraw_money.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            });
        });
        cardCall.setOnClickListener(v -> {
            String uri = "tel:"+phone;
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse(uri));
            startActivity(intent);
        });
    }

    private void showExitDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Exit App")
                .setMessage("Do you want to exit?")
                .setPositiveButton("Yes", (dialog, which) ->    finishAffinity())
                .setNegativeButton("No", null)
                .show();
    }

    private void onLogoutClick(){
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(Objects.requireNonNull(preferences.getString("mobile", null)))
                .update("fcmToken", FieldValue.delete());
        preferences.edit().clear().apply();
        Intent in = new Intent(getApplicationContext(), login.class);
        in.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        in.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(in);
        finish();
    }
    private void checkNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.POST_NOTIFICATIONS},
                    NOTIFICATION_PERMISSION_CODE
            );

        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == NOTIFICATION_PERMISSION_CODE) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show();

            } else {
                // Permission denied
                // You can show a message or disable notification feature
                Toast.makeText(this, "Notification Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
    private void apicall() {
        String mobile = preferences.getString("mobile", null);
        DocumentReference docRef  = db.collection("users").document(mobile);

        docRef.addSnapshotListener((value, error) -> {
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

                    onLogoutClick();
                }
                if (!jsonObject1.optString("session").equals(getSharedPreferences(constant.prefs, MODE_PRIVATE).getString("session", null))) {
                    Toast.makeText(MainActivity.this, "Session expired ! Please login again", Toast.LENGTH_SHORT).show();

                    onLogoutClick();
                }
                balance.setText(jsonObject1.optString("wallet"));

                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("wallet", jsonObject1.optString("wallet")).apply();
                editor.putString("upi", jsonObject1.optString("upi")).apply();
                editor.putString("accountNo", jsonObject1.optString("accountNo")).apply();
                editor.putString("IFSCCode", jsonObject1.optString("IFSCCode")).apply();
            } else {
                Log.d("Firestore", "Document does not exist.");
            }
        });
        loadHomeLine();
        loadMarkets();
        openWhatsApp();
    }

    private void loadMarkets() {

        final int MARKET_CLOSED = 0;
        final int MARKET_OPEN = 1;
        final int MARKET_YET_TO_OPEN = 2;
        final int MARKET_CLOSE_TODAY = 3;

        db.collection("markets")
                .orderBy("open_time")
                .get()
                .addOnSuccessListener(query -> {
                    swipeRefresh.setRefreshing(false);
                    ArrayList<String> names = new ArrayList<>();
                    ArrayList<String> openTimeArray = new ArrayList<>();
                    ArrayList<String> closeTimeArray = new ArrayList<>();
                    ArrayList<Integer> marketStatus = new ArrayList<>();
                    ArrayList<String> marketResults = new ArrayList<>();
                    ArrayList<Boolean> closeNextDayArray = new ArrayList<>();
                    for (DocumentSnapshot doc : query) {
                        String marketName = doc.getString("market_name");
                        String openTime = doc.getString("open_time_formatted");
                        String closeTime = doc.getString("close_time_formatted");
                        String aankdo_open = Objects.requireNonNull(doc.getString("aankdo_open")).isEmpty() ? "***" : doc.getString("aankdo_open");
                        String aankdo_close = Objects.requireNonNull(doc.getString("aankdo_close")).isEmpty() ? "***" : doc.getString("aankdo_close");
                        String figure_open = Objects.requireNonNull(doc.getString("figure_open")).isEmpty() ? "*" : doc.getString("figure_open");
                        String figure_close = Objects.requireNonNull(doc.getString("figure_close")).isEmpty() ? "*" : doc.getString("figure_close");
                        boolean isApproved = Boolean.TRUE.equals(doc.getBoolean("is_approved"));
                        String statusStr = doc.getString("status");
                        Boolean closeNextDay = doc.getBoolean("close_next_day");

                        if (!isApproved || marketName == null || statusStr == null) continue;

                        int status;
                        String displayResult;

                        if ("market_close_today".equalsIgnoreCase(statusStr)) {
                            status = MARKET_CLOSE_TODAY;
                            displayResult = "***-**-***";
                            openTime = "00:00";
                            closeTime = "00:00";
                        }else{
                            long now = getCurrentISTMillis();
                            long openMillis = getTimeInISTMillis(openTime);
                            long closeMillis = getTimeInISTMillis(closeTime);
                            if (Boolean.TRUE.equals(closeNextDay) && closeMillis <= openMillis) {
                                closeMillis += 24 * 60 * 60 * 1000; // push close to next day
                            }

                            if (now < openMillis) {
                                status = MARKET_YET_TO_OPEN;
                                displayResult = "***-**-***";
                            }
                            else if (now >= openMillis && now <= closeMillis) {
                                status = MARKET_OPEN;
                                displayResult = aankdo_open + "-" + figure_open + "*-***";
                            }
                            else {
                                status = MARKET_CLOSED;
                                displayResult =
                                        aankdo_open + "-" +
                                                figure_open + figure_close + "-" +
                                                aankdo_close;
                            }
                        }

                        names.add(marketName);
                        openTimeArray.add(openTime);
                        closeTimeArray.add(closeTime);
                        marketStatus.add(status);
                        marketResults.add(displayResult);
                        closeNextDayArray.add(closeNextDay);
                    }

                    adapter_market rc =
                            new adapter_market(MainActivity.this, names, openTimeArray, closeTimeArray, marketStatus, marketResults, closeNextDayArray);

                    recyclerviewMarket.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                    recyclerviewMarket.setAdapter(rc);
                    Log.e("sad",names.toString());
                })
                .addOnFailureListener(e ->{

                            swipeRefresh.setRefreshing(false);
                    AlertHelper.showCustomAlert(MainActivity.this, "Something went wrong" , "Please try again", 0,0);
                        });

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
        db.collection("app_config")
                .document("whatsapp")
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {
                        AlertHelper.showCustomAlert(
                                this,
                                "Sorry!",
                                "Support service is temporarily unavailable.",
                                0,
                                0
                        );
                        return;
                    }

                    phone = documentSnapshot.getString("phone");
                    assert phone != null;
                    if (!phone.startsWith("+91")) {
                        phone = "+91" + phone;
                    }
                    String message = documentSnapshot.getString("message");
                    call.setText(phone);
                    supportno.setText(phone);

                    if (TextUtils.isEmpty(phone) || TextUtils.isEmpty(message)) {
                        AlertHelper.showCustomAlert(
                                this,
                                "Sorry!",
                                "Support service is temporarily unavailable.",
                                0,
                                0
                        );
                        return;
                    }

                    url = "https://wa.me/" + phone +
                            "?text=" + Uri.encode(message);
                })
                .addOnFailureListener(e ->
                        AlertHelper.showCustomAlert(
                                this,
                                "Sorry!",
                                "Support service is temporarily unavailable.",
                                0,
                                0
                        )
                );
    }

    private void initViews() {
        balance = findViewById(R.id.balance);
        hometext = findViewById(R.id.hometext);
        exit = findViewById(R.id.exit);
        logout = findViewById(R.id.logout);
        refresh = findViewById(R.id.refresh);
        supportno = findViewById(R.id.whatsapp);
        support = findViewById(R.id.cardwhatsapp);
        scrollView = findViewById(R.id.scrollView);
        recyclerview = findViewById(R.id.recyclerview);
        recyclerviewMarket = findViewById(R.id.recyclerviewMarket);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        cardCall = findViewById(R.id.cardcall);
        cardAddMoney = findViewById(R.id.cardaddmoney);
        cardWithdraw = findViewById(R.id.cardwithdraw);
        call = findViewById(R.id.call);
        walletPill = findViewById(R.id.wallet_holder);

        walletPill.setOnClickListener(v->{
            startActivity(new Intent(MainActivity.this, deposit_money.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));

        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppUpdateManager.onActivityResult(this, requestCode);
    }

}