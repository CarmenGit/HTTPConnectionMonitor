package es.cice.httpconnectionmonitor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MainActivity extends AppCompatActivity {
    public static final String RESOURCE_SIZE="size";
    public static final String MYME_TYPE="mime";
    public static final String RESPONSE_HEADERS="headers";
    public static final String TAG="MainActivity";
    public static final int SET_PROGRESS_MAX=1;
    public static final int SET_PROGRESS_NIVEL=2;
    private TextView monitor;
    private ProgressBar pBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        monitor =(TextView)findViewById(R.id.httpMonitorTV);
        pBar=(ProgressBar) findViewById(R.id.progressBar);
        pBar.setVisibility(View.GONE);
    }

    public void startDownload(View v){
        HTTPMonitorAsyncTask task =new HTTPMonitorAsyncTask();
        try{
            task.execute((new URL("http://www.mit.edu")));
        }catch (MalformedURLException e){
            e.printStackTrace();
        }
    }

    public class ProgressBarMessage{

        private int what;
        private int data;
    }

    public class HTTPMonitorAsyncTask extends AsyncTask<URL,ProgressBarMessage,Map<String, Object>>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected Map<String, Object> doInBackground(URL... params) {
            Map<String, Object> httpData=new HashMap<>();
            if(params.length==0)
                return null;
            URL targetUrl=params[0];

            try {
                HttpURLConnection con=(HttpURLConnection) targetUrl.openConnection();
                //la clave es el nombre del encabezado
                Map<String,List<String>> headers = con.getHeaderFields();
                Set<String> keys=headers.keySet();
                for (String key:keys){

                    List<String> values =headers.get(key);
                    StringBuffer buffer=new StringBuffer();
                    for (String value:values){
                        buffer.append(value + " ");
                    }
                    Log.d(TAG, key + ": " + buffer.toString());
                    httpData.put(key, buffer.toString());
                }
                InputStream in=con.getInputStream();
                ProgressBarMessage msg=new ProgressBarMessage();
                msg.what=SET_PROGRESS_MAX;
                msg.data=in.available()>0?in.available():1200;
                publishProgress(msg);

                int size=0;
                int byteLeido;
                while((byteLeido=in.read())!=-1){
                    size++;
                    //pq va muy rápido y no deja ver nada
                    Thread.sleep(100);
                    msg=new ProgressBarMessage();
                    msg.what=SET_PROGRESS_NIVEL;
                    msg.data=size;
                    publishProgress(msg);
                }
                httpData.put(RESOURCE_SIZE, size);
                return httpData;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }catch (InterruptedException e){
                e.printStackTrace();
                return null;
            }
        }


        @Override
        protected void onProgressUpdate(ProgressBarMessage... values) {
            //podemos pasar un número variable de enteros
            //también se ejecuta en el hilo ppal
            for(ProgressBarMessage msg:values){
                switch (msg.what){
                    case SET_PROGRESS_MAX:
                        pBar.setMax(msg.data);
                        break;
                    case SET_PROGRESS_NIVEL:
                        pBar.setProgress(msg.data);
                        break;
                }
            }

        }
        @Override
        //lo que hayamos devuelto entra por aquí, podemos publicar en el interfaz de usuario pq se ejecuta en el hilo ppal
        protected void onPostExecute(Map<String, Object> stringObjectMap) {
            //se ejecuta en el hilo principal
            Set<String> keys= stringObjectMap.keySet();
                    for(String key:keys){
                        Object values=stringObjectMap.get(key);
                        monitor.append(key + ": " + values);
                    }
        }
    }

}
