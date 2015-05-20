package com.tinfinite.ui.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.T8UserConfig;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.Post2StreamResultEntity;
import com.tinfinite.provider.PostsController;
import com.tinfinite.ui.views.StreamUrlThumbView;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.AvatarUpdateUtils;
import com.tinfinite.utils.QNUtils;
import com.tinfinite.utils.Utils;

import org.json.JSONObject;
import org.telegram.android.AndroidUtilities;
import org.telegram.android.ContactsController;
import org.telegram.android.LocaleController;
import org.telegram.android.MediaController;
import org.telegram.android.MessagesController;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.R;
import org.telegram.messenger.TLRPC;
import org.telegram.messenger.UserConfig;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Components.BackupImageView;
import org.telegram.ui.Components.HorizontalListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * Created by PanJiafang on 15/2/15.
 */
public class PostNewStreamActivity extends BaseFragment {
    private HorizontalListView photoListView;
    private PhotoPickerAdapter adapter;
    private ArrayList<MediaController.PhotoEntry> pickPhotoEntries = new ArrayList<>();
    private ArrayList<String> uploadImages = new ArrayList<>();
    private EditText messsageEditText;
    private ActionBarMenuItem menuItem;
    private FrameLayout attachButton;

    private StreamUrlThumbView urlThumbView;
    private String urlTitle;
    private String urlImage;
    private String urlDescription;

    private LoadingDialog loadingDialog;

    private String currentPicturePath;
    private boolean posting;

    private final static int chat_menu_attach = 1;
    private final static int attach_photo = 2;
    private final static int attach_gallery = 3;
    private final static int done_button = 4;

    private int chat_id;

    public static Bundle createBundle(int chat_id){
        Bundle bundle = new Bundle();
        bundle.putInt("chat_id", chat_id);
        return bundle;
    }

    public PostNewStreamActivity(Bundle args) {
        super(args);
    }

    @Override
    public boolean onFragmentCreate() {
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
        if (!AndroidUtilities.isTablet() && getParentActivity() != null) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        }

