package com.github.browep.ratingspopup;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class RatingsPopup extends Activity {

    public static final String PACKAGE_NAME = "package_name";

    public static final String TO_EMAIL = "to_email";

    public static final String EMAIL_SUBJECT = "email_subject";

    private static final String NUM_TIMES_TRIED = "num_times_tried";

    public static final String DATE_FIRST_ACCESSED = "date_first_accessed";

    private static final String SHARED_PREF_FILE_NAME = "ratings_popup";

    public static final int DAY_MILLIS = 1000 * 60 * 60 * 24;

    private static final String EMAIL_TEXT = "email_text";

    public static final String HAS_BEEN_ASKED = "has_been_asked";

    private static String TAG = RatingsPopup.class.getCanonicalName();

    String packageName;

    private String toEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.rate_this_app);
        packageName = getIntent().getStringExtra(PACKAGE_NAME);
        if(TextUtils.isEmpty(packageName)){
            throw new RuntimeException(PACKAGE_NAME + " needs to be declared in intent extras");
        }

        toEmail = getIntent().getStringExtra(TO_EMAIL);
        if(TextUtils.isEmpty(toEmail)){
            throw new RuntimeException(TO_EMAIL + " needs to be declared in intent extras");
        }

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean(HAS_BEEN_ASKED, true).commit();

    }

    public void onLoveIt(View view) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
        // delay for 2s, give the market page sometime to load so that when the toast appears it has more weight
        (new Handler()).postDelayed(new Runnable() {
            public void run() {
                Toast.makeText(RatingsPopup.this, getString(R.string.ratings_popup_rate_toast), Toast.LENGTH_LONG).show();
            }
        }, 2000);
        finish();

    }

    public void onHateIt(View view) {
        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{toEmail});
        emailIntent.setType("plain/text");
        String subject = getIntent().getStringExtra(EMAIL_SUBJECT);
        if(TextUtils.isEmpty(subject)){
            subject = getString(R.string.ratings_popup_email_subject);
        }
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);

        String text = getIntent().getStringExtra(EMAIL_TEXT);
        if(TextUtils.isEmpty(text)){
            text = getString(R.string.ratings_popup_email_text);
        }
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);

        startActivity(Intent.createChooser(emailIntent, getString(R.string.ratings_popup_pick_email_application)));
        Toast.makeText(this, getString(R.string.ratings_popup_tell_us_what_you_think), Toast.LENGTH_LONG).show();
        finish();
    }

    public static boolean hasBeenAttemptedXTimes(Context context, int numTimes) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        long triedCount = sharedPreferences.getLong(NUM_TIMES_TRIED, 0);
        triedCount += 1;
        Log.d(TAG, "triedCount: " + triedCount);
        sharedPreferences.edit().putLong(NUM_TIMES_TRIED, triedCount).commit();
        return triedCount >= numTimes;
    }

    public static boolean hasBeenXDaysSinceInstall(Context context, int numDays) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        long firstTriedMillis = sharedPreferences.getLong(DATE_FIRST_ACCESSED, 0);
        if (firstTriedMillis == 0) {
            firstTriedMillis = System.currentTimeMillis();
            sharedPreferences.edit().putLong(DATE_FIRST_ACCESSED, firstTriedMillis).commit();
        }

        long millisSinceFirstTry = System.currentTimeMillis() - firstTriedMillis;
        int millisNeeded = numDays * DAY_MILLIS;

        Log.d(TAG, "millis since first try: " + millisSinceFirstTry+ ", millis needed: " + millisNeeded);
        return millisSinceFirstTry > millisNeeded;

    }

    public static boolean hasNeverBeenAsked(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(SHARED_PREF_FILE_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(HAS_BEEN_ASKED, false);
    }
}
