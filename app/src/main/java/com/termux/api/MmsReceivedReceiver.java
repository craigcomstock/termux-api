package com.termux.api;

import android.app.Notification;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.app.NotificationManagerCompat;
import android.provider.Telephony;
import android.util.Log;
import com.termux.api.util.TermuxApiLogger;

import java.io.IOException;
import java.io.File;
import java.io.FileWriter;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {
    private static final String TAG = "MmsReceivedReceiver-api";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"); // TODO share with MMS printouts

    public void onMessageReceived(Context context, Uri messageUri) {
	Log.e(TAG, "onMessageReceived, context="+context+", Uri="+messageUri);
	Cursor cursor = context.getContentResolver().query(messageUri, null, null, null, null);
	try {
	    if (cursor.moveToFirst()) {
		do {
		    int id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID));
		    // TODO why * 1000 ? observation, that's why.
		    long dateReceived = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE)) * 1000;
		    long dateSent = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE_SENT)) * 1000;
		    String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
    		    String from = getMmsFrom(context, id);
		    String message = getMmsText(context, id);
		    Log.e(TAG, "onMessageReceived, id="+id+", mmsId="+mmsId+", from="+from+", message="+message);
		    Log.e(TAG, "onMessageReceived, dateReceived="+dateReceived+", dateSent="+dateSent);

		    // now save the message to the termux-smsmms-spool :)
		    String filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/termux-smsmms-spool";
		    TermuxApiLogger.error("writing MMS to filename="+filename);
		    String msg = DATE_FORMAT.format(dateReceived) + " " + from + " " + message + "\n";
		    // TODO add [[image-file-path]] as a sort of org-style inline image reference :)
		    try {
			File file = new File(filename);
			FileWriter writer = new FileWriter(file, true);
			writer.write(msg);
			writer.close();
		    } catch (IOException ioe) {
			TermuxApiLogger.error("Failed to write msg: "+msg);
			ioe.printStackTrace();
		    }

		    // and notify
		    Notification notification = new Notification.Builder(context)
			.setContentText(message)
			.setContentTitle("mms: " + from + ":")
			.setSmallIcon(R.drawable.ic_alert)
			.setStyle(new Notification.BigTextStyle().bigText(message))
			.build();
		    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		    notificationManagerCompat.notify(1, notification);
		} while (cursor.moveToNext());
	    }
	} finally {
	    cursor.close();
	}
    }

    private String getMmsFrom(Context context, int id) {
	String selectionAdd = "msg_id=" + id;
	String uriStr = MessageFormat.format("content://mms/{0}/addr", id);
	Uri uriAddress = Uri.parse(uriStr);
	Cursor cursor = context.getContentResolver().query(uriAddress, null, selectionAdd, null, null);
	String numbers = "";
	try {
	    if (cursor.moveToFirst()) {
		do {
		    String number = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS));
		    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.TYPE));
		    Log.e(TAG, "getMmsAddr, type="+type);
		    Log.e(TAG, "getMmsAddr, number="+number);
		    if (number != null && "151".equals(type)) { // TODO magic 151?
			numbers += ","+number;
		    }
		} while (cursor.moveToNext());
	    }
	} finally {
	    cursor.close();
	}
	if (numbers.charAt(0) == ',') {
	    numbers = numbers.substring(1);
	}
	return numbers;
    }

    private String getMmsText(Context context, int id) {
	String selectionPart = "mid=" + id;
	Uri uri = Uri.parse("content://mms/part");
	Cursor cursor = context.getContentResolver().query(uri, null, selectionPart, null, null);
	try {
	    if (cursor.moveToFirst()) {
		do {
		    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.CONTENT_TYPE));
		    Log.e(TAG, "getMmsText, type="+type);
		    String text = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
		    String _data = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part._DATA));
		    String name = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.NAME));
		    String filename = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.FILENAME));
		    Log.e(TAG, "getMmsText, name="+name);
		    Log.e(TAG, "getMmsText, filename="+filename);
		    Log.e(TAG, "getMmsText, _data="+_data);
		    Log.e(TAG, "getMmsText, text="+text);
		    if ("text/plain".equals(type)) {
			if (text != null) {
			    return text;
			}
		    }
		}  while (cursor.moveToNext());
	    }
	} finally {
	    cursor.close();
	}
	return null;
    }
    
    public void onError(Context context, String error) {
	Log.e(TAG, "onError, context="+context+", error="+error);
    }

    public MmscInformation getMmscInfoForReceptionAck() {
        // Override this and provide the MMSC to send the ACK to.
        // some carriers will download duplicate MMS messages without this ACK. When using the
        // system sending method, apparently Google does not do this for us. Not sure why.
        // You might have to have users manually enter their APN settings if you cannot get them
        // from the system somehow.
	String mmscUrl = "http://mms.msg.eng.t-mobile.com/mms/wapenc";
	String mmsProxy = "";
	int proxyPort = 0;
	// com.klinker.android.send_message.MmsReceivedReceiver.
	return new MmscInformation(mmscUrl, mmsProxy, proxyPort);
    }
}
    
