package com.bytepair.stormy;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;

public class AlertDialogFragment extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        // this is the builder for the new error dialog
        Context context = getActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        // here is where we set what will appear in the error dialog
        builder.setTitle(R.string.error_title)
               .setMessage(R.string.error_message)
               .setPositiveButton(R.string.ok_button_text, null);

        // this is the actual alert dialog we are returning
        AlertDialog dialog = builder.create();

        return dialog;
    }

}
