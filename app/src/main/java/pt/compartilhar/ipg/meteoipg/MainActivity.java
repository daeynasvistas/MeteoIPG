package pt.compartilhar.ipg.meteoipg;

import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity {

    BluetoothArduino mBlue = BluetoothArduino.getInstance("IPG-Meteo");
    String resultado = "";

    // ---------------------------------------------------------------------interrupts com timer
    Timer timer = new Timer ();
    TimerTask enviar_dados = new TimerTask () {
        @Override
        public void run () {
            // your code here...
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    enviarDados();
                }
            });
        }
    };
    // ----------------------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (CheckBt()){
              mBlue.Connect();
        }; // veridica se o blueth está ligado
        //---------------------------------------------------------------------------------------  TEST envio
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                enviarDados();
                Snackbar.make(view, "Dados enviados!", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        //---------------------------------------------------------------------------------------  TEST envio FIM
// schedule the task to run starting now and then every hour...
        timer.schedule (enviar_dados, 0l, 100*60*10);   // 1000*10*60 every 10 minut
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, ConfigActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
    }//----------------------------------------------------------------------------------------------

    // ------------------------------------------------------------------------------ VERIFICA BLUETH
    private boolean CheckBt() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        TextView info_OK    = (TextView) findViewById(R.id.ID_connect);
        if (!mBluetoothAdapter.isEnabled()) {
            Toast.makeText(getApplicationContext(), "Bluetooth OFF !",
                    Toast.LENGTH_SHORT).show();
                   /* It tests if the bluetooth is enabled or not, if not the app will show a message. */
                    info_OK.setText("Bluetooth Desligado!! Na configuração do android, ativar o bluetooth (depois clique em Ligar)");
                    View Ligar = findViewById(R.id.ID_BTN_on);
                    Ligar.setVisibility(View.VISIBLE);
                    return false;
        }

        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(),
                    "Não tenho Bluetooth!", Toast.LENGTH_SHORT)
                    .show();
                    info_OK.setText("Não tenho Bluetooth!!");
                    View Ligar = findViewById(R.id.ID_BTN_on);
                    Ligar.setVisibility(View.VISIBLE);
                    return false;
        }


        info_OK.setText("Tudo Parece OK, estou ligado!!");
        View Ligar = findViewById(R.id.ID_BTN_on);
        Ligar.setVisibility(View.INVISIBLE);
    return true;
    } //----------------------------------------------------------------------------------------------


    // ---------------------------------------------------------------------------------- ENVIA CENAS
    public void enviarDados(){

        mBlue.Connect();
        String msg = mBlue.getLastMessage();
        // info_get.setText(msg);


        // ############################################################################ ENVIAR ONLINE
        // construir a string de envio
        if (msg.length()>4) {
            String[] ar = msg.split("[-]");
            SharedPreferences settings = this.getPreferences(MODE_WORLD_WRITEABLE);
            String password = settings.getString("pass", "123456789");

            TextView info_get = (TextView) findViewById(R.id.ID_get);
            info_get.setText("ENVIO: "+
                    enviarDadosOnline(
                            password, //arduino key (fazer configuração)
                            ar[1], // humidade
                            ar[2], // temp
                            ar[3], // rain
                            "1"  // cycle

                    )+" - T:"+ar[2]+" H:"+ar[1]+" C:"+ar[3]
            );
        }

    } //----------------------------------------------------------------------------------------------


    //----------------------------------------------ENVIAR
    public String enviarDadosOnline(String arduino, String hum, String temp, String rain,String cycle) {
        SharedPreferences settings = this.getPreferences(MODE_WORLD_WRITEABLE);
        String site = settings.getString("site", "http://compartilhar.pt/meteo/");
        final String url = site+ "index.php?page=gate"+ "&api_key=" +arduino+"&humidity="+hum+"&temperature="+temp+"&rain="+rain+"&cycle="+cycle;
        new Thread() {
            public void run() {
                InputStream in = null;

                try {
                    in = openHttpConnection(url);
                    in.close();
                }

                catch (IOException e1) {
                    e1.printStackTrace();
                }

            }
        }.start();

        return resultado;
    }
    //--------------------------------------------------------- LIGAÇAO
    private InputStream openHttpConnection(String urlStr) {
        InputStream in = null;
        int resCode = -1;
        // recebe só 100 char
        // web page.
        int len = 100;

        try {
            URL url = new URL(urlStr);
            URLConnection urlConn = url.openConnection();

            if (!(urlConn instanceof HttpURLConnection)) {
                throw new IOException("URL não é de um site");
            }
            HttpURLConnection httpConn = (HttpURLConnection) urlConn;
            httpConn.setAllowUserInteraction(false);
            httpConn.setInstanceFollowRedirects(true);
            httpConn.setRequestMethod("GET");
            httpConn.connect();
            resCode = httpConn.getResponseCode();

            if (resCode == HttpURLConnection.HTTP_OK) {
                in = httpConn.getInputStream();
                // como correu a postagem? ler info do resultado
                resultado = readIt(in, len);
            }


        }

        catch (MalformedURLException e) {
            e.printStackTrace();
        }

        catch (IOException e) {
            e.printStackTrace();
        }

        return in;
    }

    // Reads an InputStream and converts it to a String.
    public String readIt(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        Reader reader = null;
        reader = new InputStreamReader(stream, "UTF-8");
        char[] buffer = new char[len];
        reader.read(buffer);

        String s = new String(buffer);
        String s_resultado = s.substring(s.indexOf("<arduino>") + 9, s.indexOf("</arduino>")); // TAG usada <arduino>
        return s_resultado;//new String(buffer);
    }


    //-----------------------------------------------------------------------------------------------------------FIM-teste
    public void BTN_ligar(View view){

        CheckBt();
        mBlue.Connect();


        View Ligar = findViewById(R.id.ID_BTN_on);
        Ligar.setVisibility(View.INVISIBLE);

    }

}
