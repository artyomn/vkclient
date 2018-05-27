package vkclient.vkclient.ui.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import vkclient.vkclient.R;

public class DialogFragmentLocationNeeded extends DialogFragment {

    public interface Listener {
        void onShowLocationRequest();
    }

    private Listener listener;

    public static DialogFragmentLocationNeeded newInstance() {
        DialogFragmentLocationNeeded fragment = new DialogFragmentLocationNeeded();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Listener) context;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(context.getClass().getName() + " must implement DialogFragmentLocationNeeded.Listener");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.label_location_needed)
                .setPositiveButton(android.R.string.ok, (dialogInterface, i) -> listener.onShowLocationRequest());
        return builder.create();
    }

}
