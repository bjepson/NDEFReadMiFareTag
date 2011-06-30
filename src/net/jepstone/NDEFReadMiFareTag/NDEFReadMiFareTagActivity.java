package net.jepstone.NDEFReadMiFareTag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentFilter.MalformedMimeTypeException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Parcelable;

public class NDEFReadMiFareTagActivity extends Activity {

    private NfcAdapter mAdapter; // The phone's NFC adapter
    private String[][] mTechLists; // Which RFID tag types to look for
    
    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mAdapter = NfcAdapter.getDefaultAdapter(this);
        mPendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        // Setup an intent filter for all MIME based dispatches
        IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED);
        try {
            filter.addDataType("*/*");
        } catch (MalformedMimeTypeException e) {
            throw new RuntimeException("Failed to add data type for */*", e);
        }
        mFilters = new IntentFilter[] {
                filter,
        };
        
        // Setup a tech list for all MiFare Classic tags
        mTechLists = new String[][] { new String[] { MifareClassic.class.getName() } };

    }
    
    @Override
    public void onResume() {
        super.onResume();
        if(mAdapter != null) {
        	mAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, mTechLists);
        }
    }

    /* Called when a tag is found */
    @Override
    public void onNewIntent(Intent intent) {

    	Parcelable[] msgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        
        for(int i=0; i<msgs.length; i++) {
        	NdefRecord[] recs = ( (NdefMessage) msgs[i]).getRecords(); 
        	for (int j = 0; j < recs.length; j++) {
        		String msg = getText(recs[j].getPayload());
        		alertMsg(msg);
        	}
            
        }

    }
    
    private String getText(final byte[] payload) {
        if(payload == null) return null;
        try {
            String textEncoding = ((payload[0] & 0200) == 0) ? "UTF-8" : "UTF-16";
            int languageCodeLength = payload[0] & 0077;
            return new String(payload, languageCodeLength + 1, 
                            payload.length - languageCodeLength - 1, textEncoding);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mAdapter != null) {
        	mAdapter.disableForegroundDispatch(this);
        }
    }

    // Display a dialog
    private void alertMsg(String msg) {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(msg)
    		.setCancelable(false)
    		.setPositiveButton("OK", null);
    	AlertDialog alert = builder.create();
    	alert.show();
    }

}