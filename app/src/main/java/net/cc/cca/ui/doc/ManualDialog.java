package net.cc.cca.ui.doc;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import net.cc.cca.R;
import net.cc.cca.databinding.FloatingManualDialogBinding;
import net.cc.cca.ui.widget.EWebView;
import net.cc.cca.ui.doc.DocumentationActivity;

import android.content.Intent;

/**
 * Created by Stardust on 2017/10/24.
 */

public class ManualDialog {

    FloatingManualDialogBinding binding;
    Dialog mDialog;
    private Context mContext;

    public ManualDialog(Context context) {
        mContext = context;
        binding = FloatingManualDialogBinding.inflate(android.view.LayoutInflater.from(context));
        View view = binding.getRoot();
        mDialog = new MaterialDialog.Builder(context)
                .customView(view, false)
                .build();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }


    public ManualDialog title(String title) {
        binding.title.setText(title);
        return this;
    }

    public ManualDialog url(String url) {
        binding.ewebView.getWebView().loadUrl(url);
        return this;
    }

    public ManualDialog pinToLeft(View.OnClickListener listener) {
        binding.pinToLeft.setOnClickListener(v -> {
            mDialog.dismiss();
            listener.onClick(v);
        });
        return this;
    }

    public ManualDialog show() {
        mDialog.show();
        binding.close.setOnClickListener(v -> close());
        binding.fullscreen.setOnClickListener(v -> viewInNewActivity());
        return this;
    }

    void close() {
        mDialog.dismiss();
    }

    void viewInNewActivity() {
        mDialog.dismiss();
        Intent intent = new Intent(mContext, DocumentationActivity.class);
        intent.putExtra(DocumentationActivity.EXTRA_URL, mEWebView.getWebView().getUrl());
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(intent);
    }

}
