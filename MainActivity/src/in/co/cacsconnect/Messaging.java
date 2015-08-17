package in.co.cacsconnect;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.AllowAllHostnameVerifier;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Messaging extends Activity {
	
	public final String sendDataUrl = "http://23.253.231.192:8080/b/putData.php?value=";
	public static final String fetchDataUrl = "http://23.253.231.192:8080/b/";
	public final int updateFrequency = 500; 

	public static String nick = "default";
	public static String groupMessage = "No messages as of yet !";
	public static String reminderMessage = "";
	public static String userMessage = "";
	public static String subject = "Databases";
	public static String fileName = "messages.txt";
	public boolean isReminder = false;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.messaging);

		
		Bundle extras = getIntent().getExtras();
		nick = extras.getString("nick");
		subject = extras.getString("subject");
		fileName = subject + ".txt";
		
		// Setting up onClickListeners for send button
		Button loginButton = (Button) findViewById(R.id.sendButton);
		loginButton.setOnClickListener(sendButtonListener);

		// start group message sync
		new tryFetchingData().execute(fetchDataUrl+fileName);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	private OnClickListener sendButtonListener = new OnClickListener() {
		public void onClick(View v) {
			EditText userTypedMessage = (EditText) findViewById(R.id.userMessageBox);
			userMessage = userTypedMessage.getText().toString();

			// send user message to server
			new trySendingData().execute(sendDataUrl);

			// Clear the field..
			userTypedMessage.setText("");

		}
	};

	// Asynchronous thread for message update
	public class tryFetchingData extends AsyncTask<String, Integer, String> {

		public TextView nickText = null;
		protected String doInBackground(String... url) {
			Log.d("cust keyboard" , "on press" + url[0]);
			String data = getDataFromServer(url[0], isReminder);
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		protected void onPostExecute(String result) {
			// Update content to the UI..
			if(!isReminder){
				updateMessages();
				// Wait before trying for next update
				Handler myHandler = new Handler();
				myHandler.postDelayed(delayedUpdateLooper, updateFrequency);
			}else{
				Log.d("before details" , "here");
				Log.d("** details" , reminderMessage+"0000");
				
				StringBuffer details = new StringBuffer(); 
				
				String lines[] = reminderMessage.split("\\r?\\n");
				for(int i =0 ; i< lines.length; i++){
					String values[] = lines[i].split(";");
					if(values.length == 4){
						String title = values[0];
						String desc = values[1];
						String startTime = values[2];
						String endTime = values[3];
						details.append(title + ":" + desc + "\n" + "From " + startTime + " To " + endTime + "\n\n");
					}
				}
				nickText.setText(details.toString()); 
				nickText.refreshDrawableState();
				
			}
		}
	}

	// Asynchronous thread for user message sending
	public class trySendingData extends AsyncTask<String, Integer, Long> {

		protected Long doInBackground(String... url) {
			sendDataToServer(url[0]);
			return null;
		}

		protected void onProgressUpdate(Integer... progress) {

		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			disableSendButton();
		}

		protected void onPostExecute(Long result) {
			enableSendButton();
		}
	}

	private Runnable delayedUpdateLooper = new Runnable() {
		@Override
		public void run() {
			Log.d("in looper" ,  "in looper");
			new tryFetchingData().execute(fetchDataUrl + fileName);
		}
	};

	public void disableSendButton() {
		Button sendButton = (Button) this.findViewById(R.id.sendButton);
		sendButton.setEnabled(false);
		sendButton.setText("wait!");
	}

	public void enableSendButton() {
		Button sendButton = (Button) this.findViewById(R.id.sendButton);
		sendButton.setEnabled(true);
		sendButton.setText("send");
	}

	public void updateMessages() {
		TextView groupMessageBox = (TextView) this
				.findViewById(R.id.groupMessageBox);
		groupMessageBox.setText(groupMessage);

	}

	// Send data to server
	public void sendDataToServer(String url) {
		// Making HTTP request
		try {
        	trustAllHosts();
			// defaultHttpClient
			DefaultHttpClient httpClient = new DefaultHttpClient();

    		SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
    			    .getSchemeRegistry().getScheme("https").getSocketFactory();
    		sf.setHostnameVerifier(new AllowAllHostnameVerifier());

			
			String effectiveMessage = getEffectiveMessage(subject + ":" + nick + ": " 
					+ userMessage);
			HttpPost httpPost = new HttpPost(url + effectiveMessage);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			if (httpEntity != null) {
				httpEntity.consumeContent();
			}

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Fetching data from server
	public String getDataFromServer(String url, boolean reminder) {
		String serverResponse = null;
		InputStream is = null;
		// Making HTTP request
		try {
        	trustAllHosts();

			HttpClient httpClient = createHttpClient();
						
    		SSLSocketFactory sf = (SSLSocketFactory)httpClient.getConnectionManager()
    			    .getSchemeRegistry().getScheme("https").getSocketFactory();
    		sf.setHostnameVerifier(new AllowAllHostnameVerifier());
			HttpPost httpPost = new HttpPost(url);

			HttpResponse httpResponse = httpClient.execute(httpPost);
			HttpEntity httpEntity = httpResponse.getEntity();
			is = httpEntity.getContent();

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					is, "iso-8859-1"), 8);
			StringBuilder sb = new StringBuilder();
			String line = null;

			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
			is.close();
			serverResponse = sb.toString();
			if (serverResponse != null && !reminder)
				groupMessage = serverResponse;
			if(reminder){
				reminderMessage = serverResponse;
			}
		} catch (Exception e) {
			Log.e("Buffer Error", "Error converting result " + e.toString());
		}
		return serverResponse;
	}

	
	public static HttpClient createHttpClient()
	{
	    HttpParams params = new BasicHttpParams();
	    HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
	    HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
	    HttpProtocolParams.setUseExpectContinue(params, true);

	    SchemeRegistry schReg = new SchemeRegistry();
	    schReg.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
	    schReg.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
	    ClientConnectionManager conMgr = new ThreadSafeClientConnManager(params, schReg);

	    return new DefaultHttpClient(conMgr, params);
	}
	
    private static void trustAllHosts() {
    	
    	TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
    		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
    			return new java.security.cert.X509Certificate[] {};
    		}

    		public void checkClientTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}

    		public void checkServerTrusted(X509Certificate[] chain,
    				String authType) throws CertificateException {
    		}
    	} };

    	// Install the all-trusting trust manager
    	try {
    		SSLContext sc = SSLContext.getInstance("SSL");
    		sc.init(null, trustAllCerts, new java.security.SecureRandom());
    		
    		HttpsURLConnection
    				.setDefaultSSLSocketFactory(sc.getSocketFactory());
    		
    		HttpsURLConnection.setDefaultHostnameVerifier(org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
	
	
	
	public String getEffectiveMessage(String data) {
		data = data.replaceAll("%", "%25");
		data = data.replaceAll("\\s", "%20");
		data = data.replaceAll("#", "%23");
		data = data.replaceAll("\\{", "%7B");
		data = data.replaceAll("\\|", "%7C");
		data = data.replaceAll("\\}", "%7D");
		data = data.replaceAll("<", "%3C");
		data = data.replaceAll(">", "%3E");
		data = data.replaceAll("\"", "%22");
		data = data.replaceAll("-", "%2D");
		data = data.replaceAll("&", "%26");
		data = data.replaceAll("\\\\", "%5C");
		return data;

	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent intent;
		switch (item.getItemId()) {
		case R.id.menu_id_info:
			intent = new Intent(this, HtmlActivity.class);
			intent.putExtra("Subject", this.subject);
	        this.startActivity(intent);
			break;
		case R.id.menu_id_reminder:
			intent = new Intent(this, ReminderActivity.class);
			intent.putExtra("Subject", this.subject);
	        this.startActivity(intent);
			break;
		}
		
	    return true;        
	}

	
	
}
