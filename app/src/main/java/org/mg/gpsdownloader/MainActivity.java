package org.mg.gpsdownloader;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.ma1co.pmcademo.app.BaseActivity;
import com.github.ma1co.pmcademo.app.Logger;

import org.conscrypt.Conscrypt;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.security.Provider;
import java.security.Security;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class MainActivity extends BaseActivity {
    private ProgressBar progressBarDownloadProgress;

    private TextView textViewDownloadStatus;

    private TextView textViewDownloadResult;

    private View buttonDownload;

    private View progressBarSpin;

    private DownloadTask mDownloadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        progressBarDownloadProgress = (ProgressBar) findViewById(R.id.progressBarDownloadProgress);

        buttonDownload = findViewById(R.id.buttonDownload);

        progressBarSpin = findViewById(R.id.progressBarSpin);


        buttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDownloadTask = new DownloadTask();
                mDownloadTask.execute();
            }
        });

        textViewDownloadStatus = (TextView) findViewById(R.id.textViewDownloadStatus);
        textViewDownloadResult = (TextView) findViewById(R.id.textViewDownloadResult);

        buttonDownload.setEnabled(true);
    }

    class DownloadTask extends AsyncTask<Void, Integer, Void> {

        String error="";
        int totalBytes=0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonDownload.setEnabled(false);
            textViewDownloadStatus.setText(R.string.statusDownloading);
            progressBarSpin.setVisibility(View.VISIBLE);
            setAutoPowerOffMode(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            buttonDownload.setEnabled(true);
            if (error!="") {
                textViewDownloadStatus.setText(R.string.statusError);
                textViewDownloadResult.setText(error);
            } else {
                textViewDownloadStatus.setText(R.string.statusComplete);
                textViewDownloadResult.setText(R.string.reboot_hint);
            }
            progressBarSpin.setVisibility(View.GONE);
            setAutoPowerOffMode(true);
            mDownloadTask = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            progressBarDownloadProgress.setMax(totalBytes);
            progressBarDownloadProgress.setProgress(values[0]);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {

                // Prepare the file
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/PRIVATE/SONY/GPS");
                dir.mkdirs();
                File file = new File(dir, "ASSISTME.DAT");
                FileOutputStream out = new FileOutputStream(file);

                // Prepare the TLS1.2 support for Android 4.4
                Provider conscrypt = Conscrypt.newProvider();
                Security.insertProviderAt(conscrypt, 1);
                //X509TrustManager tm = Conscrypt.getDefaultX509TrustManager();
                SSLContext sslContext = SSLContext.getInstance("TLS", conscrypt);
                //sslContext.init(null, new TrustManager[] { tm }, null);
                sslContext.init(null, null, null);

                // Open the connection
                URL url = new URL("https://control.d-imaging.sony.co.jp/GPS/assistme.dat");
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                urlConnection.setSSLSocketFactory(new InternalSSLSocketFactory(sslContext.getSocketFactory()));
                urlConnection.connect();

                // Copy the file
                totalBytes = urlConnection.getContentLength();
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] buf = new byte[8192];
                int length;
                int pos=0;
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
                    pos+=length;
                    publishProgress(pos);
                }
                out.close();
                urlConnection.disconnect();
            } catch (Exception e) {
                error=e.toString();
                Logger.error(error);
            }
            return null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mDownloadTask != null) {
            mDownloadTask.cancel(true);
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Logger.info("setting wifi enabled state to " + false);
        wifiManager.setWifiEnabled(false);
    }
}