package com.tinfinite.ui.views;

import android.content.Context;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.tinfinite.android.sdk.T8Log;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.Emoji;
import org.telegram.messenger.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/31.
 */
public class SimpleInputView extends LinearLayout{
    @InjectView(R.id.input_view_et)
    public EditText et;
    @InjectView(R.id.input_view_send)
    public ImageView sendButton;

    private String postID;

    private SendButtonDelegate delegate;

    public SimpleInputView(Context context) {
        super(context);
        init();
    }

    public SimpleInputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SimpleInputView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setPostID(String postID){
        this.postID = postID;
    }

    private void init() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_input, this, false);
        ButterKnife.inject(this, view);
        addView(view);

        et.addTextChangedListener(new TextWatcher() {
            int count;
            @Override
            public void beforeTextChanged(CharSequence editable, int start, int count, int after) {
                T8Log.PAN_JIA_FANG.d(editable.toString());
            }

            @Override
            public void onTextChanged(CharSequence editable, int start, int before, int count) {
                this.count = count;
//                T8Log.PAN_JIA_FANG.d(editable.toString());
//                Emoji.replaceEmoji(editable, et.getPaint().getFontMetricsInt(), AndroidUtilities.dp(16));
//                SpannableString spannableString = new SpannableString(editable);
//                ImageSpan[] arrayOfImageSpan = spannableString.getSpans(0, spannableString.length(), ImageSpan.class);
//                T8Log.PAN_JIA_FANG.d(String.valueOf(arrayOfImageSpan.length));
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(editable.toString().trim().length() == 0)
                    sendButton.setEnabled(false);
                else {
                    sendButton.setEnabled(true);
                }
            }
        });

        sendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                String content = et.getText().toString();
                if(delegate != null)
                    delegate.sendContent(content);
            }
        });
    }

    public void clearText(){
        et.setText("");
    }

    public void setSendButtonDelegate(SendButtonDelegate delegate){
        this.delegate = delegate;
    }

    public interface SendButtonDelegate{
        public void sendContent(String result);
    }

    public void getTheFocus(){
        et.requestFocus();
        AndroidUtilities.showKeyboard(et);
    }

    public void clearTheFocus(){
        AndroidUtilities.hideKeyboard(et);
    }
}
