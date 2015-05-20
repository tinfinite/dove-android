/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2014.
 */

package org.telegram.ui.Cells;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Html;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;

import com.tinfinite.android.sdk.T8Log;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.ImageReceiver;
import org.telegram.android.LocaleController;
import org.telegram.android.MessageObject;
import org.telegram.android.Emoji;
import org.telegram.android.LocaleController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.TLRPC;
import org.telegram.android.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.ui.Components.AvatarDrawable;
import org.telegram.ui.Components.ResourceLoader;
import org.telegram.ui.Components.StaticLayoutEx;

public class ChatBaseCell extends BaseCell {

    public static interface ChatBaseCellDelegate {
        public abstract void didPressedUserAvatar(ChatBaseCell cell, TLRPC.User user);
        public abstract void didLongPressedUserAvatar(ChatBaseCell cell);
        public abstract void didPressedCancelSendButton(ChatBaseCell cell);
        public abstract void didLongPressed(ChatBaseCell cell);
        public abstract boolean canPerformActions();
        void didPressReplyMessage(ChatBaseCell cell, int id);
        void didPressUrl(String url);
    }

    protected class MyPath extends Path {

        private StaticLayout currentLayout;
        private int currentLine;
        private float lastTop = -1;

        public void setCurrentLayout(StaticLayout layout, int start) {
            currentLayout = layout;
            currentLine = layout.getLineForOffset(start);
            lastTop = -1;
        }

        @Override
        public void addRect(float left, float top, float right, float bottom, Direction dir) {
            if (lastTop == -1) {
                lastTop = top;
            } else if (lastTop != top) {
                lastTop = top;
                currentLine++;
            }
            float lineRight = currentLayout.getLineRight(currentLine);
            float lineLeft = currentLayout.getLineLeft(currentLine);
            if (left >= lineRight) {
                return;
            }
            if (right > lineRight) {
                right = lineRight;
            }
            if (left < lineLeft) {
                left = lineLeft;
            }
            super.addRect(left, top, right, bottom, dir);
        }
    }

    protected ClickableSpan pressedLink;
    protected boolean linkPreviewPressed;
    protected MyPath urlPath = new MyPath();
    protected static Paint urlPaint;

    public boolean isChat = false;
    protected boolean isPressed = false;
    protected boolean forwardName = false;
    protected boolean isHighlighted = false;
    protected boolean media = false;
    protected boolean isCheckPressed = true;
    private boolean wasLayout = false;
    protected boolean isAvatarVisible = false;
    protected boolean drawBackground = true;
    protected MessageObject currentMessageObject;

    private static TextPaint timePaintIn;
    private static TextPaint timePaintOut;
    private static TextPaint timeMediaPaint;
    private static TextPaint namePaint;
    private static TextPaint forwardNamePaint;
    protected static TextPaint replyNamePaint;
    protected static TextPaint replyTextPaint;
    protected static Paint replyLinePaint;

    protected int backgroundWidth = 100;

    protected int layoutWidth;
    protected int layoutHeight;

    private ImageReceiver avatarImage;
    private AvatarDrawable avatarDrawable;
    private boolean avatarPressed = false;
    private boolean forwardNamePressed = false;
    private boolean longPressed = false;

    private StaticLayout replyNameLayout;
    private StaticLayout replyTextLayout;
    private ImageReceiver replyImageReceiver;
    private int replyStartX;
    private int replyStartY;
    protected int replyNameWidth;
    private float replyNameOffset;
    protected int replyTextWidth;
    private float replyTextOffset;
    private boolean needReplyImage = false;
    private boolean replyPressed = false;
    private TLRPC.FileLocation currentReplyPhoto;

    private StaticLayout nameLayout;
    protected int nameWidth;
    private float nameOffsetX = 0;
    protected boolean drawName = true;

    private StaticLayout forwardedNameLayout;
    protected int forwardedNameWidth;
    protected boolean drawForwardedName = false;
    private int forwardNameX;
    private int forwardNameY;
    private float forwardNameOffsetX = 0;

    protected StaticLayout timeLayout;
    protected int timeWidth;
    private int timeX;
    private TextPaint currentTimePaint;
    private String currentTimeString;
    protected boolean drawTime = true;

    private TLRPC.User currentUser;
    private TLRPC.FileLocation currentPhoto;
    private String currentNameString;

    private TLRPC.User currentForwardUser;
    private String currentForwardNameString;

    protected ChatBaseCellDelegate delegate;

    protected int namesOffset = 0;

    private int last_send_state = 0;
    private int last_delete_date = 0;
    private int last_vote_value = 0;

