package com.ctrl.music.ipscan;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.conn.util.InetAddressUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by suxiaocheng on 3/5/15.
 */
public class NetTool {

    private static final String TAG = "NetTool";

    public NetTool() {
        super();
    }

    public String getLocAddress() {

        String ipaddress = "";

        try {
            Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface networks = en.nextElement();

                Enumeration<InetAddress> address = networks.getInetAddresses();

                while (address.hasMoreElements()) {
                    InetAddress ip = address.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip.getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            Log.e("", "Get the local ip address fail");
            e.printStackTrace();
        }

        //System.out.println("本机IP:" + ipaddress);
        return ipaddress;
    }

    public boolean pingCmdExec(String str) {
        boolean status = false;
        Process p;
        try {
            //ping -c 3 -w 100  中  ，-c 是指ping的次数 3是指ping 3次 ，-w 100  以秒为单位指定超时间隔，是指超时时间为100秒
            p = Runtime.getRuntime().exec("ping -c 1 -w 20 " + str);
            status = (p.waitFor() == 0) ? true : false;
            InputStream input = p.getInputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(input));
            StringBuffer buffer = new StringBuffer();
            String line = "";
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
            Log.d(TAG, String.valueOf(buffer));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return status;
    }

    public boolean ping(String str) {
        return pingCmdExec(str);
    }

    public static int getIPNumber(String ip){
        int result = -1;
        int csEnd;
        String IPTmp;

        csEnd = ip.lastIndexOf('.');
        if (csEnd == -1) {
            return result;
        }
        IPTmp = new String(ip.substring(csEnd+1));

        result = Integer.parseInt(IPTmp);

        return result;
    }

    public static boolean compareSameSubIP(String ip1, String ip2){
        int csEnd1, csEnd2;
        String IPTmp1, IPTmp2;

        csEnd1 = ip1.lastIndexOf('.');
        csEnd2 = ip2.lastIndexOf('.');
        if((csEnd1 == -1) || (csEnd2 == -1)){
            return false;
        }

        IPTmp1 = new String(ip1.substring(0, csEnd1));
        IPTmp2 = new String(ip2.substring(0, csEnd2));
        if(IPTmp1.compareTo(IPTmp2) == 0){
            return true;
        }
        return false;
    }
}
