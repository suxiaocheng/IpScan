package com.ctrl.music.ipscan;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {
    private static final String TAG = "MainActivity";

    private NetTool NetTool = new NetTool();
    private TextView TWCurrentIP;
    private String CSCurrentIP;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TWCurrentIP = (TextView)findViewById(R.id.TextViewCurrentIP);
        CSCurrentIP = NetTool.getLocAddress();
        if(TWCurrentIP != null){
            if(CSCurrentIP != null) {
                TWCurrentIP.setText(CSCurrentIP);
            }else{
                TWCurrentIP.setText("Can't Get IP address");
            }
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