    public ChatBaseCell(Context context) {
        super(context);
        if (timePaintIn == null) {
            timePaintIn = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintIn.setTextSize(AndroidUtilities.dp(12));
            timePaintIn.setColor(0xffa1aab3);

            timePaintOut = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timePaintOut.setTextSize(AndroidUtilities.dp(12));
            timePaintOut.setColor(0xff70b15c);

            timeMediaPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            timeMediaPaint.setTextSize(AndroidUtilities.dp(12));
            timeMediaPaint.setColor(0xffffffff);

            namePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            namePaint.setTextSize(AndroidUtilities.dp(14));
            namePaint.setColor(0xffa1aab3);

            forwardNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            forwardNamePaint.setTextSize(AndroidUtilities.dp(14));

            replyNamePaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            replyNamePaint.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            replyNamePaint.setTextSize(AndroidUtilities.dp(14));

            replyTextPaint = new TextPaint(TextPaint.ANTI_ALIAS_FLAG);
            replyTextPaint.setTextSize(AndroidUtilities.dp(14));
            replyTextPaint.linkColor = 0xff316f9f;

            replyLinePaint = new Paint();
        }
        avatarImage = new ImageReceiver(this);
        avatarImage.setRoundRadius(AndroidUtilities.dp(21));
        avatarDrawable = new AvatarDrawable();
        replyImageReceiver = new ImageReceiver(this);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        avatarImage.onDetachedFromWindow();
        replyImageReceiver.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        avatarImage.onAttachedToWindow();
        replyImageReceiver.onAttachedToWindow();
    }

    @Override
    public void setPressed(boolean pressed) {
        super.setPressed(pressed);
        invalidate();
    }

    protected void resetPressedLink() {
        if (pressedLink != null) {
            pressedLink = null;
        }
        linkPreviewPressed = false;
        invalidate();
    }

    public void setDelegate(ChatBaseCellDelegate delegate) {
        this.delegate = delegate;
    }

    public void setHighlighted(boolean value) {
        if (isHighlighted == value) {
            return;
        }
        isHighlighted = value;
        invalidate();
    }

    public void setCheckPressed(boolean value, boolean pressed) {
        isCheckPressed = value;
        isPressed = pressed;
        invalidate();
    }

    protected boolean isUserDataChanged() {
        if (currentMessageObject == null || currentUser == null) {
            return false;
        }
        if (last_send_state != currentMessageObject.messageOwner.send_state) {
            return true;
        }
        if (last_delete_date != currentMessageObject.messageOwner.destroyTime) {
            return true;
        }
        if (last_vote_value != currentMessageObject.messageOwner.voteValue)
            return true;

        TLRPC.User newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.from_id);
        TLRPC.FileLocation newPhoto = null;

        if (isAvatarVisible && newUser != null && newUser.photo != null) {
            newPhoto = newUser.photo.photo_small;
        }

        if (replyTextLayout == null && currentMessageObject.replyMessageObject != null) {
            return true;
        }

        if (currentPhoto == null && newPhoto != null || currentPhoto != null && newPhoto == null || currentPhoto != null && newPhoto != null && (currentPhoto.local_id != newPhoto.local_id || currentPhoto.volume_id != newPhoto.volume_id)) {
            return true;
        }

        TLRPC.FileLocation newReplyPhoto = null;

        if (currentMessageObject.replyMessageObject != null) {
            TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(currentMessageObject.replyMessageObject.photoThumbs, 80);
            if (photoSize != null && currentMessageObject.replyMessageObject.type != 13) {
                newReplyPhoto = photoSize.location;
            }
        }

        if (currentReplyPhoto == null && newReplyPhoto != null) {
            return true;
        }