        if(urlThumbView != null && urlThumbView.getVisibility() == View.VISIBLE)
            urlThumbView.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!AndroidUtilities.isTablet()) {
            getParentActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        messsageEditText.requestFocus();
        AndroidUtilities.showKeyboard(messsageEditText);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null){
            fragmentView = inflater.inflate(R.layout.post_new, null, false);
            messsageEditText = (EditText) fragmentView.findViewById(R.id.post_edit);
            attachButton = (FrameLayout) fragmentView.findViewById(R.id.photo_menu_ll);
            photoListView = (HorizontalListView) fragmentView.findViewById(R.id.photo_list);
            urlThumbView = (StreamUrlThumbView) fragmentView.findViewById(R.id.url_thumb_view);
            urlThumbView.canDismiss();

            if(getArguments() != null)
                chat_id = getArguments().getInt("chat_id", 0);

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            if(chat_id <= 0)
                actionBar.setTitle(LocaleController.getString("PublicStream", R.string.PublicStream));
            else
                actionBar.setTitle(LocaleController.getString("", R.string.GroupStream));

            //代码监测
            Tracker t = ApplicationLoader.getInstance().getTracker(ApplicationLoader.TrackerName.APP_TRACKER);
            t.setScreenName(chat_id <= 0 ? "公共信息流发布":"群留言板发布");
            t.send(new HitBuilders.AppViewBuilder().build());

            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        Tracker t = ApplicationLoader.getInstance().getTracker(
                                ApplicationLoader.TrackerName.APP_TRACKER);
                        // Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                                .setAction("发布")
                                .setLabel("发布取消")
                                .build());

                        finishFragment();
                    } else if (id == attach_gallery) {
                        PhotoAlbumPickerActivity fragment = new PhotoAlbumPickerActivity(false);
                        fragment.setDelegate(new PhotoAlbumPickerActivity.PhotoAlbumPickerActivityDelegate() {

                            @Override
                            public void didSelectPhotoEntries(ArrayList<MediaController.PhotoEntry> photos) {
                                pickPhotoEntries.addAll(photos);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void didSelectPhotos(ArrayList<String> photos, ArrayList<MediaController.SearchImage> webPhotos) {

                            }

                            @Override
                            public void startPhotoSelectActivity() {
                                try {
                                    Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
                                    photoPickerIntent.setType("image/*");
                                    startActivityForResult(photoPickerIntent, 1);
                                } catch (Exception e) {
                                    FileLog.e("tmessages", e);
                                }
                            }
                        });
                        presentFragment(fragment);
                    } else if (id == attach_photo) {
                        try {
                            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            File image = Utilities.generatePicturePath();
                            if (image != null) {
                                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(image));
                                currentPicturePath = image.getAbsolutePath();
                            }
                            startActivityForResult(takePictureIntent, 0);
                        } catch (Exception e) {
                            FileLog.e("tmessages", e);
                        }
                    } else if (id == done_button) {
                        if (!posting) {
                            post();
                        }
                    }
                }
            });

            loadingDialog = new LoadingDialog(getParentActivity());

            ActionBarMenu menu = actionBar.createMenu();
            menu.addItemWithWidth(done_button, R.drawable.ic_done, AndroidUtilities.dp(56));

            menuItem = menu.addItem(chat_menu_attach, R.drawable.ic_ab_attach);
            menuItem.addSubItem(attach_photo, LocaleController.getString("ChatTakePhoto", R.string.ChatTakePhoto), R.drawable.ic_attach_photo);
            menuItem.addSubItem(attach_gallery, LocaleController.getString("ChatGallery", R.string.ChatGallery), R.drawable.ic_attach_gallery);
            menuItem.setShowFromBottom(true);
            menuItem.setBackgroundDrawable(null);

            addToAttachLayout(menuItem);

            messsageEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(count == 1){
                        String add = s.subSequence(start, start+1).toString();
                        if(add.equals(" ")){
                            String url = Utils.getUrlFromText(s.toString());
                            if(url != null){
                                urlThumbView.setVisibility(View.VISIBLE);
                                urlThumbView.setContent(url, new StreamUrlThumbView.LoadUrlDelegate() {
                                    @Override
                                    public void loadResult(String url, String title, String image, String description) {
                                        urlTitle = title;
                                        urlImage = image;
                                        urlDescription = description;
                                    }
                                });
                            }
                        }
                    } else if(count > 4){
                        String url = Utils.getUrlFromText(s.toString());
                        if(url != null){
                            urlThumbView.setVisibility(View.VISIBLE);
                            urlThumbView.setContent(url, new StreamUrlThumbView.LoadUrlDelegate() {
                                @Override
                                public void loadResult(String url, String title, String image, String description) {
                                    urlTitle = title;
                                    urlImage = image;
                                    urlDescription = description;
                                }
                            });
                        }
                    } else if(before > 0){
                        String text = s.toString();
                        if(text.indexOf(" ") > text.indexOf("http")){
                            String url = Utils.getUrlFromText(s.toString());
                            if(url != null){
                                String thumbUrl = urlThumbView.getUrl();
                                if(thumbUrl != null && url.equals(thumbUrl))
                                    return;
                                urlThumbView.setVisibility(View.VISIBLE);
                                urlThumbView.setContent(url, new StreamUrlThumbView.LoadUrlDelegate() {
                                    @Override
                                    public void loadResult(String url, String title, String image, String description) {
                                        urlTitle = title;
                                        urlImage = image;
                                        urlDescription = description;
                                    }
                                });
                            } else
                                urlThumbView.setVisibility(View.GONE);
                        } else {
                            urlThumbView.setVisibility(View.GONE);
                        }
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });

            adapter = new PhotoPickerAdapter(getParentActivity());
            photoListView.setAdapter(adapter);
            if (pickPhotoEntries.size() <= 0 ) {
                photoListView.setVisibility(View.GONE);
            }
            photoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
                    builder.setMessage(LocaleController.getString("AreYouSureDeletePhoto", R.string.AreYouSureDeletePhoto));
                    builder.setPositiveButton(LocaleController.getString("OK", R.string.OK), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            pickPhotoEntries.remove(adapter.getItem(position));

                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton(LocaleController.getString("Cancel", R.string.Cancel), null);
                    showAlertDialog(builder);


                }
            });

        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }


        return fragmentView;
    }

    public void addToAttachLayout(View view) {
        if (attachButton == null) {
            return;
        }
        if (view.getParent() != null) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            viewGroup.removeView(view);
        }
        attachButton.addView(view);
        FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) view.getLayoutParams();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = AndroidUtilities.dp(48);
        layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
        view.setLayoutParams(layoutParams);
    }

    @Override
    public boolean onBackPressed() {
        Tracker t = ApplicationLoader.getInstance().getTracker(
                ApplicationLoader.TrackerName.APP_TRACKER);
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                .setAction("发布")
                .setLabel("发布取消")
                .build());

        return super.onBackPressed();
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == 0) {
                PhotoViewer.getInstance().setParentActivity(getParentActivity());
                final ArrayList<Object> arrayList = new ArrayList<>();
                int orientation = 0;
                try {
                    ExifInterface ei = new ExifInterface(currentPicturePath);
                    int exif = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
                    switch(exif) {
                        case ExifInterface.ORIENTATION_ROTATE_90:
                            orientation = 90;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_180:
                            orientation = 180;
                            break;
                        case ExifInterface.ORIENTATION_ROTATE_270:
                            orientation = 270;
                            break;
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
                arrayList.add(new MediaController.PhotoEntry(0, 0, 0, currentPicturePath, orientation, false));

                PhotoViewer.getInstance().openPhotoForSelect(arrayList, 0, 2, new PhotoViewer.EmptyPhotoViewerProvider() {
                    @Override
                    public void sendButtonPressed(int index) {
                        MediaController.PhotoEntry photoEntry = (MediaController.PhotoEntry) arrayList.get(0);
                        pickPhotoEntries.add(0,photoEntry);
                        adapter.notifyDataSetChanged();
                    }
                });
//                Utilities.addMediaToGallery(currentPicturePath);
                currentPicturePath = null;
            } else if (requestCode == 1) {
                if (data == null || data.getData() == null) {
                    showAttachmentError();
                    return;
                }
            }
        }
    }
    private void showAttachmentError() {
        if (getParentActivity() == null) {
            return;
        }
        Toast toast = Toast.makeText(getParentActivity(), LocaleController.getString("UnsupportedAttachment", R.string.UnsupportedAttachment), Toast.LENGTH_SHORT);
        toast.show();
    }

    private class PhotoPickerAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public PhotoPickerAdapter(Context context) {
            mContext = context;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            if (pickPhotoEntries.size() > 0) {
                photoListView.setVisibility(View.VISIBLE);
            } else {
                photoListView.setVisibility(View.GONE);
            }
        }

        @Override
        public boolean isEnabled(int i) {
            return true;
        }

        @Override
        public int getCount() {
            return pickPhotoEntries.size();
        }

        @Override
        public Object getItem(int i) {
            return pickPhotoEntries.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = LayoutInflater.from(mContext).inflate(R.layout.post_photo_row, parent, false);
            }
            BackupImageView photoImage = (BackupImageView) view.findViewById(R.id.picked_photo);
            MediaController.PhotoEntry photoEntry = pickPhotoEntries.get(position);

            if (photoEntry.thumbPath != null) {
                photoImage.setImage(photoEntry.thumbPath, null, getParentActivity().getResources().getDrawable(R.drawable.nophotos));
            } else if (photoEntry.path != null) {
                photoImage.setOrientation(photoEntry.orientation, true);
//                photoImage.setImage("thumb://" + photoEntry.imageId + ":" + photoEntry.path, null, getParentActivity().getResources().getDrawable(R.drawable.nophotos));
                int size = (int) (AndroidUtilities.getPhotoSize() / AndroidUtilities.density);
                photoImage.setImage(photoEntry.path, String.format(Locale.US, "%d_%d", size, size), getParentActivity().getResources().getDrawable(R.drawable.nophotos));
            } else {
                photoImage.setImageResource(R.drawable.nophotos);
            }
            
            return view;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

    private void post () {
        posting = true;
        if (pickPhotoEntries != null && pickPhotoEntries.size() > 0) {
            loadingDialog.show();

            postPhoto();
        } else {
            loadingDialog.show();

            postInternal(null);
        }

    }

    private void postPhoto () {

        if(pickPhotoEntries.size() > 6) {
            T8Toast.lt(LocaleController.getString("", R.string.Forward2StreamWithMuchImage));
            loadingDialog.dismiss();
            return;
        }

        ApiRequestHelper.getQNToken(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.GRAPH.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        Tracker t = ApplicationLoader.getInstance().getTracker(
                                ApplicationLoader.TrackerName.APP_TRACKER);
                        // Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                                .setAction("发布")
                                .setLabel("发布失败")
                                .build());
                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(final String responseString) {
                        try {
                            JSONObject rootJson = new JSONObject(responseString);
                            if (rootJson.has("qnUptoken")) {
                                String qnToken = rootJson.getString("qnUptoken");
                                for (MediaController.PhotoEntry entry : pickPhotoEntries) {
                                    uploadImages.add(entry.path);
                                }

                                QNUtils qnUtils = new QNUtils(qnToken, uploadImages, new QNUtils.QNUploadDelegate() {
                                    @Override
                                    public void uploadResult(HashMap<String, String> result) {
                                        if (result != null) {
                                                postInternal(result);
                                        } else {
                                            //TODO
                                            T8Log.ZHAO_ZHEN.d("图片上传失败");
                                            T8Toast.lt(LocaleController.getString("Forward2StreamUploadImageFailed", R.string.Forward2StreamUploadImageFailed));
                                            Tracker t = ApplicationLoader.getInstance().getTracker(
                                                    ApplicationLoader.TrackerName.APP_TRACKER);
                                            // Build and send an Event.
                                            t.send(new HitBuilders.EventBuilder()
                                                    .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                                                    .setAction("发布")
                                                    .setLabel("发布失败")
                                                    .build());

                                            loadingDialog.dismiss();
                                        }
                                    }
                                });
                                qnUtils.start();
                            } else {
                                loadingDialog.dismiss();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            loadingDialog.dismiss();
                        }
                    }
                }).execute();
            }
        });
    }

    private void postInternal(HashMap<String, String> photos) {
        NodeEntity.PostNodeEntity postNodeEntity = new NodeEntity.PostNodeEntity();
        postNodeEntity.setText(messsageEditText.getText().toString());

        StringBuilder sb = new StringBuilder();
        if(photos != null){
            for(String image : uploadImages){
                sb.append(photos.get(image));
                sb.append(",");
            }
        }
        if(uploadImages.size() > 0)
            postNodeEntity.setImage(sb.substring(0, sb.length() - 1));
        if(urlThumbView.getVisibility() == View.VISIBLE) {
            Log.d("zhaozhen33", "title " + urlTitle);
            Log.d("zhaozhen33", "urlImage " + urlImage);
            Log.d("zhaozhen33", "urlDescription " + urlDescription);
            postNodeEntity.setUrl(urlThumbView.getUrl());
            postNodeEntity.setUrl_title(urlTitle);
            postNodeEntity.setUrl_image(urlImage);
            postNodeEntity.setUrl_description(urlDescription);
        }

        final NodeEntity entity = new NodeEntity();
        entity.setPost(postNodeEntity);
        if(chat_id <= 0)
            entity.setIs_public(1);
        else
            entity.setIs_public(0);
        entity.setType(NodeEntity.NODE_TYPE_POST);

        NodeEntity.AuthorEntity author = new NodeEntity.AuthorEntity();
        int clientId = UserConfig.getClientUserId();
        TLRPC.User user = MessagesController.getInstance().getUser(clientId);
        author.setId(T8UserConfig.getUserId());
        author.setTg_user_id(String.valueOf(clientId));
        author.setAvatar(AvatarUpdateUtils.getUserImageFileAndUpload(true));
        if (user.username != null && user.username.length() > 0) {
            author.setUsername(user.username);
        } else {
            author.setUsername(ContactsController.formatName(user.first_name, user.last_name));
        }
        entity.setAuthor(author);

        Date postTime = new Date();
        postTime.setTime(System.currentTimeMillis());
        entity.setCreate_at(postTime);

        ApiRequestHelper.postCreate(String.valueOf(UserConfig.getClientUserId()), "",postNodeEntity.toString(), entity.getIs_public(), chat_id, entity.getType(), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.POST_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        posting = false;
                        loadingDialog.dismiss();
                        Tracker t = ApplicationLoader.getInstance().getTracker(
                                ApplicationLoader.TrackerName.APP_TRACKER);
                        // Build and send an Event.
                        t.send(new HitBuilders.EventBuilder()
                                .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                                .setAction("发布")
                                .setLabel("发布失败")
                                .build());

                        loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        posting = false;
                        loadingDialog.dismiss();
                        Post2StreamResultEntity resultEntity = new Post2StreamResultEntity();
                        resultEntity = resultEntity.jsonParse(responseString);
                        if(resultEntity.getError() == null) {
                            Tracker t = ApplicationLoader.getInstance().getTracker(
                                    ApplicationLoader.TrackerName.APP_TRACKER);
                            // Build and send an Event.
                            t.send(new HitBuilders.EventBuilder()
                                    .setCategory(chat_id <= 0 ? "公共信息流发布":"群留言板发布")
                                    .setAction("发布")
                                    .setLabel("发布成功")
                                    .build());
                            T8Toast.lt(LocaleController.getString("", R.string.Forward2StreamSuccess));
                            PostsController.getInstance().addNewPost(String.valueOf(chat_id), resultEntity.getPost_id(), entity);
                            finishFragment();
                        }
                        else {
                            T8Toast.lt(resultEntity.getError().getMessage());
                            Tracker t = ApplicationLoader.getInstance().getTracker(
                                    ApplicationLoader.TrackerName.APP_TRACKER);
                            // Build and send an Event.
                            t.send(new HitBuilders.EventBuilder()
                                    .setCategory(chat_id <= 0 ? "公共信息流发布" : "群留言板发布")
                                    .setAction("发布")
                                    .setLabel("发布失败")
                                    .build());
                        }
                    }
                }).execute();
            }
        });
    }
}
