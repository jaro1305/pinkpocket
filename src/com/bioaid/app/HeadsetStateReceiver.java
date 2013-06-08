package com.bioaid.app;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class HeadsetStateReceiver extends BroadcastReceiver {
    private ProgressDialog dialog;
    private MainActivity mainActivity;

    protected HeadsetStateReceiver(MainActivity paramMainActivity) {
        this.mainActivity = paramMainActivity;
    }

    protected void handleHeadSetEvent(Intent paramIntent) {
        if (paramIntent.getAction()
                .equals("android.intent.action.HEADSET_PLUG"))
            switch (paramIntent.getIntExtra("state", -1)) {
            case 0:
                this.mainActivity.suspend();
                this.dialog = ProgressDialog.show(this.mainActivity, "",
                        "Please insert headphones!", true);
                break;
            case 1:
                this.mainActivity.resume();
                if ((this.dialog != null) && (this.dialog.isShowing())) {
                    this.dialog.dismiss();
                    this.dialog = null;
                }
                break;
            default:
                // Oh noes
            }
    }

    @Override
    public void onReceive(Context paramContext, Intent paramIntent) {
        handleHeadSetEvent(paramIntent);
    }
}