package com.example.wifireceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static android.os.SystemClock.sleep;
import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {

    private WifiManager wifiManager;
    private ListView listView;
    private Button buttonScan;
    private Button buttonCSV;
    private int size = 0;
    private List<ScanResult> results;
    public List<String[]> results_all = new ArrayList<String[]>();
    public ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private TextView console;
    private TextView coordinate;
    private int curLen;
    private String csv;
    public int n;
    public boolean stop;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        n = 0;
        setContentView(R.layout.activity_main);


        buttonScan = findViewById(R.id.scanBtn);
        buttonCSV = findViewById(R.id.buttonCSV);
        stop = true;
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stop = !stop;
                if (!stop) {
                    buttonScan.setText("abort");
                    new scanWifi().execute();
                }
                else{
                    buttonScan.setText("scan wifi");
                }

            }
        });
        buttonCSV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                printCSV();
            }
        });
        curLen = 0;
        console = findViewById(R.id.console);
        coordinate = findViewById(R.id.Coordinate);
        listView = findViewById(R.id.wifiList);
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        console.setMovementMethod(new ScrollingMovementMethod());
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(this, "WiFi is disabled ... We need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, arrayList);
        listView.setAdapter(adapter);
    }

    private void printCSV(){
        try {
            String uuid = UUID.randomUUID().toString();
            csv = (getExternalFilesDir(null) + "/datasetKoordinat"+uuid + ".csv"); // Here csv file name is MyCsvFile.csv
            CSVWriter writer = null;
            writer = new CSVWriter(new FileWriter(csv));
            writer.writeAll(results_all);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        printConsole("exported all "+ results_all.size() + "data to "+ csv);
    }
    public void printConsole(String s){
        console.append(n+": "+ s +"\n");
        n+=1;
        final Layout layout = console.getLayout();
        if(layout != null){
            int scrollDelta = layout.getLineBottom(console.getLineCount() - 1)
                    - console.getScrollY() - console.getHeight();
            if(scrollDelta > 0)
                console.scrollBy(0, scrollDelta);
        }

    }

    private class scanWifi extends AsyncTask<String, Void, String> {
        // daemon class to get rssi and freq of certain wifi


        @Override
        protected String doInBackground(String... params) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    printConsole("started;");

                }
            });
            for (int x = 0; x<100; x++){
                if (stop){
                    break;
                }
                wifiManager.startScan();
                results = wifiManager.getScanResults();
                ArrayList<String> temp = new ArrayList<>();
                temp.add(coordinate.getText() +"");
                for (ScanResult scanResult : results) {
                    temp.add(scanResult.SSID);
                    temp.add(scanResult.BSSID );
                    temp.add(scanResult.level+"");
                    temp.add(scanResult.frequency+"");
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        printConsole("scanned data - " + results_all.size());
                        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
                        buttonScan.setText("scan wifi");
                    }
                });
                String[] temp2 = new String[temp.size()];
                temp2 = temp.toArray(temp2);
                results_all.add(temp2);
                sleep(100);
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            stop = !stop;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    printConsole("scanning done; Current data: " + results_all.size());


                }
            });
            // do something with result
        }
    }
    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);
            //ArrayList<String> temp = new ArrayList<>();
            //temp.add(coordinate.getText() +"");
            for (ScanResult scanResult : results) {
                //temp.add(scanResult.BSSID );
                //temp.add(scanResult.level+"");
                arrayList.add(scanResult.SSID + " - " +scanResult.BSSID + " - " + scanResult.level + " - " +scanResult.frequency);
                adapter.notifyDataSetChanged();
            }
            //String[] temp2 = new String[temp.size()];
            //temp2 = temp.toArray(temp2);
            //results_all.add(temp2);
        };
    };
}