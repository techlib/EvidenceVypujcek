package cz.techlib.evidencevypujcek;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.NfcV;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.commons.lang3.ArrayUtils;

public class MainActivity extends Activity {
    NFCForegroundUtil nfcForegroundUtil = null;
    private ListView bookList;
    private BookListAdapter adapter;
    private SharedPreferences sharedPref;
    private Toast toast;
    static final ArrayList<HashMap<String, String>> dataList = new ArrayList<HashMap<String, String>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        nfcForegroundUtil = new NFCForegroundUtil(this);

        if (nfcForegroundUtil.getNfc() == null) {
            setContentView(R.layout.activity_no_nfc);
        } else {
            setContentView(R.layout.activity_main);
            bookList = (ListView) findViewById(R.id.list);
            bookList.setEmptyView(findViewById(R.id.empty));


            adapter = new BookListAdapter(this, R.layout.custom_row_view, dataList);
            bookList.setAdapter(adapter);


            sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

            Context context = getApplicationContext();
            this.toast = Toast.makeText(context, getString(R.string.read_error), Toast.LENGTH_SHORT);
            this.toast.setGravity(Gravity.CENTER, 0, 0);

            bookList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {

                    HashMap<String, String> selectedObject = (HashMap<String, String>) bookList.getItemAtPosition(position);

                    if (selectedObject.get("sent") != "true") {
                        selectedObject.put("to_send", selectedObject.get("to_send") == "true" ? "false" : "true");
                        adapter.notifyDataSetChanged();
                    }
                }
            });
        }
    }

    @Override
    public boolean onCreatePanelMenu(int featureId, Menu menu) {
        if (nfcForegroundUtil.getNfc() == null) {
            return false;
        }
        return super.onCreatePanelMenu(featureId, menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (nfcForegroundUtil.getNfc() == null) {
            return false;
        }
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                this.startActivity(intent);
                return true;

            case R.id.menu_clear:
                this.dataList.clear();
                this.adapter.notifyDataSetChanged();
                return true;

            case R.id.menu_upload:
                uploadBooks();
                return true;

            case R.id.menu_reload_names:
                reloadNames();
                return true;
        }
        return true;
    }

    private void uploadBooks() {
        BookAsyncUploader p = new BookAsyncUploader(
                this,
                adapter,
                sharedPref.getString("service_upload_url", ""),
                sharedPref.getBoolean("service_use_auth", false),
                sharedPref.getString("service_auth_user", ""),
                sharedPref.getString("service_auth_password", "")
        );
        p.execute();
    }

    private void reloadNames() {
        for (int i = 0; i < this.adapter.getCount(); i++) {
            HashMap<String, String> item = this.adapter.getItem(i);
            if (item.get("error") == "true") {
                BookAsyncProcessor p = new BookAsyncProcessor(adapter,
                        sharedPref.getString("service_url", ""));
                p.execute(item);
            }
        }
    }

    public void onPause() {
        super.onPause();
        if (nfcForegroundUtil.getNfc() != null) {
            nfcForegroundUtil.disableForeground();
        }
    }

    public void onResume() {
        super.onResume();

        if (nfcForegroundUtil.getNfc() != null) {
            nfcForegroundUtil.enableForeground();

            if (!nfcForegroundUtil.getNfc().isEnabled()) {
                Toast.makeText(getApplicationContext(),
                        "Povolte NFC komunikaci a restartujte aplikaci.",
                        Toast.LENGTH_LONG).show();
                startActivity(
                        new Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS));
            }
        }

    }

    public void onNewIntent(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        byte[] id = tag.getId();
        NfcV nfcMessage = NfcV.get(tag);

        try {
            nfcMessage.connect();

            int resultIndex = 0;
            int blockIndex = 0;
            int blocks = 4; // 0 means 1 block, so it's +1 than 'blocks'

            byte[] cmd = new byte[]{
                    (byte)0x60,
                    (byte)0x23,
                    (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,  // placeholder for tag UID
                    (byte)blockIndex,
                    (byte)blocks};

            System.arraycopy(id, 0, cmd, 2, 8);
            byte[] data = nfcMessage.transceive(cmd);

            int start = 2;
            byte[] results = new byte[data.length];

            for (int i = 0; i <= blocks; i++) {
                for (int b = start; b < start + 4; b++) {
                    // First data block may contain zero bytes, which does not denote end of book code.
                    // this caused troubles with some books
                    if (data[b] == 0 && i > 1) {
                        break;
                    }
                    results[resultIndex] = data[b];
                    resultIndex++;
                }
                start += 5;
            }
            nfcMessage.close();

            results = ArrayUtils.subarray(results, 3, resultIndex);
            String res = new String(results);

            HashMap<String, String> temp = new HashMap<String, String>();
            temp.put("sent", "false");
            temp.put("to_send", "false");
            temp.put("code", res);

            dataList.add(0, temp);
            adapter.notifyDataSetChanged();

            this.toast.cancel();

            BookAsyncProcessor p = new BookAsyncProcessor(adapter,
                    sharedPref.getString("service_url", ""));
            p.execute(temp);

        } catch (IOException e) {
            this.toast.show();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}
