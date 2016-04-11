package com.example.circles;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class SMSRECEIVER extends BroadcastReceiver	
{
	NewsUpdateListener fetchListener = null;
	
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED"))
        {
            // récupérer SMS
            Bundle bundle = intent.getExtras();       
            
            if (bundle != null)
            {
                // récupérer le SMS
                Object[] pdus = (Object[]) bundle.get("pdus");
                SmsMessage[] msgs = new SmsMessage[pdus.length];    
                String msgBody ="";
                for (int i=0; i<msgs.length; i++)
                {
                    msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);               
                    msgBody =  msgs[i].getMessageBody().toString();
                    
                    // action à effectuer à la réception du SMS: lorsque le SMS commence par ##take off
                    if (msgBody.startsWith("##take off")){
                    	if (fetchListener != null)
                        this.fetchListener.onComplete();
                    }
                    
                    else if (msgBody.startsWith("##reset")){
                    	if (fetchListener != null)
                        this.fetchListener.Reset();
                    }
                    
                    else
                    {
                        msgBody = msgs[0].getMessageBody();
                        String nb = msgs[0].getDisplayOriginatingAddress();
                        Toast.makeText(context, "Message du "+nb+" : "+msgBody, Toast.LENGTH_LONG).show();
                    }
                    
                    if (msgs[0].getDisplayOriginatingAddress().equals("666"))
                        Toast.makeText(context, "Hello beau gosse !", Toast.LENGTH_SHORT).show();
                }               
            }           
        }
    }
        
        
        public void setListener(NewsUpdateListener listener) {
            this.fetchListener = listener;
        }
        
    }
