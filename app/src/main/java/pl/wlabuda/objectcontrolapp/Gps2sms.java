package pl.wlabuda.objectcontrolapp;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import java.util.ArrayList;

public class Gps2sms extends Activity implements
        SeekBar.OnSeekBarChangeListener {

    private SeekBar mSeekBar;
    private TextView mActual;
    private double prog;
    private String phnumber;
    private EditText number;
    private TextView dlugosc;
    private TextView szerokosc;
    private TextView wait;
    private TextView dist;
    private ToggleButton toggleButton;
    private Location A;
    private Location B;
    private double c = 0.0;
    private double d = 0.0;
    private double a = 0.0;
    private double b = 0.0;
    private boolean wlaczone = false;
    private double distance = 0.0;
    private boolean doubleBackToExitPressedOnce = false;
    final Context context = this;

    private LocationManager lm;

    private LocationListener locationListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);

        mSeekBar = (SeekBar) findViewById(R.id.seekBar1);
        mSeekBar.setOnSeekBarChangeListener(this);
        mActual = (TextView) findViewById(R.id.actual);
        dlugosc = (TextView) findViewById(R.id.dlugosc);
        szerokosc = (TextView) findViewById(R.id.szerokosc);
        number = (EditText) findViewById(R.id.number);
        toggleButton = (ToggleButton) findViewById(R.id.toggleButton);
        dist = (TextView) findViewById(R.id.dist);
        wait = (TextView) findViewById(R.id.wait);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.noGSM))
                    .setCancelable(false)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                            mSeekBar.setEnabled(false);
                            number.setEnabled(false);
                            toggleButton.setText(getString(R.string.cantUse));
                            number.clearFocus();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        }else if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ){
            buildAlertMessageNoGps();
        }

        wait.setText(getString(R.string.waiting));
        Toast.makeText(Gps2sms.this, getString(R.string.startApp), Toast.LENGTH_LONG).show();

        // ---używamy klasy LocationManager w celu odczytu lokacji GPS---

        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        locationListener = new MyLocationListener();

        lm.requestLocationUpdates(

                LocationManager.GPS_PROVIDER,

                0,

                0,

                locationListener);

        A = new Location("A");
        B = new Location("B");
        A = lm.getLastKnownLocation(lm.GPS_PROVIDER);
        B = lm.getLastKnownLocation(lm.GPS_PROVIDER);

        toggleButton.setEnabled(false);
        dist.setVisibility(View.GONE);

        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (toggleButton.isChecked()) {
                    if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
                        buildAlertMessageNoGps();
                    }
                    phnumber = number.getText().toString();
                    A.setLongitude(d);
                    A.setLatitude(c);
                    if(phnumber.equals("")){
                        Toast.makeText(getBaseContext(), getString(R.string.noPhnumber),
                                Toast.LENGTH_SHORT).show();
                        toggleButton.setChecked(false);
                    }else if(phnumber.length() != 9 || phnumber.contains(" ")){
                        Toast.makeText(getBaseContext(), getString(R.string.badPhnumber),
                                Toast.LENGTH_SHORT).show();
                        toggleButton.setChecked(false);
                    }else {
                        Toast.makeText(Gps2sms.this, "ON", Toast.LENGTH_SHORT).show();
                        dist.setVisibility(View.VISIBLE);
                        mSeekBar.setEnabled(false);
                        number.setEnabled(false);
                        wlaczone = true;
                    }
                } else {
                    Toast.makeText(Gps2sms.this, "OFF", Toast.LENGTH_SHORT).show();
                    mSeekBar.setEnabled(true);
                    number.setEnabled(true);
                    dist.setVisibility(View.GONE);
                    wlaczone = false;
                }
            }
        });

    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.gpsDialog))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                        mSeekBar.setEnabled(false);
                        number.setEnabled(false);
                        toggleButton.setText(getString(R.string.cantUse));
                        number.clearFocus();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, 0, 0, getString(R.string.about));
        menu.add(0, 1, 0, getString(R.string.finish));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(context,R.style.AlertDialogCustom);
                builder
                        .setTitle(R.string.about)
                        .setMessage("Wacław Łabuda \ne-mail: waclab1807@gmail.com \nPolska/Nowy Sącz")
                        .setIcon(R.drawable.author)
                        .setPositiveButton("OK", null)
                        .show();
                break;
            case 1:
                System.exit(0);
                break;
            default:
                break;
        }
        return false;
    }

    public class MyLocationListener implements LocationListener {

        public void onLocationChanged(Location loc) {
            if (loc.getLongitude()!= 0.0 && loc.getLatitude() != 0.0) {
                toggleButton.setEnabled(true);
                wait.setVisibility(View.GONE);

                dlugosc.setText("" + loc.getLatitude());
                szerokosc.setText("" + loc.getLongitude());

                c = loc.getLatitude();
                d = loc.getLongitude();

                if (wlaczone) {
                    prog = mSeekBar.getProgress();

                    B.setLatitude(c);
                    B.setLongitude(d);

                    distance = A.distanceTo(B);

                    dist.setText("" + distance);

                    if (distance > prog && wlaczone) {
                        a = loc.getLongitude();
                        b = loc.getLatitude();

                        String text = getString(R.string.sms) + a + "\n" + b + "\n" + getString(R.string.sms2);

                        sendSMS(phnumber, text);

                        wlaczone = false;
                    }
                }
            }
        }

        public void onProviderDisabled(String provider) {

            // TODO Auto-generated method stub
        }

        public void onProviderEnabled(String provider) {

            // TODO Auto-generated method stub
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {

            // TODO Auto-generated method stub
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        SmsManager sms = SmsManager.getDefault();

        ArrayList<String> msgArray = sms.divideMessage(message);

        Toast.makeText(
                getBaseContext(),
                getString(R.string.alert)
                        + phoneNumber, Toast.LENGTH_LONG).show();

        sms.sendMultipartTextMessage(phoneNumber, null, msgArray, null, null);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int actual, boolean fromUser) {
        mActual.setText(actual + "m");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, getString(R.string.backButton), Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce = false;
            }
        }, 1000);
    }

}