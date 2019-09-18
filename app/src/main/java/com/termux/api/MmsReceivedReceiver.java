package com.termux.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import android.util.Log;

public class MmsReceivedReceiver extends com.klinker.android.send_message.MmsReceivedReceiver {
    private static final String TAG = "MmsReceivedReceiver-api";

    public void onMessageReceived(Context context, Uri messageUri) {
	Log.e(TAG, "onMessageReceived, context="+context+", Uri="+messageUri);
	Cursor cursor = context.getContentResolver().query(messageUri, null, null, null, null);
	try {
	    if (cursor.moveToFirst()) {
		do {
		    int id = cursor.getInt(cursor.getColumnIndex(Telephony.Mms._ID));
		    String mmsId = cursor.getString(cursor.getColumnIndex(Telephony.Mms.MESSAGE_ID));
		    String message = getMmsText(context, id);
		    Log.e(TAG, "onMessageReceived, id="+id+", mmsId="+mmsId+", message="+message);
		} while (cursor.moveToNext());
	    }
	} finally {
	    cursor.close();
	}
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
    
