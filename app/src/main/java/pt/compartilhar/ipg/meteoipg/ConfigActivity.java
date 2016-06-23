package pt.compartilhar.ipg.meteoipg;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.EditText;

public class ConfigActivity extends AppCompatActivity {
    static SharedPreferences settings;
    static SharedPreferences.Editor editor;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        settings = this.getPreferences(MODE_WORLD_WRITEABLE);
        editor = settings.edit();
        //get value
        //eill return "0" if preference not exists, else return stored value
        final String val = settings.getString("site", "0");
        final String val2 = settings.getString("pass", "0");
        EditText CFG_site = (EditText) findViewById(R.id.ID_CFG_site);
        CFG_site.setText(val);
        EditText CFG_pass = (EditText) findViewById(R.id.ID_CFG_password);
        CFG_site.setText(val);
        CFG_pass.setText(val2);
        //store value
        editor.putString("site", val);
        editor.putString("pass", val2);
        editor.commit();



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editor = settings.edit();
                EditText CFG_site = (EditText) findViewById(R.id.ID_CFG_site);
                editor.putString("site",CFG_site.getText().toString());

                EditText CFG_pass = (EditText) findViewById(R.id.ID_CFG_password);
                editor.putString("pass",CFG_pass.getText().toString());
                editor.commit();
             //   CFG_site.setText(val);

                Snackbar.make(view, "Configurações atualizadas", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

}
