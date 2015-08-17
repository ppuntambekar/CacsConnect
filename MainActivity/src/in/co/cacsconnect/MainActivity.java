package in.co.cacsconnect;


import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Setting up onClickListeners for send button
		Button loginButton = (Button) findViewById(R.id.useThisButton);
		loginButton.setOnClickListener(useThisButtonListener);

		Button resetButton = (Button) findViewById(R.id.resetButton);
		resetButton.setOnClickListener(resetButtonListener);

		SharedPreferences settings = getPreferences(0);
		String nickName = settings.getString("nick", "");
		EditText nickText = (EditText) findViewById(R.id.nameEditText);
		nickText.setText(nickName); 
		
		Spinner spinner = (Spinner) findViewById(R.id.subjects_spinner);
		// Create an ArrayAdapter using the string array and a default spinner layout
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
		        R.array.subjects_array, android.R.layout.simple_spinner_item);
		// Specify the layout to use when the list of choices appears
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Apply the adapter to the spinner
		spinner.setAdapter(adapter);
		
		
	}

	private OnClickListener useThisButtonListener = new OnClickListener() {
		public void onClick(View v) {
			EditText userTypedMessage = (EditText) findViewById(R.id.nameEditText);
			String nick = userTypedMessage.getText().toString();
			
			Spinner spinner = (Spinner) findViewById(R.id.subjects_spinner);
			String subject = spinner.getSelectedItem().toString();
			
			
			SharedPreferences settings = getPreferences(0);
			SharedPreferences.Editor editor = settings.edit();
		    editor.putString("nick", nick);			
		    editor.commit();
			
			if (nick.compareTo("") != 0) {
				startNewIntent(nick, subject);
			}
		}
	};

	private OnClickListener resetButtonListener = new OnClickListener() {
		public void onClick(View v) {
			SharedPreferences settings = getPreferences(0);
			SharedPreferences.Editor editor = settings.edit();
		    editor.remove("nick");		
		    
			EditText nickText = (EditText) findViewById(R.id.nameEditText);
			nickText.setText(""); 
			nickText.refreshDrawableState();
		}
	};

	
	
	public void startNewIntent(String nick, String subject) {
		Intent i = new Intent(this, Messaging.class);
		i.putExtra("nick", nick);
		i.putExtra("subject", subject);
		startActivityForResult(i, 9);
	}	
	
}
