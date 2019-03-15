package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TelephonyManager telephonyManager;
    int dbm;
    public double latitude = 0.0;
    public double longitude = 0.0;
    TextView textView;
    Button mSubmit;
    SignalStrength signalStrength;
    private StringBuilder text = new StringBuilder();
    BufferedReader reader = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubmit = findViewById(R.id.create);
        textView = findViewById(R.id.textView);

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View mView) {

                try
                {
                    Criteria criteria= new  Criteria();
                    criteria.setAccuracy(Criteria.ACCURACY_FINE);

                    LocationManager lm = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
                    lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 5000, locationListener);

                    telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                        signalStrength = telephonyManager.getSignalStrength();
                    }
                    assert signalStrength != null;
                    String signalStrengthString = signalStrength.toString();

                    String[] parts = signalStrengthString.split(" ");


                    if ( telephonyManager.getNetworkType() == TelephonyManager.NETWORK_TYPE_LTE){

                        // For Lte SignalStrength: dbm = ASU - 140.
                        dbm = Integer.parseInt(parts[8])-140;

                    }
                    else{

                        // For GSM Signal Strength: dbm =  (2*ASU)-113.
                        if (signalStrength.getGsmSignalStrength() != 99) {
                            dbm = -113 + 2 * signalStrength.getGsmSignalStrength();

                        }
                    }

                    String baseDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
                    String fileName = "GSMStrength.csv";
                    String filePath = baseDir + File.separator + fileName;
                    File file = new File(filePath);

                    if (!file.exists()) {
                        try {
                            //noinspection ResultOfMethodCallIgnored
                            file.createNewFile();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    try {
                        FileWriter fileWriter  = new FileWriter(file);
                        BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
                        //noinspection StringConcatenationInsideStringBufferAppend
                        bufferedWriter.append(String.valueOf(longitude)+","+String.valueOf(latitude) + "," + dbm);
                        bufferedWriter.newLine();
                        bufferedWriter.close();

                        try {
                            FileReader fr = new FileReader(file);
                            reader = new BufferedReader(fr);

                            // do reading, usually loop until end of file reading
                            String mLine;
                            while ((mLine = reader.readLine()) != null) {
                                text.append(mLine);
                                text.append('\n');
                            }
                        } catch (IOException e) {
                            Toast.makeText(getApplicationContext(),"Error reading file!",Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) {
                                    //log the exception
                                }
                            }

                            FirebaseApp.initializeApp(MainActivity.this);
                            DatabaseReference mDatabase;
                            mDatabase = FirebaseDatabase.getInstance().getReference();

                            if(latitude!=0&&longitude!=0)
                            {
                                //Show the values in text field
                                textView.setText(text);

                                //Push to Server
                                Map<String, Number> dbValuesHash = new HashMap<>();
                                dbValuesHash.put("latitude",latitude);
                                dbValuesHash.put("longitude",longitude);
                                dbValuesHash.put("dbm",dbm);

                                mDatabase.push().setValue(dbValuesHash);
                            }

                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    }catch (Exception e){
                        Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_LONG).show();
                    }
                }


        });

    }

    // Define a listener that responds to location updates
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location
            // provider.
            longitude = location.getLongitude();
            latitude = location.getLatitude();

            Log.e("LONG", longitude+"");
            Log.e("LAT", latitude+"");
        }

        public void onStatusChanged(String provider, int status,
                                    Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };
}


