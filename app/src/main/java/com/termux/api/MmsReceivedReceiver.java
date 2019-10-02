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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {
    private static final String TAG = "MmsReceivedReceiver-api";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS"); // TODO share with MMS printouts

    public void onMessageReceived(Context context, Uri messageUri) {
	Log.e(TAG, "onMessageReceived, context="+context+", Uri="+messageUri);
	Cursor cursor = context.getContentResolver().query(messageUri, null, null, null, null);
	try {
	    if (cursor != null && cursor.moveToFirst()) {
		do {
		    int id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID));
		    // TODO why * 1000 ? observation, that's why.
		    long dateReceived = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE)) * 1000;
		    long dateSent = cursor.getLong(cursor.getColumnIndex(Telephony.Mms.DATE_SENT)) * 1000;
		    String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
    		    String addr = getMmsAddr(context, id); // format: <from> <to,to,to,to>
		    String message = getMmsText(context, id);
		    Log.e(TAG, "onMessageReceived, id="+id+", mmsId="+mmsId+", addr="+addr+", message="+message);
		    Log.e(TAG, "onMessageReceived, dateReceived="+dateReceived+", dateSent="+dateSent);

		    String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
		    String destDir = MessageFormat.format("{0}/smsmms", storagePath);
		    new File(destDir).mkdirs();
		    String destPath = destDir + "/spool";
		    TermuxApiLogger.error("writing MMS to filename="+destPath);
		    String msg = DATE_FORMAT.format(dateReceived) + " " + addr + " " + message + "\n";
		    try {
			File file = new File(destPath);
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
			.setContentTitle("mms: " + addr + ":")
			.setSmallIcon(R.drawable.ic_alert)
			.setStyle(new Notification.BigTextStyle().bigText(message))
			.build();
		    NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
		    notificationManagerCompat.notify(1, notification);
		} while (cursor.moveToNext());
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
    }

    private String getMmsAddr(Context context, int id) {
	String selectionAdd = "msg_id=" + id;
	String uriStr = MessageFormat.format("content://mms/{0,number,#}/addr", id);
	Uri uriAddress = Uri.parse(uriStr);
	Log.e(TAG, "getMmsAddr, uriAddress="+uriAddress);
	Cursor cursor = context.getContentResolver().query(uriAddress, null, selectionAdd, null, null);
	String from = "";
	String to = "";
	try {
	    if (cursor != null && cursor.moveToFirst()) {
		do {
		    String number = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.ADDRESS));
		    String type = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Addr.TYPE));
		    Log.e(TAG, "getMmsAddr, type="+type);
		    Log.e(TAG, "getMmsAddr, number="+number);
		    if (number != null && "151".equals(type)) { // TODO magic 151?
			to += ","+number;
		    }
		    if (number != null && "137".equals(type)) { // TODO magic 137 means from?
			from = number;
		    }
		} while (cursor.moveToNext());
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	if (to.charAt(0) == ',') {
	    to = to.substring(1);
	}
	return from + " => " + to;
    }

    // TODO refactor to return text message and paths to any other attachments that we can save like images and such
    private String getMmsText(Context context, int id) {
	String selectionPart = "mid=" + id;
	Uri uri = Uri.parse("content://mms/part");
	Log.e(TAG, "getMmsText, uri="+uri);
	Cursor cursor = context.getContentResolver().query(uri, null, selectionPart, null, null);
	String message = "";
	
	try {
	    if (cursor != null && cursor.moveToFirst()) {
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
			    message = message + " " + text + " ";
			}
		    }
		    if (type.startsWith("image")) {
			String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
			String destDir = MessageFormat.format("{0}/mms/{1,number,#}", storagePath, id);
			new File(destDir).mkdirs();
			
			String destPath = MessageFormat.format("{0}/mms/{1,number,#}/{2}",
							       storagePath,
							       id,
							       name);
			Log.e(TAG, "getMmsText, destPath="+destPath);

			String partId = cursor.getString(cursor.getColumnIndex("_id"));

			// Let's just cut to the chase and save the thing off to destPath via filestream.
			Uri partURI = Uri.parse("content://mms/part/" + partId);
			InputStream is = null;
			FileOutputStream os = null;
			
			try {
			    is = context.getContentResolver().openInputStream(partURI);
			    os = new FileOutputStream(new File(destPath));
			
			    byte[] buffer = new byte[1024];
			    int length;
			    while ((length = is.read(buffer)) > 0) {
				os.write(buffer, 0, length);
			    }
			} catch(IOException ioe) {
			    TermuxApiLogger.error("Failed to copy image from "+partURI+" to "+destPath);
			    ioe.printStackTrace();
			} finally {
			    if (os != null) {
				try {
				    os.close();
				} catch (IOException e) {}
			    }
			    if (is != null) {
				try {
				    is.close();
				} catch (IOException e) {}
			    }
			}
			message += " file://" + destPath + " ";
		    }
		}  while (cursor.moveToNext());
	    }
	} finally {
	    if (cursor != null) {
		cursor.close();
	    }
	}
	return message;
    }

    private static void copyFileUsingStream(File source, File dest) throws IOException {
	InputStream is = null;
	OutputStream os = null;
	try {
	    is = new FileInputStream(source);
	    os = new FileOutputStream(dest);
	    byte[] buffer = new byte[1024];
	    int length;
	    while ((length = is.read(buffer)) > 0) {
		os.write(buffer, 0, length);
	    }
	} catch(Exception e) {
	    e.printStackTrace();
	    Log.e(TAG, "copyFileUsingStream() failed: "+e);
	} finally {
	    is.close();
	    os.close();
	}
    }    
    
    public void onError(Context context, String error) {
 	Log.e(TAG, "onError, context="+context+", error="+error);
	String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
	String destDir = MessageFormat.format("{0}/smsmms", storagePath);
	new File(destDir).mkdirs();
	String destPath = destDir + "/spool";
	TermuxApiLogger.error("writing MMS to destPath="+destPath);
	String msg = " ERROR: " + error + "\n";
	try {
	    File file = new File(destPath);
	    FileWriter writer = new FileWriter(file, true);
	    writer.write(msg);
	    writer.close();
	} catch (IOException ioe) {
	    TermuxApiLogger.error("Failed to write msg: "+msg);
	    ioe.printStackTrace();
	}
	
	// and notify
	Notification notification = new Notification.Builder(context)
	    .setContentText(msg)
	    .setContentTitle("mms: " + msg)
	    .setSmallIcon(R.drawable.ic_alert)
	    .setStyle(new Notification.BigTextStyle().bigText(msg))
	    .build();
	NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(context);
	notificationManagerCompat.notify(1, notification);
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
    
