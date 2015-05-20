package com.tinfinite.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.loopj.android.http.RequestParams;
import com.tinfinite.android.sdk.T8Log;
import com.tinfinite.android.sdk.T8Toast;
import com.tinfinite.android.sdk.api.ApiHttpClient;
import com.tinfinite.android.sdk.api.ApiRequestHelper;
import com.tinfinite.android.sdk.api.ApiUrlHelper;
import com.tinfinite.entity.NodeEntity;
import com.tinfinite.entity.Post2StreamResultEntity;
import com.tinfinite.ui.adapter.Forward2StreamAdapter;
import com.tinfinite.ui.widget.LoadingDialog;
import com.tinfinite.utils.QNUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;
import org.telegram.android.LocaleController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.BaseFragment;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by PanJiafang on 15/3/24.
 */
public class Forward2StreamFragment extends BaseFragment{
    private static final int FORWARD_GROUP = 0;
    private static final int FORWARD_GROUP_AND_PUBLIC = 2;

    private static final int TYPE_FORWARD = 1;

    private static final String INTENT_KEY = "data";

    private ListView listView;
    private EditText et;
    private CheckBox cbox;

    private NodeEntity.ForwardNodeEntity forwardNodeEntity;

    private ArrayList<String> uploadImages = new ArrayList<>();

    private Forward2StreamAdapter adapter;

    private LoadingDialog loadingDialog;

    public static Bundle createBundle(NodeEntity.ForwardNodeEntity entity){
        Bundle bundle = new Bundle();
        bundle.putParcelable(INTENT_KEY, entity);
        return bundle;
    }

