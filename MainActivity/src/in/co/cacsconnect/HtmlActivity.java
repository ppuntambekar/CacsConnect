package in.co.cacsconnect;

import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

public class HtmlActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String subject = "";
        WebView webview = new WebView(this);
        setContentView(webview);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	subject = extras.getString("Subject");
        }
        try {
            InputStream fin = getAssets().open(subject+".html");
                byte[] buffer = new byte[fin.available()];
                fin.read(buffer);
                fin.close();
                webview.loadData(new String(buffer), "text/html", "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
