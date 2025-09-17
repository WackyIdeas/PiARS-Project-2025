package bogdan.cvetanovski.pasalic;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/*
Example
new Thread(new Runnable() {
    public void run() {
        try {
            JSONObject jsonobject = httpHelper.getJSONObjectFromURL(GET_ONE);
            JSONArray message = jsonobject.getJSONArray("message");
            for (int i = 0; i < message.length(); i++) {
                final String value = message.getString(i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mListAdapter.add(value);
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}).start();
*/
public class HttpHelper {

    private static final int SUCCESS = 200;

    // Change the IP address accordingly
    public static final String HTTP_SERVER = "http://192.168.1.17:8080/api/";

    /*HTTP get json Array*/
    public static JSONArray getJSONArrayFromURL(String endpoint) throws IOException, JSONException {
        HttpURLConnection urlConnection;
        java.net.URL url = new URL(HTTP_SERVER + endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        /*header fields*/
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        try {
            urlConnection.connect();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();
        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON data- " + jsonString);
        int responseCode =  urlConnection.getResponseCode();
        urlConnection.disconnect();
        System.out.println(responseCode);
        return responseCode == SUCCESS ? new JSONArray(jsonString) : null;
    }

    /*HTTP get json object*/
    public static JSONObject getJSONObjectFromURL(String endpoint) throws IOException, JSONException {
        HttpURLConnection urlConnection;
        java.net.URL url = new URL(HTTP_SERVER+endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        /*header fields*/
        urlConnection.setRequestMethod("GET");
        urlConnection.setRequestProperty("Accept", "application/json");
        urlConnection.setReadTimeout(10000 /* milliseconds */ );
        urlConnection.setConnectTimeout(15000 /* milliseconds */ );
        try {
            urlConnection.connect();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return null;
        }
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line + "\n");
        }
        br.close();

        String jsonString = sb.toString();
        Log.d("HTTP GET", "JSON obj- " + jsonString);
        int responseCode =  urlConnection.getResponseCode();
        urlConnection.disconnect();
        System.out.println(responseCode);
        return responseCode == SUCCESS ? new JSONObject(jsonString) : null;
    }

    /*HTTP post*/
    public static boolean postJSONObjectFromURL(String endpoint, JSONObject jsonObject) throws IOException {
        HttpURLConnection urlConnection = null;
        java.net.URL url = new URL(HTTP_SERVER+endpoint);
        urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setRequestMethod("POST");
        urlConnection.setRequestProperty("Content-Type", "application/json;charset=UTF-8");
        urlConnection.setRequestProperty("Accept","application/json");
        /*needed when used POST or PUT methods*/
        urlConnection.setDoOutput(true);
        urlConnection.setDoInput(true);
        try {
            urlConnection.connect();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            return false;
        }
        DataOutputStream os = new DataOutputStream(urlConnection.getOutputStream());
        /*write json object*/
        os.writeBytes(jsonObject.toString());
        os.flush();
        os.close();
        int responseCode =  urlConnection.getResponseCode();
        Log.i("STATUS", String.valueOf(urlConnection.getResponseCode()));
        Log.i("MSG" , urlConnection.getResponseMessage());
        urlConnection.disconnect();
        System.out.println(responseCode);
        return (responseCode==SUCCESS);
    }

}
