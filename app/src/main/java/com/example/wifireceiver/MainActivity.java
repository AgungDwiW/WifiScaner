package com.example.wifireceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.opencsv.CSVWriter;

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
    private List<String[]> results_all = new ArrayList<String[]>();
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private TextView console;
    private TextView coordinate;
    private int curLen;
    private String csv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String uuid = UUID.randomUUID().toString();
        csv = (Environment.getExternalStorageDirectory().getAbsolutePath() + "/datasetKoordinat"+uuid + ".csv"); // Here csv file name is MyCsvFile.csv
        buttonScan = findViewById(R.id.scanBtn);
        buttonCSV = findViewById(R.id.buttonCSV);
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scanWifi();
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
            CSVWriter writer = null;
            writer = new CSVWriter(new FileWriter(csv));
            writer.writeAll(results_all);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        console.setText(console.getText()+"exported all "+ results_all.size() + "data to "+ csv);
    }

    private void scanWifi() {
        if (isEmpty(coordinate.getText())){
            console.setText(console.getText()+"input coordinate! \n" );
            return;
        }

        while (results_all.size() <curLen+20) {
            console.setText(console.getText() + "\n scanning wifi; location:" + coordinate.getText() + "\n");
            arrayList.clear();
            registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            //wifiManager.startScan();
            //Toast.makeText(this, "Scanning WiFi ...", Toast.LENGTH_SHORT).show();
            results = wifiManager.getScanResults();
            ArrayList<String> temp = new ArrayList<>();
            temp.add(coordinate.getText() +"");
            for (ScanResult scanResult : results) {
                temp.add(scanResult.BSSID );
                temp.add(scanResult.level+"");
                arrayList.add(scanResult.BSSID + " - " + scanResult.level);
                adapter.notifyDataSetChanged();
            }
            String[] temp2 = new String[temp.size()];
            temp2 = temp.toArray(temp2);
            results_all.add(temp2);
            sleep(500);
        }
        wifiManager.startScan();
        console.setText(console.getText() + "scanning done; Current data: " + results_all.size() + "\n");
        curLen = results_all.size();
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
                arrayList.add(scanResult.BSSID + " - " + scanResult.level);
                adapter.notifyDataSetChanged();
            }
            //String[] temp2 = new String[temp.size()];
            //temp2 = temp.toArray(temp2);
            //results_all.add(temp2);
        };
    };
}