package alex.rxtestproject;

import android.app.Activity;
import android.app.ProgressDialog;

public class ProgressDisplay {

    private ProgressDialog progressDialog;

    ProgressDisplay(Activity activity) {
        progressDialog = new ProgressDialog(activity);
    }

    void showProgress(String text) {
        if (!progressDialog.isShowing()) {
            progressDialog.setTitle(text);
            progressDialog.show();
        }
    }

    void hideProgress() {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
