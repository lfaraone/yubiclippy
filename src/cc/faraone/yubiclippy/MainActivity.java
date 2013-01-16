package cc.faraone.yubiclippy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
    String currentOtp;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    protected void onResume() {
        super.onResume();
        
        Log.i("init", "registering");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        ndef.addDataScheme("https");
        NfcAdapter.getDefaultAdapter(this).enableForegroundDispatch(this, pendingIntent, new IntentFilter[] {ndef}, null);
    }
    
    protected void onPause() {
        super.onPause();
        NfcAdapter.getDefaultAdapter(this).disableForegroundDispatch(this);
    }
    
    
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        
        TextView dataView = (TextView)findViewById(R.id.textView1);
        currentOtp = getOtpFromIntent(intent);
        dataView.setText(currentOtp);
        
        Button copyButton = (Button)findViewById(R.id.button1);
        if (!copyButton.isEnabled()) {
            copyButton.setEnabled(true);
            copyButton.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    ClipboardManager clipboard = (ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    
                    ClipData clip = ClipData.newPlainText("YubiKey One Time Password", currentOtp);
                    clipboard.setPrimaryClip(clip);
                    
                }});
        }
        
    }
    
    /**
     * Extracts a YubiKey-style OTP from an intent.
     * 
     * @param intent The intent we should extract the OTP from.
     * @return the OTP string or null if not found
     * XXX: Perhaps we should raise an exception.
     */
    private String getOtpFromIntent(Intent intent) {
        Pattern otpPattern = Pattern.compile("^.*([cbdefghijklnrtuv]{44})$");
        String data = intent.getDataString();
        Matcher matcher = otpPattern.matcher(data);
        if (matcher.matches()) {
            return matcher.group(1);
        } else {
            return null;
        }
    }

}
