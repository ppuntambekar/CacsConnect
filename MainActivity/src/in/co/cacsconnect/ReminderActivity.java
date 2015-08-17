package in.co.cacsconnect;



import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ReminderActivity extends Activity {
    /** Called when the activity is first created. */
	String subject;
	private static Context context;
	Messaging msg = new Messaging();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	subject = extras.getString("Subject");
        }
        context = getApplicationContext();
		setContentView(R.layout.reminder);
		
		msg.isReminder = true;
		Messaging.tryFetchingData task = msg.new tryFetchingData();
		task.nickText = (TextView) findViewById(R.id.textView2);
		task.execute(Messaging.fetchDataUrl+"Reminder"+subject+".txt");
		
		
		// Setting up onClickListeners for send button
		Button loginButton = (Button) findViewById(R.id.reminderButton);
		loginButton.setOnClickListener(reminderListener);

		// start group message sync
		

    }
    
    
    
	private OnClickListener reminderListener = new OnClickListener() {
		public void onClick(View v) {
			
			Date date = new Date();
			String lines[] = msg.reminderMessage.split("\\r?\\n");
			for(int i =0 ; i< lines.length; i++){
				String values[] = lines[i].split(";");
				if(values.length == 4){
					String title = values[0];
					String desc = values[1];
					String startTime = values[2];
					String endTime = values[3];
					SimpleDateFormat formatter = new SimpleDateFormat("EEEE, MM/dd/yyyy HH:mm");
					try {
						Date startDate = formatter.parse(startTime);
						Date endDate = formatter.parse(endTime);
						//pushAppointmentsToCalender( title , desc, "CACS Building", 1, startDate.getTime(), endDate.getTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}

			
			Toast toast = Toast.makeText(ReminderActivity.this, "Reminder has been set.", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER, toast.getXOffset() / 2, toast.getYOffset() / 2);
			toast.show();
		}
	};

	 public static long pushAppointmentsToCalender(String title, String addInfo, String place, int status, long startDate, long endDate) {
		    /***************** Event: note(without alert) *******************/

		    String eventUriString = "content://com.android.calendar/events";
		    Uri EVENTS_URI = Uri.parse(eventUriString);
		    
		    ContentValues eventValues = new ContentValues();

		    eventValues.put("calendar_id", 1); // id, We need to choose from
		                                        // our mobile for primary
		                                        // its 1
		    eventValues.put(Events.TITLE, title);
		    eventValues.put("description", addInfo);
		    eventValues.put("eventLocation", place);



		    eventValues.put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startDate);
		    eventValues.put(CalendarContract.EXTRA_EVENT_END_TIME, endDate);
		    eventValues.put("eventTimezone", TimeZone.getDefault().getID());
		    
		    eventValues.put("eventStatus", status); // This information is
		    // sufficient for most
		    // entries tentative (0),
		    // confirmed (1) or canceled
		    // (2):

		    Log.d("details **" ,  "before ******");
		    Uri eventUri = context.getContentResolver().insert(EVENTS_URI, eventValues);
		    if(eventUri == null){
		    	Log.d("details **" ,  "is null");
		    }else{
		    	Log.d("details **" ,  context.getContentResolver().toString());
		    }
		    long eventID = Long.parseLong(eventUri.getLastPathSegment());

	        String reminderUriString = "content://com.android.calendar/reminders";
	        Uri REMINDER_URI = Uri.parse(reminderUriString);
	        
	        ContentValues reminderValues = new ContentValues();

	        reminderValues.put("event_id", eventID);
	        reminderValues.put("minutes", 5); // Default value of the
	                                            // system. Minutes is a
	                                            // integer
	        reminderValues.put("method", 1); // Alert Methods: Default(0),
	                                            // Alert(1), Email(2),
	                                            // SMS(3)

	        Uri reminderUri = context.getContentResolver().insert(REMINDER_URI, reminderValues);


		    return eventID;

		}
	




}
