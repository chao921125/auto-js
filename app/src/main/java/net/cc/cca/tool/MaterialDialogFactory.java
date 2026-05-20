package net.cc.cca.tool;

import android.content.Context;

import com.afollestad.materialdialogs.MaterialDialog;
import net.cc.cca.R;

/**
 * Created by Stardust on 2017/4/18.
 */

public class MaterialDialogFactory {
    public static MaterialDialog createProgress(Context context) {
        return new MaterialDialog.Builder(context)
                .progress(true, 0)
                .cancelable(false)
                .content(R.string.text_processing)
                .build();
    }

    public static MaterialDialog showProgress(Context context) {
        MaterialDialog dialog = createProgress(context);
        dialog.show();
        return dialog;
    }
}
