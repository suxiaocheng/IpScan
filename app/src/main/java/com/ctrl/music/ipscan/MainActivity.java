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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private static final int PROGRESS_STATE_IDLE = 0X0;
    private static final int PROGRESS_STATE_SCAN = 0X1;

    private NetTool NTMyTool = new NetTool();
    private TextView TWCurrentIP;
    private String CSCurrentIP;
    private Button BTScan;
    private ProgressBar PBScanProgress;

    private EditText ETStartAddress;
    private EditText ETEndAddress;

    private int BTScanCount = 0x0;
    private int BTScanTotal = 0x0;

    private int iProgressState = PROGRESS_STATE_IDLE;

    private String StringStartAddress;
    private String StringEndAddress;

    private ArrayAdapter<String> APIPList;
    private ListView LVIPList;

    private Handler handler = new Handler();
    private ExecutorService executorService = Executors.newFixedThreadPool(5);
    private ArrayList<Future> ListFTaskStatus = new ArrayList<Future>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ETStartAddress = (EditText) findViewById(R.id.editTextScanStartIP);
        ETEndAddress = (EditText) findViewById(R.id.editTextScanEndIP);

        TWCurrentIP = (TextView) findViewById(R.id.TextViewCurrentIP);
        CSCurrentIP = NTMyTool.getLocAddress();
        if (TWCurrentIP != null) {
            if ((CSCurrentIP != null) && (CSCurrentIP.compareTo("") != 0)) {
                TWCurrentIP.setText(CSCurrentIP);

                int csEnd = CSCurrentIP.lastIndexOf('.');
                String IpTmp = CSCurrentIP.substring(0, csEnd);
                if (csEnd != -1) {
                    StringStartAddress = String.format(IpTmp + ".%d", 0x0);
                    StringEndAddress = String.format(IpTmp + ".%d", 0xff);

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
        Future futureTmp;
        futureTmp = executorService.submit(new Runnable() {
            public void run() {
                boolean status;
                status = NTMyTool.ping(ip);
                if (status) {
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
                            setCtrlState(true);
                            ListFTaskStatus.clear();
                        }
                    }
                });
            }

        });

        ListFTaskStatus.add(futureTmp);
    }

    public void setCtrlState(boolean enable){
        //BTScan.setEnabled(enable);
        BTScan.setText((!enable)?"Cancel":"Scan");
        PBScanProgress.setVisibility((enable) ? View.INVISIBLE : View.VISIBLE);
        ETStartAddress.setEnabled(enable);
        ETEndAddress.setEnabled(enable);

        iProgressState = enable?PROGRESS_STATE_IDLE:PROGRESS_STATE_SCAN;
    }

    public void cancelExecPing(){
        Future futureTmp;
        for(Iterator<Future> iterator = ListFTaskStatus.iterator();iterator.hasNext();) {
            futureTmp = iterator.next();
            if(!futureTmp.isDone()){
                futureTmp.cancel(true);
            }
        }
        /* Check for all task and dump status */
        int count = 0;
        for(Iterator<Future> iterator = ListFTaskStatus.iterator();iterator.hasNext();) {
            futureTmp = iterator.next();
            count++;
            if((!futureTmp.isDone()) && (!futureTmp.isCancelled())){
                Log.d(TAG, "Task:" + count + "is not quit normally");
            }
        }
        ListFTaskStatus.clear();
    }

    public void ScanClick(View v) {
        int count;
        String IPTmp;
        int csEnd;
        int dAddStart, dAddEnd;

        if(iProgressState == PROGRESS_STATE_IDLE) {
            APIPList.clear();

            StringStartAddress = ETStartAddress.getText().toString();
            StringEndAddress = ETEndAddress.getText().toString();

            if (!NetTool.compareSameSubIP(StringStartAddress, StringEndAddress)) {
                LVIPList.setAdapter(APIPList);
                return;
            }

            dAddStart = NetTool.getIPNumber(StringStartAddress);
            dAddEnd = NetTool.getIPNumber(StringEndAddress);
            if ((dAddStart == -1) || (dAddEnd == -1) || ((dAddEnd - dAddStart) < 0)) {
                LVIPList.setAdapter(APIPList);
                return;
            }

            csEnd = CSCurrentIP.lastIndexOf('.');
            if (csEnd == -1) {
                LVIPList.setAdapter(APIPList);
                return;
            }
            IPTmp = CSCurrentIP.substring(0, csEnd);

            BTScanCount = 0;
            BTScanTotal = dAddEnd - dAddStart + 1;
            setCtrlState(false);

            for (count = 0; count < BTScanTotal; count++) {
                //Log.d(TAG, String.format(IPTmp + ".%d", count));
                ScanThread(String.format(IPTmp + ".%d", count + dAddStart));
            }
        }else if(iProgressState == PROGRESS_STATE_SCAN){
            cancelExecPing();
            setCtrlState(true);
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

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
        cancelExecPing();
        setCtrlState(true);
    }
}