    public Forward2StreamFragment(Bundle args) {
        super(args);
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null){
            fragmentView = new LinearLayout(context);
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("Forward2Stream_Title", R.string.Forward2Stream_Title));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    } else if(id == 1){
                        uploadImages.clear();

                        if(loadingDialog != null)
                            loadingDialog.show();

                        ArrayList<NodeEntity.ForwardContentEntity> contentEntities = forwardNodeEntity.getContent();
                        for(NodeEntity.ForwardContentEntity contentEntity : contentEntities){
                            if(contentEntity.getMessagetype() == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_IMAGE){
                                String localFile = contentEntity.getMessagecontent();
                                if(!StringUtils.isEmpty(localFile))
                                    uploadImages.add(localFile);
                            }
                        }
//                        String third_group_image = forwardNodeEntity.getThird_group_image();
//                        if(!StringUtils.isEmpty(third_group_image))
//                            uploadImages.add(third_group_image);

                        T8Log.PAN_JIA_FANG.d("上传图片个数："+uploadImages.size());

                        if(uploadImages.size() == 0) {
                            forward(null);
                            return;
                        }
                        ApiRequestHelper.getQNToken(String.valueOf(UserConfig.getClientUserId()), new ApiRequestHelper.BuildParamsCallBack() {
                            @Override
                            public void build(RequestParams params) {
                                ApiUrlHelper.GRAPH.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                                    @Override
                                    public void onFailure() {
                                        if(loadingDialog != null)
                                            loadingDialog.dismiss();
                                    }

                                    @Override
                                    public void onSuccess(final String responseString) {
                                        try {
                                            JSONObject rootJson = new JSONObject(responseString);
                                            if (rootJson.has("qnUptoken")) {
                                                String qnToken = rootJson.getString("qnUptoken");

                                                QNUtils qnUtils = new QNUtils(qnToken, uploadImages, new QNUtils.QNUploadDelegate() {
                                                    @Override
                                                    public void uploadResult(HashMap<String, String> result) {
                                                        if(result != null){
                                                            forward(result);
                                                        } else {
                                                            //TODO
                                                            if(loadingDialog != null)
                                                                loadingDialog.dismiss();
                                                            T8Log.PAN_JIA_FANG.d("图片上传失败");
                                                            T8Toast.lt(LocaleController.getString("Forward2StreamUploadImageFailed", R.string.Forward2StreamUploadImageFailed));
                                                        }
                                                    }
                                                });
                                                qnUtils.start();
                                            }
                                        } catch (Exception e) {
                                            if(loadingDialog != null)
                                                loadingDialog.dismiss();
                                            e.printStackTrace();
                                        }
                                    }
                                }).execute();
                            }
                        });

                    }
                }
            });
            ActionBarMenu menu = actionBar.createMenu();
            menu.addItem(1, R.drawable.ic_done);

            loadingDialog = new LoadingDialog(getParentActivity());

            View view = inflater.inflate(R.layout.fragment_forward2stream, null);

            listView = (ListView) view.findViewById(R.id.forward2stream_listview);
            et = (EditText) view.findViewById(R.id.forward2stream_et);
            cbox = (CheckBox) view.findViewById(R.id.forward2stream_cbox);

            Bundle bundle = getArguments();
            forwardNodeEntity = bundle.getParcelable(INTENT_KEY);
            adapter = new Forward2StreamAdapter(getParentActivity(), forwardNodeEntity.getContent());
            listView.setAdapter(adapter);

            view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

            ((LinearLayout) fragmentView).addView(view);
        } else {
            ViewGroup parent = (ViewGroup)fragmentView.getParent();
            if (parent != null) {
                parent.removeView(fragmentView);
            }
        }
        return fragmentView;
    }

    private void forward(HashMap<String, String> images){
        T8Log.PAN_JIA_FANG.d("准备上传到服务器");

        if(images != null){
            ArrayList<NodeEntity.ForwardContentEntity> contentEntities = forwardNodeEntity.getContent();
            for(NodeEntity.ForwardContentEntity contentEntity : contentEntities){
                if(contentEntity.getMessagetype() == NodeEntity.ForwardContentEntity.MESSAGE_TPYE_IMAGE){
                    String localFile = contentEntity.getMessagecontent();
                    if(!StringUtils.isEmpty(localFile))
                        contentEntity.setMessagecontent(images.get(localFile));
                }
            }

            String third_group_image = forwardNodeEntity.getThird_group_image();
            if(!StringUtils.isEmpty(third_group_image))
                forwardNodeEntity.setThird_group_image(images.get(third_group_image));
        }

        forwardNodeEntity.setComment(et.getText().toString());

        NodeEntity entity = new NodeEntity();
        entity.setForward(forwardNodeEntity);
        entity.setIs_public(cbox.isChecked() ? FORWARD_GROUP_AND_PUBLIC : FORWARD_GROUP);
        entity.setType(TYPE_FORWARD);

        T8Log.PAN_JIA_FANG.d(entity.toString());

        ApiRequestHelper.postCreate(String.valueOf(UserConfig.getClientUserId()), forwardNodeEntity.toString(), "", entity.getIs_public(), Integer.parseInt(forwardNodeEntity.getThird_group_id()), entity.getType(), new ApiRequestHelper.BuildParamsCallBack() {
            @Override
            public void build(RequestParams params) {
                ApiUrlHelper.POST_CREATE.build(params, new ApiHttpClient.DoveHttpResponseHandler() {
                    @Override
                    public void onFailure() {
                        if(loadingDialog != null)
                            loadingDialog.dismiss();
                    }

                    @Override
                    public void onSuccess(String responseString) {
                        if(loadingDialog != null)
                            loadingDialog.dismiss();

                        Post2StreamResultEntity resultEntity = new Post2StreamResultEntity();
                        resultEntity = resultEntity.jsonParse(responseString);
                        if(resultEntity.getError() == null) {
                            T8Toast.lt(LocaleController.getString("", R.string.Forward2StreamSuccess));
                            finishFragment();
                        }
                        else
                            T8Toast.lt(resultEntity.getError().getMessage());
                    }
                }).execute();
            }
        });

    }

}
