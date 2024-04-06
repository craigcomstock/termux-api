package com.termux.api.receivers;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.Telephony;
import android.util.Log;

import com.klinker.android.send_message.MmsReceivedReceiver;

import org.w3c.dom.Document;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;


public class MmsReceivedReceiverImpl extends MmsReceivedReceiver {
    private static final String TAG = "MmsReceivedReceiverImpl";

    @Override
    public void onMessageReceived(Context context, Uri messageUri) {

        Log.e(TAG, "onMessageReceived, context="+context+", Uri="+messageUri);
        Cursor cursor = context.getContentResolver().query(messageUri, null, null, null, null);
        try {
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(Telephony.Mms._ID));
                    // TODO why * 1000 ? observation, that's why.
                    long dateReceived = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Mms.DATE)) * 1000;
                    long dateSent = cursor.getLong(cursor.getColumnIndexOrThrow(Telephony.Mms.DATE_SENT)) * 1000;
                    String mmsId = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.MESSAGE_ID));
                    String addr = getMmsAddr(context, id); // format: <from> <to,to,to,to>
                    // TODO from is maybe not quite right?
                    String message = getMmsText(context, id);
                    Log.e(TAG, "onMessageReceived, id="+id+", mmsId="+mmsId+", addr="+addr+", message="+message);
                    Log.e(TAG, "onMessageReceived, dateReceived="+dateReceived+", dateSent="+dateSent);

                    String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    String destDir = MessageFormat.format("{0}/mms", storagePath);
                    new File(destDir).mkdirs();
                    String destPath = destDir + "/spool";
                    Log.e(TAG, "writing MMS to filename="+destPath);
                    String msg = dateReceived + " " + addr + " " + message + "\n";
                    try {
                        File file = new File(destPath);
                        FileWriter writer = new FileWriter(file, true);
                        writer.write(msg);
                        writer.close();
                    } catch (IOException ioe) {
                        Log.e(TAG, "Failed to write msg: "+msg);
                        ioe.printStackTrace();
                    }
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
                    String number = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Addr.ADDRESS));
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Addr.TYPE));
                    Log.e(TAG, "getMmsAddr, type="+type);
                    Log.e(TAG, "getMmsAddr, number="+number);
                    if (number != null && "151".equals(type)) { // TODO magic 151?
                        to += ","+number;
                    }
                    // TODO FIXME, 137 type is NOT FROM, some other info is needed?
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
        return from + " " + to;
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
                    String type = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Part.CONTENT_TYPE));
                    Log.e(TAG, "getMmsText, type="+type);
                    String text = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Part.TEXT));
                    String _data = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Part._DATA));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Part.NAME));
                    String filename = cursor.getString(cursor.getColumnIndexOrThrow(Telephony.Mms.Part.FILENAME));
                    Log.e(TAG, "getMmsText, name="+name);
                    Log.e(TAG, "getMmsText, filename="+filename);
                    Log.e(TAG, "getMmsText, _data="+_data);
                    /*
                    so my vcard got a filename of null, should parse out the end of _data?

E/MmsReceivedReceiverImpl: getMmsText, type=text/x-vcard
E/MmsReceivedReceiverImpl: getMmsText, name=null
E/MmsReceivedReceiverImpl: getMmsText, filename=null
E/MmsReceivedReceiverImpl: getMmsText, _data=/data/user_de/0/com.android.providers.telephony/app_parts/PART_1681441002767_Craig Com.vcf
                     */
                    /* maybe inside of text there is the filename for a vcf (x-vcard) file?
                    04-05 21:08:28.106  3775  3809 E MmsReceivedReceiverImpl: <ref src="Craig Alt.vcf"/>
                     */
                    Log.e(TAG, "getMmsText, text="+text);
                    if ("text/plain".equals(type)) {
                        if (text != null) {
                            message = message + " " + text + " ";
                        }
                    }
                    /*
                    TODO here, we need to be MUCH more nuanced about parsing this business
                    a vcf card will come in with smil and that's where we know the filename
                    and then later as another chunk will be the _data itself
                    so need to associate par sections in SMIL with later chunks of data for that SMIL
                    if ("application/smil".equals(type)) {
                        Log.e(TAG, "CRAIG: need to parse smil/xml");
                        try {
                            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                            DocumentBuilder db = dbf.newDocumentBuilder();
                            Document doc = db.parse(text);
                            XPath xpath = XPathFactory.newInstance().newXPath();
                            XPathExpression xpe = xpath.compile("//smil/body/par/ref[@src]");
                            name = (String) xpe.evaluate(doc, XPathConstants.STRING);
                        } catch( Exception e ) {
                            e.printStackTrace();
                            Log.e(TAG, "CRAIG: problem parsing smil content: " + e.getMessage());
                        }
                    }
                     */
                    if (_data != null) {
                        String storagePath = Environment.getExternalStorageDirectory().getAbsolutePath();
                        String destDir = MessageFormat.format("{0}/mms/{1,number,#}", storagePath, id);
                        new File(destDir).mkdirs();

                        String destPath = MessageFormat.format("{0}/mms/{1,number,#}/{2}",
                                storagePath,
                                id,
                                name);
                        Log.e(TAG, "getMmsText, destPath="+destPath);

                        String partId = cursor.getString(cursor.getColumnIndexOrThrow("_id"));

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
                            Log.e(TAG, "Failed to copy data from "+partURI+" to "+destPath);
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
        String destDir = MessageFormat.format("{0}/mms", storagePath);
        new File(destDir).mkdirs();
        String destPath = destDir + "/spool";
        Log.e(TAG, "writing MMS to destPath="+destPath);
        String msg = " ERROR: " + error + "\n";
        try {
            File file = new File(destPath);
            FileWriter writer = new FileWriter(file, true);
            writer.write(msg);
            writer.close();
        } catch (IOException ioe) {
            Log.e(TAG, "Failed to write msg: "+msg);
            ioe.printStackTrace();
        }
    }
}
