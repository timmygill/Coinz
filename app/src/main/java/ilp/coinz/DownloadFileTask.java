package ilp.coinz;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadFileTask extends AsyncTask<String, Void, String> {

    private final String tag = "DownloadFileTask";

    private DownloadFileResponse delegate = null;

    @Override
    protected String doInBackground(String... urls){
        try{
            return loadFileFromNetwork(urls[0]);
        } catch (IOException e){
            return e.toString();
            //return "Unable to load content. Check your network connection.";
        }
    }

    private String loadFileFromNetwork(String urlString) throws IOException{
        return readStream(downloadUrl(new URL(urlString)));
    }

    private InputStream downloadUrl(URL url) throws IOException{
        Log.d(tag, "Trying to download " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }

    @NonNull
    private String readStream(InputStream stream) throws IOException{
        Log.d(tag, "Trying to read stream");
            java.util.Scanner s = new java.util.Scanner(stream).useDelimiter("\\Z");

            String result =  s.hasNext() ? s.next() : "";

            stream.close();
            //Log.d(tag, "result: " + result);
            return result;
    }



    @Override
    protected void onPostExecute(String result){
        super.onPostExecute(result);
        try {
            delegate.downloadFinish(result);
        } catch (NullPointerException e){
            Log.d(tag, "Fatal error, coin data not found");
            Log.d(tag, e.toString());
        }
    }

    public void setDelegate(DownloadFileResponse delegate) {
        this.delegate = delegate;
    }
}
