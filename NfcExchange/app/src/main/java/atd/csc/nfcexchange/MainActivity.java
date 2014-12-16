package atd.csc.nfcexchange;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


public class MainActivity extends Activity {
    public final static String info_Response = null,info_age=null,info_weight=null,info_name=null,info_sex=null;
    ProgressDialog prgDialog;
    EditText patid;

    public static int response_weight, response_age,info_id;
    public static String response_name,response_sex,response_bg;

    private TextView mTextView;
    private NfcAdapter mNfcAdapter;
    public static final String MIME_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcDemo";

    // service URL
   private static final String SERVICE_URL = "http://nfc.t.proxylocal.com/RestWebService/rest/patient";
    //private static final String SERVICE_URL = "http://localhost:5000/RestWebService/rest/patient";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       // mTextView = (TextView) findViewById(R.id.textView_explanation);

        mNfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (mNfcAdapter == null) {
            // Stop here, we definitely need NFC
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
            return;

        }

        if (!mNfcAdapter.isEnabled()) {
          //  mTextView.setText("NFC is disabled.");
        } else {
          //  mTextView.setText(R.string.explanation);
        }

        handleIntent(getIntent());



        patid = (EditText)findViewById(R.id.pid);
        prgDialog = new ProgressDialog(this);

        // Set Progress Dialog Text
        prgDialog.setMessage("Please wait...");
    }

    @Override
    protected void onResume() {
        super.onResume();

        /**
         * It's important, that the activity is in the foreground (resumed). Otherwise
         * an IllegalStateException is thrown.
         */
        setupForegroundDispatch(this, mNfcAdapter);
    }

    @Override
    protected void onPause() {
        /**
         * Call this before onPause, otherwise an IllegalArgumentException is thrown as well.
         */
        stopForegroundDispatch(this, mNfcAdapter);

        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        /**
         * This method gets called, when a new Intent gets associated with the current activity instance.
         * Instead of creating a new activity, onNewIntent will be called. For more information have a look
         * at the documentation.
         *
         * In our case this method gets called, when the user attaches a Tag to the device.
         */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        // TODO: handle Intent

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {

            String type = intent.getType();
            if (MIME_TEXT_PLAIN.equals(type)) {

                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                new NdefReaderTask().execute(tag);

            } else {
                Log.d(TAG, "Wrong mime type: " + type);
            }

            }
        }

    private class NdefReaderTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // NDEF is not supported by this Tag.
                return null;
            }

            NdefMessage ndefMessage = ndef.getCachedNdefMessage();

            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readText(NdefRecord record) throws UnsupportedEncodingException {
        /*
         * See NFC forum specification for "Text Record Type Definition" at 3.2.1
         *
         * <span class="skimlinks-unlinked">http://www.nfc-forum.org/specs</span>/
         *
         * bit_7 defines encoding
         * bit_6 reserved for future use, must be 0
         * bit_5..0 length of IANA language code
         */

            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
              //  mTextView.setText("Read content: " + result);
                info_id = Integer.parseInt(result);
                callWebService();
              //  get_data();
            }
        }
    }

    /**
     * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void setupForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        final Intent intent = new Intent(activity.getApplicationContext(), activity.getClass());
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

        final PendingIntent pendingIntent = PendingIntent.getActivity(activity.getApplicationContext(), 0, intent, 0);

        IntentFilter[] filters = new IntentFilter[1];
        String[][] techList = new String[][]{};

        // Notice that this is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            filters[0].addDataType(MIME_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            throw new RuntimeException("Check your mime type.");
        }

        adapter.enableForegroundDispatch(activity, pendingIntent, filters, techList);
    }

    /**
     * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
     * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity activity, NfcAdapter adapter) {
        adapter.disableForegroundDispatch(activity);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /*@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSobj.put("ID", id);
        elected(item);
    }*/

    public void get_data(View view) {
        String Patient = patid.getText().toString();
        info_id = Integer.parseInt(Patient);
        callWebService();
    }

    public void callWebService(){

        String URL = SERVICE_URL + "/id?id=" + info_id;
        Log.d("URL",""+ URL+"");
        WebServiceTask wst = new WebServiceTask(WebServiceTask.GET_TASK, this, "Getting data...");

        wst.execute(new String[] { URL });
        }

    public void handleResponse(String response) {
        try {

            if(response.contains("Patient ID not found"))
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setMessage("Patient ID not found");
                alertDialogBuilder.setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {


                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
            else {

                JSONObject jso = new JSONObject(response);


                response_weight = jso.getInt("Weight");
                response_age = jso.getInt("Age");
                response_name = jso.getString("Name");
                response_bg = jso.getString("BG");
                response_sex = jso.getString("Sex");

                Log.d("weight_main", " " + response_weight + " ");
                Log.d("age_main", " " + response_age + " ");
                Log.d("name_main", " " + response_name + " ");
                Log.d("sex_main", " " + response_sex + " ");
                callDisplayResponse();

            }
        } catch (Exception e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }
        Log.d("Response",""+ response+"");


    }

    public void callDisplayResponse()
    {

        Intent intent = new Intent(this, DisplayMessageActivity.class);

       /* StringBuilder response = new StringBuilder();
        response.append("Patient ID: " + response_id);
        response.append(System.getProperty("line.separator"));
        response.append("Age: " + response_age);
        String output = response.toString();
        intent.putExtra(info_Response,output);*/

        Bundle bundle = new Bundle();

        bundle.putString("info_weight"  ," "+response_weight);
        bundle.putString("info_age" ," "+response_age);
        bundle.putString("info_name" ," "+response_name);
        bundle.putString("info_sex" ," "+response_sex);
        bundle.putString("info_id" ," "+info_id);
        intent.putExtras(bundle);
        Log.d("bundle",""+ bundle+"");
        startActivity(intent);
        // intent.putExtra(info_age,""+age);
    }
    private class WebServiceTask extends AsyncTask<String, Integer, String> {


        public static final int GET_TASK = 2;

        private static final String TAG = "WebServiceTask";

        // connection timeout, in milliseconds (waiting to connect)
        private static final int CONN_TIMEOUT = 100000;

        // socket timeout, in milliseconds (waiting for data)
        private static final int SOCKET_TIMEOUT = 100000;

        private int taskType = GET_TASK;
        private Context mContext = null;
        private String processMessage = "Processing...";



        private ProgressDialog pDlg = null;

        public WebServiceTask(int taskType, Context mContext, String processMessage) {

            this.taskType = taskType;
            this.mContext = mContext;
            this.processMessage = processMessage;
        }


        private void showProgressDialog() {

            pDlg = new ProgressDialog(mContext);
            pDlg.setMessage(processMessage);
            pDlg.setProgressDrawable(mContext.getWallpaper());
            pDlg.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            pDlg.setCancelable(false);
            pDlg.show();

        }

        @Override
        protected void onPreExecute() {


            showProgressDialog();

        }

        protected String doInBackground(String... urls) {

            String url = urls[0];
            String result = "";

            HttpResponse response = doResponse(url);

            if (response == null) {
                return result;
            } else {

                try {

                    try {
                        result = inputStreamToString(response.getEntity().getContent());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } catch (IllegalStateException e) {
                    Log.e(TAG, e.getLocalizedMessage(), e);

                }


            }

            return result;
        }


        @Override
        protected void onPostExecute(String response) {

            handleResponse(response);
            pDlg.dismiss();

        }

        // Establish connection and socket (data retrieval) timeouts
        private HttpParams getHttpParams() {

            HttpParams http = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(http, CONN_TIMEOUT);
            HttpConnectionParams.setSoTimeout(http, SOCKET_TIMEOUT);

            return http;
        }

        private HttpResponse doResponse(String url) {

            // Use our connection and data timeouts as parameters for our
            // DefaultHttpClient
            HttpClient httpclient = new DefaultHttpClient(getHttpParams());

            HttpResponse response = null;

            try {
                switch (taskType) {

                    case GET_TASK:
                        HttpGet httpget = new HttpGet(url);
                        response = httpclient.execute(httpget);
                        break;
                }
            } catch (Exception e) {

                Log.e(TAG, e.getLocalizedMessage(), e);

            }

            return response;
        }

    }
    private String inputStreamToString(InputStream is) {

        String line = "";
        StringBuilder total = new StringBuilder();

        // Wrap a BufferedReader around the InputStream
        BufferedReader rd = new BufferedReader(new InputStreamReader(is));

        try {
            // Read response until the end
            while ((line = rd.readLine()) != null) {
                total.append(line);
            }
        } catch (IOException e) {
            Log.e(TAG, e.getLocalizedMessage(), e);
        }

        // Return full string
        return total.toString();
    }

}
