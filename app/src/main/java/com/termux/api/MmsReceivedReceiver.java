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
		    if ("text/plain".equals(type)) {
			String path = cursor.getString(cursor.getColumnIndex(Telephony.Mms.Part.TEXT));
			if (path != null) {
			    return path;
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
	return null; // TODO
    }
}
    
