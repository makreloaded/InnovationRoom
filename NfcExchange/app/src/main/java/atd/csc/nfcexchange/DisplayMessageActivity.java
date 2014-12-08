package atd.csc.nfcexchange;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;


public class DisplayMessageActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_message);


        Bundle intent = getIntent().getExtras();
        //Bundle intent =getIntent().getExtras();
        String response_id = intent.getString("info_id");
        String response_age = intent.getString("info_age");
        String response_name = intent.getString("info_name");
        String response_sex = intent.getString("info_sex");
        String response_weight = intent.getString("info_weight");

        EditText patid = (EditText)findViewById(R.id.patient_id);
        EditText age= (EditText)findViewById(R.id.age);
        EditText name= (EditText)findViewById(R.id.name);
        EditText sex= (EditText)findViewById(R.id.sex);
        EditText weight= (EditText)findViewById(R.id.weight);
        EditText bg= (EditText)findViewById(R.id.bg);
        EditText allergic= (EditText)findViewById(R.id.alg);
        EditText doc= (EditText)findViewById(R.id.doc);
        EditText ward= (EditText)findViewById(R.id.ward);
        EditText bed= (EditText)findViewById(R.id.bed);


        patid.setText(" ");
        age.setText(" ");
        name.setText(" ");
        sex.setText(" ");
        weight.setText(" ");

        Log.d("id_display", response_id);
        Log.d("age_display",response_age);

        patid.setText(response_id);
        age.setText(response_age);
        name.setText(response_name);
        sex.setText(response_sex);
        weight.setText(response_weight);
        bg.setText(" B+ ");
        allergic.setText("--");
        doc.setText(" Dr. Satish ");
        ward.setText("13");
        bed.setText("1");

       /* TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(response);
        setContentView(textView);*/
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_message, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