        String newNameString = null;
        if (drawName && isChat && newUser != null && !currentMessageObject.isOut()) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }

        if (currentNameString == null && newNameString != null || currentNameString != null && newNameString == null || currentNameString != null && newNameString != null && !currentNameString.equals(newNameString)) {
            return true;
        }

        newUser = MessagesController.getInstance().getUser(currentMessageObject.messageOwner.fwd_from_id);
        newNameString = null;
        if (newUser != null && drawForwardedName && currentMessageObject.messageOwner.fwd_from_id != 0) {
            newNameString = ContactsController.formatName(newUser.first_name, newUser.last_name);
        }
        return currentForwardNameString == null && newNameString != null || currentForwardNameString != null && newNameString == null || currentForwardNameString != null && newNameString != null && !currentForwardNameString.equals(newNameString);
    }

    protected void measureTime(MessageObject messageObject) {
        if (!media) {
            if (messageObject.isOut()) {
                currentTimePaint = timePaintOut;
            } else {
                currentTimePaint = timePaintIn;
            }
        } else {
            currentTimePaint = timeMediaPaint;
        }
        currentTimeString = LocaleController.formatterDay.format((long) (messageObject.messageOwner.date) * 1000);
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));
    }

    public void setMessageObject(MessageObject messageObject) {
        currentMessageObject = messageObject;
        last_send_state = messageObject.messageOwner.send_state;
        last_delete_date = messageObject.messageOwner.destroyTime;
        last_vote_value = messageObject.messageOwner.voteValue;
        isPressed = false;
        isCheckPressed = true;
        isAvatarVisible = false;
        wasLayout = false;
        replyNameLayout = null;
        replyTextLayout = null;
        replyNameWidth = 0;
        replyTextWidth = 0;
        currentReplyPhoto = null;

        currentUser = MessagesController.getInstance().getUser(messageObject.messageOwner.from_id);
//        if (isChat && !messageObject.isOut()) {// 单聊群聊都可以显示对方头像
//        if (!messageObject.isOut()) {//显示我的头像
        if (true) {
            isAvatarVisible = true;
            if (currentUser != null) {
                if (currentUser.photo != null) {
                    currentPhoto = currentUser.photo.photo_small;
                } else {
                    currentPhoto = null;
                }
                avatarDrawable.setInfo(currentUser);
            } else {
                currentPhoto = null;
                avatarDrawable.setInfo(messageObject.messageOwner.from_id, null, null, false);
            }
            avatarImage.setImage(currentPhoto, "50_50", avatarDrawable, false);
        }


        if (!media) {
            if (currentMessageObject.isOut()) {
                currentTimePaint = timePaintOut;
            } else {
                currentTimePaint = timePaintIn;
            }
        } else {
            currentTimePaint = timeMediaPaint;
        }

        currentTimeString = LocaleController.formatterDay.format((long) (currentMessageObject.messageOwner.date) * 1000);
        int i = Math.abs(currentMessageObject.messageOwner.voteValue);
        if (i == 1){
            currentTimeString = currentMessageObject.messageOwner.voteValue+""+LocaleController.getString("Vote", R.string.Vote)+"   "+currentTimeString;
        } else if (i > 1){
            currentTimeString = currentMessageObject.messageOwner.voteValue+""+LocaleController.getString("Votes", R.string.Votes)+"   "+currentTimeString;
        }
        timeWidth = (int)Math.ceil(currentTimePaint.measureText(currentTimeString));

        namesOffset = 0;

        if (drawName && isChat && currentUser != null && !currentMessageObject.isOut()) {
            currentNameString = ContactsController.formatName(currentUser.first_name, currentUser.last_name);
            nameWidth = getMaxNameWidth();

            CharSequence nameStringFinal = TextUtils.ellipsize(currentNameString.replace("\n", " "), namePaint, nameWidth - AndroidUtilities.dp(12), TextUtils.TruncateAt.END);
            nameLayout = new StaticLayout(nameStringFinal, namePaint, nameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (nameLayout.getLineCount() > 0) {
                nameWidth = (int)Math.ceil(nameLayout.getLineWidth(0));
            } else {
                nameWidth = 0;
            }
        } else {
            currentNameString = null;
            nameLayout = null;
            nameWidth = 0;
        }

        if (drawForwardedName && messageObject.isForwarded()) {
            currentForwardUser = MessagesController.getInstance().getUser(messageObject.messageOwner.fwd_from_id);
            if (currentForwardUser != null) {
                currentForwardNameString = ContactsController.formatName(currentForwardUser.first_name, currentForwardUser.last_name);

                forwardedNameWidth = getMaxNameWidth();

                CharSequence str = TextUtils.ellipsize(currentForwardNameString.replace("\n", " "), forwardNamePaint, forwardedNameWidth - AndroidUtilities.dp(40), TextUtils.TruncateAt.END);
                str = AndroidUtilities.replaceTags(String.format("%s\n%s <b>%s</b>", LocaleController.getString("ForwardedMessage", R.string.ForwardedMessage), LocaleController.getString("From", R.string.From), str));
                forwardedNameLayout = StaticLayoutEx.createStaticLayout(str, forwardNamePaint, forwardedNameWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false, TextUtils.TruncateAt.END, forwardedNameWidth, 2);
                if (forwardedNameLayout.getLineCount() > 1) {
                    forwardedNameWidth = Math.max((int) Math.ceil(forwardedNameLayout.getLineWidth(0)), (int) Math.ceil(forwardedNameLayout.getLineWidth(1)));
                    forwardNameOffsetX = Math.min(forwardedNameLayout.getLineLeft(0), forwardedNameLayout.getLineLeft(1));
                } else {
                    forwardedNameWidth = 0;
                }
            } else {
                currentForwardNameString = null;
                forwardedNameLayout = null;
                forwardedNameWidth = 0;
            }
        } else {
            currentForwardNameString = null;
            forwardedNameLayout = null;
            forwardedNameWidth = 0;
        }

        if (messageObject.isReply()) {
            namesOffset += AndroidUtilities.dp(42);
            if (messageObject.contentType == 2 || messageObject.contentType == 3) {
                namesOffset += AndroidUtilities.dp(4);
            } else if (messageObject.contentType == 1) {
                if (messageObject.type == 13) {
                    namesOffset -= AndroidUtilities.dp(42);
                } else {
                    namesOffset += AndroidUtilities.dp(5);
                }
            }

            int maxWidth;
            if (messageObject.type == 13) {
                int width;
                if (AndroidUtilities.isTablet()) {
                    if (AndroidUtilities.isSmallTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        width = AndroidUtilities.displaySize.x;
                    } else {
                        int leftWidth = AndroidUtilities.displaySize.x / 100 * 35;
                        if (leftWidth < AndroidUtilities.dp(320)) {
                            leftWidth = AndroidUtilities.dp(320);
                        }
                        width = AndroidUtilities.displaySize.x - leftWidth;
                    }
                } else {
                    width = AndroidUtilities.displaySize.x;
                }
                if (messageObject.isOut()) {
                    maxWidth = width - backgroundWidth - AndroidUtilities.dp(60);
                } else {
                    maxWidth = width - backgroundWidth - AndroidUtilities.dp(56 + (isChat ? 61 : 0));
                }
            } else {
                maxWidth = getMaxNameWidth() - AndroidUtilities.dp(22);
            }
            if (!media && messageObject.contentType != 0) {
                maxWidth -= AndroidUtilities.dp(8);
            }

            CharSequence stringFinalName = null;
            CharSequence stringFinalText = null;
            if (messageObject.replyMessageObject != null) {
                TLRPC.PhotoSize photoSize = FileLoader.getClosestPhotoSizeWithSize(messageObject.replyMessageObject.photoThumbs, 80);
                if (photoSize == null || messageObject.replyMessageObject.type == 13 || messageObject.type == 13 && !AndroidUtilities.isTablet()) {
                    replyImageReceiver.setImageBitmap((Drawable) null);
                    needReplyImage = false;
                } else {
                    currentReplyPhoto = photoSize.location;
                    replyImageReceiver.setImage(photoSize.location, "50_50", null, true);
                    needReplyImage = true;
                    maxWidth -= AndroidUtilities.dp(44);
                }

                TLRPC.User user = MessagesController.getInstance().getUser(messageObject.replyMessageObject.messageOwner.from_id);
                if (user != null) {
                    stringFinalName = TextUtils.ellipsize(ContactsController.formatName(user.first_name, user.last_name).replace("\n", " "), replyNamePaint, maxWidth - AndroidUtilities.dp(8), TextUtils.TruncateAt.END);
                }
                if (messageObject.replyMessageObject.messageText != null && messageObject.replyMessageObject.messageText.length() > 0) {
                    String mess = messageObject.replyMessageObject.messageText.toString();
                    if (mess.length() > 150) {
                        mess = mess.substring(0, 150);
                    }
                    mess = mess.replace("\n", " ");
                    stringFinalText = Emoji.replaceEmoji(mess, replyTextPaint.getFontMetricsInt(), AndroidUtilities.dp(14));
                    stringFinalText = TextUtils.ellipsize(stringFinalText, replyTextPaint, maxWidth - AndroidUtilities.dp(8), TextUtils.TruncateAt.END);
                }
            }
            if (stringFinalName == null) {
                stringFinalName = LocaleController.getString("Loading", R.string.Loading);
            }
            try {
                replyNameLayout = new StaticLayout(stringFinalName, replyNamePaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                if (replyNameLayout.getLineCount() > 0) {
                    replyNameWidth = (int)Math.ceil(replyNameLayout.getLineWidth(0)) + AndroidUtilities.dp(12 + (needReplyImage ? 44 : 0));
                    replyNameOffset = replyNameLayout.getLineLeft(0);
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
            try {
                if (stringFinalText != null) {
                    replyTextLayout = new StaticLayout(stringFinalText, replyTextPaint, maxWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
                    if (replyTextLayout.getLineCount() > 0) {
                        replyTextWidth = (int) Math.ceil(replyTextLayout.getLineWidth(0)) + AndroidUtilities.dp(12 + (needReplyImage ? 44 : 0));
                        replyTextOffset = replyTextLayout.getLineLeft(0);
                    }
                }
            } catch (Exception e) {
                FileLog.e("tmessages", e);
            }
        }

        requestLayout();
    }

    public final MessageObject getMessageObject() {
        return currentMessageObject;
    }

    protected int getMaxNameWidth() {
        return backgroundWidth - AndroidUtilities.dp(8);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean result = false;
        float x = event.getX();
        float y = event.getY();
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            longPressed = false;
            if (delegate == null || delegate.canPerformActions()) {
                if (isAvatarVisible && avatarImage.isInsideImage(x, y)) {
                    avatarPressed = true;
                    result = true;
                } else if (drawForwardedName && forwardedNameLayout != null) {
                    if (x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + AndroidUtilities.dp(32)) {
                        forwardNamePressed = true;
                        result = true;
                    }
                } else if (currentMessageObject.isReply()) {
                    if (x >= replyStartX && x <= replyStartX + Math.max(replyNameWidth, replyTextWidth) && y >= replyStartY && y <= replyStartY + AndroidUtilities.dp(35)) {
                        replyPressed = true;
                        T8Log.PAN_JIA_FANG.d("click reply message");
                        result = true;
                    }
                }
                if (result) {
                    startCheckLongPress();
                }
            }
        } else {
            if(!longPressed) {
                if (event.getAction() != MotionEvent.ACTION_MOVE) {
                    cancelCheckLongPress();
                }
                if (avatarPressed) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (!longPressed) {
                            avatarPressed = false;
                            playSoundEffect(SoundEffectConstants.CLICK);
                            if (delegate != null) {
                                delegate.didPressedUserAvatar(this, currentUser);
                            }
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (isAvatarVisible && !avatarImage.isInsideImage(x, y)) {
                            avatarPressed = false;
                        }
                    }
                } else if (forwardNamePressed) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        forwardNamePressed = false;
                        playSoundEffect(SoundEffectConstants.CLICK);
                        if (delegate != null) {
                            delegate.didPressedUserAvatar(this, currentForwardUser);
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (!(x >= forwardNameX && x <= forwardNameX + forwardedNameWidth && y >= forwardNameY && y <= forwardNameY + AndroidUtilities.dp(32))) {
                            forwardNamePressed = false;
                        }
                    }
                } else if (replyPressed) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        replyPressed = false;
                        playSoundEffect(SoundEffectConstants.CLICK);
                        if (delegate != null) {
                            delegate.didPressReplyMessage(this, currentMessageObject.messageOwner.reply_to_msg_id);
                        }
                    } else if (event.getAction() == MotionEvent.ACTION_CANCEL) {
                        replyPressed = false;
                    } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        if (!(x >= replyStartX && x <= replyStartX + Math.max(replyNameWidth, replyTextWidth) && y >= replyStartY && y <= replyStartY + AndroidUtilities.dp(35))) {
                            replyPressed = false;
                        }
                    }
                }
            }
        }
        return result;
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (currentMessageObject == null) {
            super.onLayout(changed, left, top, right, bottom);
            return;
        }

        if (changed || !wasLayout) {
            layoutWidth = getMeasuredWidth();
            layoutHeight = getMeasuredHeight();

            timeLayout = new StaticLayout(currentTimeString, currentTimePaint, timeWidth, Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);
            if (!media) {
                if (!currentMessageObject.isOut()) {
//                    timeX = backgroundWidth - AndroidUtilities.dp(9) - timeWidth + (isChat ? AndroidUtilities.dp(52) : 0);//单聊私聊显示对方头像
                    timeX = backgroundWidth - AndroidUtilities.dp(9) - timeWidth + AndroidUtilities.dp(52);
                } else {
//                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(38.5f);
                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(38.5f) - AndroidUtilities.dp(52);
                }
            } else {
                if (!currentMessageObject.isOut()) {
//                    timeX = backgroundWidth - AndroidUtilities.dp(4) - timeWidth + (isChat ? AndroidUtilities.dp(52) : 0);//单聊私聊显示对方头像
                    timeX = backgroundWidth - AndroidUtilities.dp(4) - timeWidth + AndroidUtilities.dp(52);
                } else {
//                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(42.0f);//显示我的头像
                    timeX = layoutWidth - timeWidth - AndroidUtilities.dp(42.0f) - AndroidUtilities.dp(52);
                }
            }

            if (isAvatarVisible) {
                //将图片的位置设置到名字下面
//                avatarImage.setImageCoords(AndroidUtilities.dp(6), AndroidUtilities.dp(3), AndroidUtilities.dp(42), AndroidUtilities.dp(42));
                if (!currentMessageObject.isOut())
                    avatarImage.setImageCoords(AndroidUtilities.dp(6), AndroidUtilities.dp(3) + getTopHeightWithoutReply(), AndroidUtilities.dp(42), AndroidUtilities.dp(42));
                else
                    avatarImage.setImageCoords(layoutWidth - AndroidUtilities.dp(52),  AndroidUtilities.dp(3) + getTopHeightWithoutReply(), AndroidUtilities.dp(42), AndroidUtilities.dp(42));
            }

            wasLayout = true;
        }
    }

    protected void onAfterBackgroundDraw(Canvas canvas) {

    }

    /**
     * Edit By PanJiafang
     * 获取名字的高度
     */
    public int getTopHeight(){
        int height = AndroidUtilities.dp(8)+namesOffset;
        if (drawName && nameLayout != null) {
            height += nameLayout.getHeight();
        }
        return height;
    }

    public int getTopHeightWithoutReply(){
        return getTopHeight() - namesOffset;
    }

    /**
     * Edit By PanJiafang
     * 获取转发信息的高度
     */
    public int getForwardedHeight(){
        int height = 0;
        if(drawForwardedName && forwardedNameLayout != null)
            height += forwardedNameLayout.getHeight();
        return height;
    }

    /**
     * Edit By PanJiafang
     * 低于5.0系统版本的手机需要2DP的便宜
     */
    public int getTopOffset(){
        if(Build.VERSION.SDK_INT < 21)
            return AndroidUtilities.dp(2);
        return 0;
    }

    @Override
    protected void onLongPress() {
        longPressed = true;
        if (delegate != null) {
            if(avatarPressed) {
                avatarPressed = false;
                delegate.didLongPressedUserAvatar(this);
            }
            else
                delegate.didLongPressed(this);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (currentMessageObject == null) {
            return;
        }

        if (!wasLayout) {
            requestLayout();
            return;
        }

        //将名字绘制在气泡外面
        if (drawName && nameLayout != null) {
            canvas.save();
            canvas.translate(AndroidUtilities.dp(52) + AndroidUtilities.dp(12) - nameOffsetX, AndroidUtilities.dp(10));
            namePaint.setColor(AvatarDrawable.getNameColorForId(currentUser.id));
            nameLayout.draw(canvas);
            canvas.restore();
        }

        if (isAvatarVisible) {
            avatarImage.draw(canvas);
        }

        Drawable currentBackgroundDrawable = null;
        if (currentMessageObject.isOut()) {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOutSelected;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOutSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableOut;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableOut;
                }
            }
            //Edit By PanJiafang. 聊天气泡放到姓名下面
            if(currentMessageObject.isReply())
                setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : AndroidUtilities.dp(9)) - AndroidUtilities.dp(52), AndroidUtilities.dp(1)+getTopHeight()-namesOffset, backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2)+namesOffset);
            else
                setDrawableBounds(currentBackgroundDrawable, layoutWidth - backgroundWidth - (!media ? 0 : AndroidUtilities.dp(9)) - AndroidUtilities.dp(52), AndroidUtilities.dp(1)+getTopHeight(), backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2));
        } else {
            if (isPressed() && isCheckPressed || !isCheckPressed && isPressed || isHighlighted) {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableInSelected;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableInSelected;
                }
            } else {
                if (!media) {
                    currentBackgroundDrawable = ResourceLoader.backgroundDrawableIn;
                } else {
                    currentBackgroundDrawable = ResourceLoader.backgroundMediaDrawableIn;
                }
            }
