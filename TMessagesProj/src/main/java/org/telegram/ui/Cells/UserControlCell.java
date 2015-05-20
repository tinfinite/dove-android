/*
 * This is the source code of Telegram for Android v. 1.7.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Cells;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tinfinite.entity.JoinRequestEntity;
import com.tinfinite.utils.Utils;

import org.apache.commons.lang3.StringUtils;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ConnectionsManager;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.BackupImageView;

public class UserControlCell extends FrameLayout {
    private static final int RIGHT_MARGIN = 100;
    private BackupImageView avatarImageView;
    private TextView nameTextView;
    private TextView messageTextView;
    private ImageView imageView;
    private TextView ApproveBtn;
    private TextView IgnoreBtn;

    private AvatarDrawable avatarDrawable;

    public UserControlCell(Context context, int padding) {
        super(context);

        avatarImageView = new BackupImageView(context);
        avatarImageView.setRoundRadius(AndroidUtilities.dp(24));
        addView(avatarImageView);
        LayoutParams layoutParams = (LayoutParams) avatarImageView.getLayoutParams();
        layoutParams.width = AndroidUtilities.dp(48);
        layoutParams.height = AndroidUtilities.dp(48);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        layoutParams.leftMargin = LocaleController.isRTL ? 0 : AndroidUtilities.dp(16);
        layoutParams.rightMargin = LocaleController.isRTL ? AndroidUtilities.dp(padding) : 0;
        layoutParams.topMargin = AndroidUtilities.dp(8);
        avatarImageView.setLayoutParams(layoutParams);
        avatarDrawable = new AvatarDrawable();

        nameTextView = new TextView(context);
        nameTextView.setTextColor(0xff212121);
        nameTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 17);
        nameTextView.setLines(1);
        nameTextView.setMaxLines(1);
        nameTextView.setSingleLine(true);
        nameTextView.setEllipsize(TextUtils.TruncateAt.END);
        nameTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(nameTextView);
        layoutParams = (LayoutParams) nameTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? RIGHT_MARGIN : (68));
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? (68) : RIGHT_MARGIN);
        layoutParams.topMargin = AndroidUtilities.dp(10.5f);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        nameTextView.setLayoutParams(layoutParams);

        messageTextView = new TextView(context);
        messageTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
        messageTextView.setLines(1);
        messageTextView.setMaxLines(1);
        messageTextView.setSingleLine(true);
        messageTextView.setEllipsize(TextUtils.TruncateAt.END);
        messageTextView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        addView(messageTextView);
        layoutParams = (LayoutParams) messageTextView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? RIGHT_MARGIN : (68));
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? (68) : RIGHT_MARGIN);
        layoutParams.topMargin = AndroidUtilities.dp(33.5f);
        layoutParams.gravity = LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT;
        messageTextView.setLayoutParams(layoutParams);

        imageView = new ImageView(context);
        imageView.setScaleType(ImageView.ScaleType.CENTER);
        addView(imageView);
        layoutParams = (LayoutParams) imageView.getLayoutParams();
        layoutParams.width = LayoutParams.WRAP_CONTENT;
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        layoutParams.leftMargin = AndroidUtilities.dp(LocaleController.isRTL ? 0 : 16);
        layoutParams.rightMargin = AndroidUtilities.dp(LocaleController.isRTL ? 16 : 0);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL;
        imageView.setLayoutParams(layoutParams);
        imageView.setVisibility(View.GONE);

        LinearLayout buttonContainer = new LinearLayout(context);
        addView(buttonContainer);
        layoutParams = (LayoutParams) buttonContainer.getLayoutParams();
        layoutParams.width = LinearLayout.LayoutParams.WRAP_CONTENT;
        layoutParams.height = AndroidUtilities.dp(60);
        layoutParams.rightMargin = AndroidUtilities.dp(16);
        layoutParams.gravity = (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL;
        buttonContainer.setLayoutParams(layoutParams);

        ApproveBtn = new TextView(context);
        ApproveBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.approve_bg));
        ApproveBtn.setTextColor(getResources().getColor(android.R.color.white));
        ApproveBtn.setText(getResources().getString(R.string.approve_join_group));
        buttonContainer.addView(ApproveBtn);
        LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) ApproveBtn.getLayoutParams();
        lp.width = AndroidUtilities.dp(0);
        lp.weight = 1;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.rightMargin = AndroidUtilities.dp(2);
        ApproveBtn.setLayoutParams(lp);
        ApproveBtn.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));

        IgnoreBtn = new TextView(context);
        IgnoreBtn.setBackgroundDrawable(getResources().getDrawable(R.drawable.ignore_bg));
        IgnoreBtn.setTextColor(getResources().getColor(android.R.color.white));
        IgnoreBtn.setText(getResources().getString(R.string.ignore_join_group));
        buttonContainer.addView(IgnoreBtn);
        lp = (LinearLayout.LayoutParams) IgnoreBtn.getLayoutParams();
        lp.width = AndroidUtilities.dp(0);
        lp.weight = 1;
        lp.height = LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER_VERTICAL;
        lp.leftMargin = AndroidUtilities.dp(2);
        IgnoreBtn.setLayoutParams(lp);
        IgnoreBtn.setPadding(AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8), AndroidUtilities.dp(8));
    }

    public void setData(JoinRequestEntity.RequestEntity entity) {
        nameTextView.setText(entity.getTelegram_username());
        messageTextView.setText(entity.getMessage());

        String key = entity.getTelegram_user_avatar();
        TLRPC.TL_fileLocation file = null;
        if(!StringUtils.isEmpty(key))
            file = Utils.getFileLocation(key);

        avatarImageView.setImage(file, "50_50", getResources().getDrawable(R.drawable.default_profile_img_l));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(64), MeasureSpec.EXACTLY));
    }

    public interface JoinRequestHandler {
        public OnClickListener onApprove();
        public OnClickListener onIgnore();
    }
    public void setButtonOnClickListener(JoinRequestHandler handler) {
        if(handler != null) {
            ApproveBtn.setOnClickListener(handler.onApprove());
            IgnoreBtn.setOnClickListener(handler.onIgnore());
        }
    }

    public void setButtonTag(int index) {
        this.ApproveBtn.setTag(index);
        this.IgnoreBtn.setTag(index);
    }
}
