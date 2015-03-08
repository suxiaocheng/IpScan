package com.ctrl.music.ipscan;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private NetTool NetTool = new NetTool();
    private TextView TWCurrentIP;
    private String CSCurrentIP;
    private Button BTScan;
    private ProgressBar PBScanProgress;

    private EditText ETStartAddress;
    private EditText ETEndAddress;

    private int BTScanCount = 0x0;
    private int BTScanTotal = 0x0;

    private String StringStartAddress;
    private String StringEndAddress;

    private ArrayAdapter<String> APIPList;
    private ListView LVIPList;

    private Handler handler = new Handler();
    private ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ETStartAddress = (EditText) findViewById(R.id.editTextScanStartIP);
        ETEndAddress = (EditText) findViewById(R.id.editTextScanEndIP);

        TWCurrentIP = (TextView) findViewById(R.id.TextViewCurrentIP);
        CSCurrentIP = NetTool.getLocAddress();
        if (TWCurrentIP != null) {
            if ((CSCurrentIP != null) && (CSCurrentIP.compareTo("") != 0)) {
                TWCurrentIP.setText(CSCurrentIP);

                int csEnd = CSCurrentIP.lastIndexOf('.');
                String IpTmp = new String(CSCurrentIP.substring(0, csEnd));
                if (csEnd != -1) {
                    StringStartAddress = new String(String.format(IpTmp + ".%d", 0x0));
                    StringEndAddress = new String(String.format(IpTmp + ".%d", 0xff));

                    ETStartAddress.setText(StringStartAddress);
                    ETEndAddress.setText(StringEndAddress);
                } else {
                    StringStartAddress = "";
                    StringEndAddress = "";
                }
            } else {
                TWCurrentIP.setText("Can't Get IP address");
            }
        }

        BTScan = (Button) findViewById(R.id.buttonScanIP);

        // construct the list view adapter
        APIPList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1);

        LVIPList = (ListView) findViewById(R.id.listViewActivityIP);
        LVIPList.setAdapter(APIPList);

        PBScanProgress = (ProgressBar) findViewById(R.id.progressBarScanIP);
    }

    private void ScanThread(final String ip) {
        executorService.submit(new Runnable() {
            public void run() {
                boolean status;
                status = NetTool.ping(ip);
                if (status == true) {
                    handler.post(new Runnable() {
                        public void run() {
                            APIPList.add(ip);
                            LVIPList.setAdapter(APIPList);
                        }
                    });
                }
                BTScanCount++;
                Log.d(TAG, "BTScanCount:" + BTScanCount);

                handler.post(new Runnable() {
                    public void run() {
                        PBScanProgress.setProgress(BTScanCount * 100 / BTScanTotal);
                        if (BTScanCount == BTScanTotal) {
                            BTScan.setEnabled(true);
                            PBScanProgress.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

        });
    }

    public void ScanClick(View v) {
        int count;
        String IPTmp;
        int csEnd;
        int dAddStart, dAddEnd;

        APIPList.clear();

        StringStartAddress = ETStartAddress.getText().toString();
        StringEndAddress = ETEndAddress.getText().toString();

        if (NetTool.compareSameSubIP(StringStartAddress, StringEndAddress) == false) {
            LVIPList.setAdapter(APIPList);
            return;
        }

        dAddStart = NetTool.getIPNumber(StringStartAddress);
        dAddEnd = NetTool.getIPNumber(StringEndAddress);
        if((dAddStart == -1) || (dAddEnd == -1) || ((dAddEnd -dAddStart) < 0)){
            LVIPList.setAdapter(APIPList);
            return;
        }

        csEnd = CSCurrentIP.lastIndexOf('.');
        if (csEnd == -1) {
            LVIPList.setAdapter(APIPList);
            return;
        }
        IPTmp = new String(CSCurrentIP.substring(0, csEnd));

        BTScanCount = 0;
        BTScanTotal = dAddEnd - dAddStart + 1;
        BTScan.setEnabled(false);
        PBScanProgress.setVisibility(View.VISIBLE);

        for (count = 0; count < BTScanTotal; count++) {
            //Log.d(TAG, String.format(IPTmp + ".%d", count));
            ScanThread(String.format(IPTmp + ".%d", count+dAddStart));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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
        }
        return super.onOptionsItemSelected(item);
    }
}
