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
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MainActivity extends BaseActivity {
    private ProgressBar progressBarUploadProgress;

    private TextView textViewPhotosToUploadCount;
    private TextView textViewUploadedCount;
    private TextView textViewUploadStatus;

    private View buttonUploadPhotos;
    private View buttonSettings;

    private View progressBarSpin;

    private UploadTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        progressBarUploadProgress = (ProgressBar) findViewById(R.id.progressBarUploadProgress);

        buttonUploadPhotos = findViewById(R.id.buttonUploadPhotos);
        buttonSettings = findViewById(R.id.buttonSettings);

        progressBarSpin = findViewById(R.id.progressBarSpin);


        buttonUploadPhotos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUploadTask = new UploadTask();
                mUploadTask.execute();
            }
        });

        textViewPhotosToUploadCount = (TextView) findViewById(R.id.textViewPhotosToUploadCount);
        textViewUploadedCount = (TextView) findViewById(R.id.textViewUploadedCount);
        textViewUploadStatus = (TextView) findViewById(R.id.textViewUploadStatus);

        buttonUploadPhotos.setEnabled(toUploadCount != 0);
    }

    private static final int NOT_INITIALIZED = -1;

    private int toUploadCount = NOT_INITIALIZED;

    class UploadTask extends AsyncTask<Void, Integer, Void> {

        String error="";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            buttonUploadPhotos.setEnabled(false);
            buttonSettings.setEnabled(false);
            textViewUploadStatus.setText(R.string.statusUploading);
            progressBarSpin.setVisibility(View.VISIBLE);
            setAutoPowerOffMode(false);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            buttonUploadPhotos.setEnabled(true);
            buttonSettings.setEnabled(true);
            if (error!="")
                textViewUploadStatus.setText(error);
            else
                textViewUploadStatus.setText(R.string.statusComplete);
            progressBarSpin.setVisibility(View.GONE);
            setAutoPowerOffMode(true);
            mUploadTask = null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                File sdCard = Environment.getExternalStorageDirectory();
                File dir = new File (sdCard.getAbsolutePath() + "/PRIVATE/SONY/GPS");
                dir.mkdirs();
                File file = new File(dir, "ASSISTME.DAT");
                FileOutputStream out = new FileOutputStream(file);

                /*Provider conscrypt = Conscrypt.newProvider();
                Security.insertProviderAt(conscrypt, 1);
                OkHttpClient.Builder okHttpBuilder = new OkHttpClient()
                    .newBuilder()
                    .connectionSpecs(Collections.singletonList(ConnectionSpec.RESTRICTED_TLS));
                try {
                    X509TrustManager tm = Conscrypt.getDefaultX509TrustManager();
                    SSLContext sslContext = SSLContext.getInstance("TLS", conscrypt);
                    sslContext.init(null, new TrustManager[] { tm }, null);
                    okHttpBuilder.sslSocketFactory(new InternalSSLSocketFactory(sslContext.getSocketFactory()), tm);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                OkHttpClient okHttpClient = okHttpBuilder.build();
                Request request = new Request.Builder()
                    .url("https://control.d-imaging.sony.co.jp/GPS/assistme.dat") // You can try another TLS 1.3 capable HTTPS server
                    .build();
                okHttpClient.newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(final Call call, IOException e) {
                            e.printStackTrace();
                            Logger.error("onFailure()");
                        }

                        @Override
                        public void onResponse(Call call,final Response response) throws IOException {
                            Logger.info("onResponse() tlsVersion=" + response.handshake().tlsVersion());
                            Logger.info("onResponse() cipherSuite=" + response.handshake().cipherSuite().toString());
                            // D/TestApp##: onResponse() tlsVersion=TLS_1_3
                            // D/TestApp##: onResponse() cipherSuite=TLS_AES_256_GCM_SHA384
                        }
                    });*/

                /*OkHttpClient.Builder client = new OkHttpClient.Builder()
                    .followRedirects(true)
                    .followSslRedirects(true)
                    .cache(null)
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .writeTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS);

                SSLContext sc = SSLContext.getInstance("TLSv1.2");
                sc.init(null, null, null);
                client.sslSocketFactory(new Tls12SocketFactory(sc.getSocketFactory()));

                ConnectionSpec cs = new ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
                    .tlsVersions(TlsVersion.TLS_1_2)
                    .build();

                List<ConnectionSpec> specs = new ArrayList<>();
                specs.add(cs);
                //specs.add(ConnectionSpec.COMPATIBLE_TLS);
                //specs.add(ConnectionSpec.CLEARTEXT);

                client.connectionSpecs(specs);

                OkHttpClient okHttpClient = client.build();
                Request request = new Request.Builder()
                    .url("https://control.d-imaging.sony.co.jp/GPS/assistme.dat") // You can try another TLS 1.3 capable HTTPS server
                    .build();
                okHttpClient.newCall(request)
                    .enqueue(new Callback() {
                        @Override
                        public void onFailure(final Call call, IOException e) {
                            e.printStackTrace();
                            Logger.error("onFailure()");
                        }

                        @Override
                        public void onResponse(Call call,final Response response) throws IOException {
                            Logger.info("onResponse() tlsVersion=" + response.handshake().tlsVersion());
                            Logger.info("onResponse() cipherSuite=" + response.handshake().cipherSuite().toString());
                            // D/TestApp##: onResponse() tlsVersion=TLS_1_3
                            // D/TestApp##: onResponse() cipherSuite=TLS_AES_256_GCM_SHA384
                        }
                    });*/
                Provider conscrypt = Conscrypt.newProvider();
                Security.insertProviderAt(conscrypt, 1);
                //X509TrustManager tm = Conscrypt.getDefaultX509TrustManager();
                SSLContext sslContext = SSLContext.getInstance("TLS", conscrypt);
                //sslContext.init(null, new TrustManager[] { tm }, null);
                sslContext.init(null, null, null);
                URL url = new URL("https://control.d-imaging.sony.co.jp/GPS/assistme.dat");
                //HttpsURLConnection.setDefaultSSLSocketFactory(new TLSSocketFactory());
                HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
                Logger.info("1");
                urlConnection.setSSLSocketFactory(new InternalSSLSocketFactory(sslContext.getSocketFactory()));
                Logger.info("2");
                urlConnection.connect();
                Logger.info("3");
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                byte[] buf = new byte[8192];
                int length;
                while ((length = in.read(buf)) != -1) {
                    out.write(buf, 0, length);
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
        if(mUploadTask != null) {
            mUploadTask.cancel(true);
        }
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        Logger.info("setting wifi enabled state to " + false);
        wifiManager.setWifiEnabled(false);
    }
}