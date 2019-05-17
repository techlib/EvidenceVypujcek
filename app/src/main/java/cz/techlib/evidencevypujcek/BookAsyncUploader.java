package cz.techlib.evidencevypujcek;


import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import org.xmlpull.v1.XmlSerializer;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Xml;

import org.apache.commons.codec.binary.Base64;

public class BookAsyncUploader extends AsyncTask<Void, String, Void> {

    private ProgressDialog progressDialog;
    private String url;
    private BookListAdapter adapter;
    private MainActivity mainWindow;
    private boolean useAuth = false;
    private String username = "";
    private String password = "";

    public BookAsyncUploader(MainActivity activity, BookListAdapter adapter, String url, Boolean useAuth, String username, String password) {
        this.adapter = adapter;
        this.url = url;
        this.mainWindow = activity;
        this.useAuth = useAuth;
        this.username = username;
        this.password = password;
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(this.mainWindow);
        progressDialog.setMessage(mainWindow.getResources().getString(R.string.upload_message));
        progressDialog.setTitle(R.string.upload_title);
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(Void result) {

        this.adapter.notifyDataSetChanged();
        progressDialog.dismiss();
    }


    protected Void doInBackground(Void... params) {

        for (int i = 0; i < this.adapter.getCount(); i++) {

            HashMap<String, String> item = this.adapter.getItem(i);

            if (item.get("to_send") == "false" || item.get("sent") == "true") {
                continue;
            }

            try {

                XmlSerializer serializer = Xml.newSerializer();
                StringWriter writer = new StringWriter();

                serializer.setOutput(writer);
                serializer.startDocument("UTF-8", true);
                serializer.startTag("", "circLoggerRequest30");
                serializer.startTag("", "barcode");
                serializer.text(item.get("code"));
                serializer.endTag("", "barcode");
                serializer.endTag("", "circLoggerRequest30");
                serializer.endDocument();

                String content = writer.toString();

                URL url = new URL(this.url);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoOutput(true);
                connection.setDoInput(true);
                connection.setInstanceFollowRedirects(false);
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                connection.setRequestProperty("charset", "utf-8");
                connection.setRequestProperty("Content-Length", "" + Integer.toString(content.getBytes().length));

                if (this.useAuth) {
                    String authData = this.username + ":" + this.password;
                    String auth = new String(Base64.encodeBase64(authData.getBytes()));
                    connection.setRequestProperty("Authorization", "Basic " + auth);
                }
                connection.setUseCaches(false);

                DataOutputStream wr;
                wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(content);
                wr.flush();
                wr.close();

                String reply;
                InputStream in = connection.getInputStream();

                StringBuffer sb = new StringBuffer();
                try {
                    int chr;

                    while ((chr = in.read()) != -1) {
                        sb.append((char) chr);
                        ;
                    }
                    reply = sb.toString();

                    if (reply.contains("<status>LOAN</status>")) {
                        throw new LoanException("This book is loaned: ".concat(reply));
                    }

                    if (!reply.contains("<status>ACK</status>")) {
                        throw new Exception("Wrong answer from server: ".concat(reply));
                    }

                } finally {
                    in.close();
                }

                connection.disconnect();
                item.put("sent", "true");
            } catch (LoanException e) {
                item.put("loan", "true");
                item.put("sent", "true");
            } catch (Exception e) {
                item.put("error", "true");
                item.put("error_message", e.getMessage());
            }
        }
        return null;
    }
}