//            if (isChat) {//单聊私聊显示对方头像
            if (true) {
                //Edit By PanJiafang. 聊天气泡放到姓名下面
//                setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp(52 + (!media ? 0 : 9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2));
                if(currentMessageObject.isReply())
                    setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp(52 + (!media ? 0 : 9)), AndroidUtilities.dp(1) + getTopHeightWithoutReply(), backgroundWidth, layoutHeight - getTopHeightWithoutReply() - AndroidUtilities.dp(2));
                else
                    setDrawableBounds(currentBackgroundDrawable, AndroidUtilities.dp(52 + (!media ? 0 : 9)), AndroidUtilities.dp(1) + getTopHeight(), backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2));
            } else {
                //Edit By PanJiafang. 聊天气泡放到姓名下面
//                setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1), backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2));
                setDrawableBounds(currentBackgroundDrawable, (!media ? 0 : AndroidUtilities.dp(9)), AndroidUtilities.dp(1) + getTopHeight(), backgroundWidth, layoutHeight - getTopHeight() - AndroidUtilities.dp(2));
            }
        }
        if (drawBackground) {
            currentBackgroundDrawable.draw(canvas);
        }

        onAfterBackgroundDraw(canvas);



        if (drawForwardedName && forwardedNameLayout != null) {
            forwardNameY = AndroidUtilities.dp(10 + (drawName ? 19 : 0));
            if (currentMessageObject.isOut()) {
                forwardNamePaint.setColor(0xff4a923c);
                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(10);
                forwardNameY = AndroidUtilities.dp(18);
            } else {
                forwardNamePaint.setColor(0xff006fc8);
                forwardNameX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(19);
                forwardNameY = AndroidUtilities.dp(10 + (drawName ? 24 : 8));
            }
            canvas.save();
            canvas.translate(forwardNameX - forwardNameOffsetX, forwardNameY);
            forwardedNameLayout.draw(canvas);
            canvas.restore();
        }

        if (currentMessageObject.isReply()) {
            if (currentMessageObject.type == 13) {
                replyLinePaint.setColor(0xffffffff);
                replyNamePaint.setColor(0xffffffff);
                replyTextPaint.setColor(0xffffffff);
                int backWidth;
                if (currentMessageObject.isOut()) {
                    backWidth = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(32);
                    replyStartX = currentBackgroundDrawable.getBounds().left - AndroidUtilities.dp(9) - backWidth;
                } else {
                    backWidth = getWidth() - currentBackgroundDrawable.getBounds().right - AndroidUtilities.dp(32);
                    replyStartX = currentBackgroundDrawable.getBounds().right + AndroidUtilities.dp(23);
                }
                Drawable back;
                if (ApplicationLoader.isCustomTheme()) {
                    back = ResourceLoader.backgroundBlack;
                } else {
                    back = ResourceLoader.backgroundBlue;
                }
                replyStartY = layoutHeight - AndroidUtilities.dp(58);
                back.setBounds(replyStartX - AndroidUtilities.dp(7), replyStartY - AndroidUtilities.dp(6), replyStartX - AndroidUtilities.dp(7) + backWidth, replyStartY + AndroidUtilities.dp(41));
                back.draw(canvas);
            } else {
                if (currentMessageObject.isOut()) {
                    replyLinePaint.setColor(0xff8dc97a);
                    replyNamePaint.setColor(0xff61a349);
                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
                        replyTextPaint.setColor(0xff000000);
                    } else {
                        replyTextPaint.setColor(0xff70b15c);
                    }
                    replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
                } else {
                    replyLinePaint.setColor(0xff6c9fd2);
                    replyNamePaint.setColor(0xff377aae);
                    if (currentMessageObject.replyMessageObject != null && currentMessageObject.replyMessageObject.type == 0) {
                        replyTextPaint.setColor(0xff000000);
                    } else {
                        replyTextPaint.setColor(0xff999999);
                    }
                    if (currentMessageObject.contentType == 1 && media) {
                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(11);
                    } else {
                        replyStartX = currentBackgroundDrawable.getBounds().left + AndroidUtilities.dp(20);
                    }
                }
                replyStartY = AndroidUtilities.dp(12 + (drawForwardedName && forwardedNameLayout != null ? 36 : 0)) + getTopHeightWithoutReply();
            }
            canvas.drawRect(replyStartX, replyStartY, replyStartX + AndroidUtilities.dp(2), replyStartY + AndroidUtilities.dp(35), replyLinePaint);
            if (needReplyImage) {
                replyImageReceiver.setImageCoords(replyStartX + AndroidUtilities.dp(10), replyStartY, AndroidUtilities.dp(35), AndroidUtilities.dp(35));
                replyImageReceiver.draw(canvas);
            }
            if (replyNameLayout != null) {
                canvas.save();
                canvas.translate(replyStartX - replyNameOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY);
                replyNameLayout.draw(canvas);
                canvas.restore();
            }
            if (replyTextLayout != null) {
                canvas.save();
                canvas.translate(replyStartX - replyTextOffset + AndroidUtilities.dp(10 + (needReplyImage ? 44 : 0)), replyStartY + AndroidUtilities.dp(19));
                replyTextLayout.draw(canvas);
                canvas.restore();
            }
        }

        if (drawTime || !media) {
            if (media) {
                setDrawableBounds(ResourceLoader.mediaBackgroundDrawable, timeX - AndroidUtilities.dp(3), layoutHeight - AndroidUtilities.dp(27.5f), timeWidth + AndroidUtilities.dp(6 + (currentMessageObject.isOut() ? 20 : 0)), AndroidUtilities.dp(16.5f));
                ResourceLoader.mediaBackgroundDrawable.draw(canvas);

                canvas.save();
                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(12.0f) -timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            } else {
                canvas.save();
                canvas.translate(timeX, layoutHeight - AndroidUtilities.dp(6.5f) - timeLayout.getHeight());
                timeLayout.draw(canvas);
                canvas.restore();
            }

            if (currentMessageObject.isOut()) {
                boolean drawCheck1 = false;
                boolean drawCheck2 = false;
                boolean drawClock = false;
                boolean drawError = false;
                boolean isBroadcast = (int)(currentMessageObject.getDialogId() >> 32) == 1;

                if (currentMessageObject.isSending()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = true;
                    drawError = false;
                } else if (currentMessageObject.isSendError()) {
                    drawCheck1 = false;
                    drawCheck2 = false;
                    drawClock = false;
                    drawError = true;
                } else if (currentMessageObject.isSent()) {
                    if (!currentMessageObject.isUnread()) {
                        drawCheck1 = true;
                        drawCheck2 = true;
                    } else {
                        drawCheck1 = false;
                        drawCheck2 = true;
                    }
                    drawClock = false;
                    drawError = false;
                }

                if (drawClock) {
                    if (!media) {
                        setDrawableBounds(ResourceLoader.clockDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.clockDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.clockDrawable.getIntrinsicHeight());
//                        setDrawableBounds(clockDrawable, layoutWidth - backgroundWidth - clockDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - clockDrawable.getIntrinsicHeight());
                        ResourceLoader.clockDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(ResourceLoader.clockMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.clockMediaDrawable.getIntrinsicHeight());
//                        setDrawableBounds(clockMediaDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(4) - clockMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - clockMediaDrawable.getIntrinsicHeight());
                        ResourceLoader.clockMediaDrawable.draw(canvas);
                    }
                }
                if (isBroadcast) {
                    if (drawCheck1 || drawCheck2) {
                        if (!media) {
                            setDrawableBounds(ResourceLoader.broadcastDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.broadcastDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(8.0f) - ResourceLoader.broadcastDrawable.getIntrinsicHeight());
//                            setDrawableBounds(broadcastDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(2) - broadcastDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.0f) - broadcastDrawable.getIntrinsicHeight());
                            ResourceLoader.broadcastDrawable.draw(canvas);
                        } else {
                            setDrawableBounds(ResourceLoader.broadcastMediaDrawable, layoutWidth - AndroidUtilities.dp(24.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.broadcastMediaDrawable.getIntrinsicHeight());
//                            setDrawableBounds(broadcastMediaDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(6) - broadcastMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - broadcastMediaDrawable.getIntrinsicHeight());
                            ResourceLoader.broadcastMediaDrawable.draw(canvas);
                        }
                    }
                } else {
                    if (drawCheck2) {
                        if (!media) {
                            if (drawCheck1) {
                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(22.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
//                                setDrawableBounds(checkDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(4) - checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - checkDrawable.getIntrinsicHeight());
                            } else {
                                setDrawableBounds(ResourceLoader.checkDrawable, layoutWidth - AndroidUtilities.dp(18.5f) - ResourceLoader.checkDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.checkDrawable.getIntrinsicHeight());
//                                setDrawableBounds(checkDrawable, layoutWidth - backgroundWidth - checkDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - checkDrawable.getIntrinsicHeight());
                            }
                            ResourceLoader.checkDrawable.draw(canvas);
                        } else {
                            if (drawCheck1) {
                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(26.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
//                                setDrawableBounds(checkMediaDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(8) - checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - checkMediaDrawable.getIntrinsicHeight());
                            } else {
                                setDrawableBounds(ResourceLoader.checkMediaDrawable, layoutWidth - AndroidUtilities.dp(22.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.checkMediaDrawable.getIntrinsicHeight());
//                                setDrawableBounds(checkMediaDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(4) - checkMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - checkMediaDrawable.getIntrinsicHeight());
                            }
                            ResourceLoader.checkMediaDrawable.draw(canvas);
                        }
                    }
                    if (drawCheck1) {
                        if (!media) {
                            setDrawableBounds(ResourceLoader.halfCheckDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.halfCheckDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(8.5f) - ResourceLoader.halfCheckDrawable.getIntrinsicHeight());
//                            setDrawableBounds(halfCheckDrawable, layoutWidth - backgroundWidth - halfCheckDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(8.5f) - halfCheckDrawable.getIntrinsicHeight());
                            ResourceLoader.halfCheckDrawable.draw(canvas);
                        } else {
                            setDrawableBounds(ResourceLoader.halfCheckMediaDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(13.0f) - ResourceLoader.halfCheckMediaDrawable.getIntrinsicHeight());
//                            setDrawableBounds(halfCheckMediaDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(2) - halfCheckMediaDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(13.0f) - halfCheckMediaDrawable.getIntrinsicHeight());
                            ResourceLoader.halfCheckMediaDrawable.draw(canvas);
                        }
                    }
                }
                if (drawError) {
                    if (!media) {
                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(18) - ResourceLoader.errorDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(6.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
//                        setDrawableBounds(errorDrawable, layoutWidth - backgroundWidth - errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(6.5f) - errorDrawable.getIntrinsicHeight());
                        ResourceLoader.errorDrawable.draw(canvas);
                    } else {
                        setDrawableBounds(ResourceLoader.errorDrawable, layoutWidth - AndroidUtilities.dp(20.5f) - ResourceLoader.errorDrawable.getIntrinsicWidth() - AndroidUtilities.dp(52), layoutHeight - AndroidUtilities.dp(12.5f) - ResourceLoader.errorDrawable.getIntrinsicHeight());
//                        setDrawableBounds(errorDrawable, layoutWidth - backgroundWidth - AndroidUtilities.dp(2) - errorDrawable.getIntrinsicWidth(), layoutHeight - AndroidUtilities.dp(12.5f) - errorDrawable.getIntrinsicHeight());
                        ResourceLoader.errorDrawable.draw(canvas);
                    }
                }
            }
        }
    }
}
