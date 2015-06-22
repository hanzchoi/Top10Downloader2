package nyc.c4q.personabe1984.top10downloader;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


public class MainActivity extends Activity {

    Button btnParse;
    ListView listApps;
    String xmlData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnParse = (Button) findViewById(R.id.btnParse);
        listApps = (ListView) findViewById(R.id.listApps);

        btnParse.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ParseApplications parse = new ParseApplications(xmlData);
                boolean operationStatus = parse.process();
                if(operationStatus ){
                    ArrayList<Application> allApps = parse.getApplications();

                    ArrayAdapter<Application> adapter = new ArrayAdapter<Application>
                            (MainActivity.this, R.layout.list_item, allApps);
                    listApps.setVisibility(listApps.VISIBLE);
                    listApps.setAdapter(adapter);
                }else{
                    Log.d("MainActivity" , "Error parsing file");
                }
            }
        });

        new DownloadData().execute("http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=10/xml");
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
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class DownloadData extends AsyncTask<String, Void,String> {

        String myXmlData;

        protected String doInBackground(String... urls) { //the ... means it will accept 0 or many urls by deinition with this AsyncTask we can proess more than one thing
            try {
                myXmlData = downloadXML(urls[0]);

            } catch (IOException e) {
                return "Unable to download XML file";
            }

            return "";
        }

        protected void onPostExecute(String result){
            Log.d("OnPostExecute", myXmlData);
            xmlData = myXmlData;

        }

        private String downloadXML(String theUrl) throws IOException { //if there is an error in this method, thorow it back to the calling method and we want that method to deal with it
            int BUFFER_SIZE = 2000;   //20000 chara at a time.
            InputStream is = null;  //the mecanism we will use to do the download

            String xmlContents = ""; //temp container for our data

            try {
                URL url = new URL(theUrl);  //start opening the url the website address
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();  //open a link or a a reference to that website
                conn.setReadTimeout(10000);  //for whatever the reason we cannot download the file we want to close it gracefully. the maximum time to wait for an input stream read before giving up mill sec
                conn.setConnectTimeout(15000); //connection timer same reason as before
                conn.setRequestMethod("GET");  // get is a stanard way to access data for a web browser
                conn.setDoInput(true);          //input data
                int response = conn.getResponseCode();     //get a response and see what happens
                Log.d("DownloadXML", "The response returned is: " + response); //if the response was okay then it would be 200
                is = conn.getInputStream();

                InputStreamReader isr = new InputStreamReader(is);  //read through whatever we send you
                int charRead;
                char[] inputBuffer = new char[BUFFER_SIZE];
                try {
                    while ((charRead = isr.read(inputBuffer)) > 0)
                    {   //Reading through the inputBUffer charRead is the number of characters that have been read by this process
                        String readString = String.copyValueOf(inputBuffer, 0, charRead);//go through the array and we start at 0 and go through however many character have been read

                        xmlContents += readString; //continually add whatever we read to the xmlContents string
                        // and then now that we've read that we want to clear out the inputBuffer
                        inputBuffer = new char[BUFFER_SIZE];
                    }

                    return xmlContents;

                } catch (IOException e) {
                    e.printStackTrace();  //shows where it crashed
                    return null;          // if there was a problem then it wont return anything
                }

            } finally {
                if (is != null)  //no matter what, if there's an error we still want to execute the code that's in here
                    is.close();   //whether there is an error or not, make sure we close this InputStream
            }
        }
    }
}
