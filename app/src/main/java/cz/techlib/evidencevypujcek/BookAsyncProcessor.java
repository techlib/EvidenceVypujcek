package cz.techlib.evidencevypujcek;

import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;


public class BookAsyncProcessor extends AsyncTask<HashMap<String, String>, Void, HashMap<String, String>> {

    private BookListAdapter adapter;

    private String url;
    private String param;

    public BookAsyncProcessor(BookListAdapter adapter, String url) {
        this.adapter = adapter;
        this.url = url;
    }

    @Override
    protected HashMap<String, String> doInBackground(HashMap<String, String>... params) {
        try {

            String finalUrl = this.url.replace("{id}", URLEncoder.encode(params[0].get("code"), "UTF-8"));
            URL serviceUrl = new URL(finalUrl);
            ObjectMapper mapper = new ObjectMapper();
            HashMap<String, String> result = mapper.readValue(serviceUrl.openStream(), HashMap.class);

            if (result.containsKey("error")) {
                throw new Exception(result.get("error"));
            }

            params[0].put("name", result.get("title"));
            params[0].put("author", result.get("author"));
            params[0].put("location", result.get("collection"));
            params[0].put("error", "false");


        } catch (Exception e) {
            params[0].put("location", "Chyba");
            params[0].put("error", "true");
            params[0].put("error_message", e.getMessage());
        }
        return params[0];

    }

    protected void onPostExecute(HashMap<String, String> result) {
        adapter.notifyDataSetChanged();
    }

}