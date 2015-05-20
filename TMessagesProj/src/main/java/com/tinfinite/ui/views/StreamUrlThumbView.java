package com.tinfinite.ui.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tinfinite.utils.Utils;

import org.opengraph.OpenGraph;
import org.telegram.android.volley.RequestQueue;
import org.telegram.android.volley.Response;
import org.telegram.android.volley.VolleyError;
import org.telegram.android.volley.toolbox.ImageRequest;
import org.telegram.android.volley.toolbox.Volley;
import org.telegram.messenger.R;

import java.util.concurrent.Executors;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by PanJiafang on 15/3/30.
 */
public class StreamUrlThumbView extends BaseView {

    @InjectView(R.id.web_image)
    public ImageView webImage;

    @InjectView(R.id.web_del)
    public ImageView webDel;

    @InjectView(R.id.web_title)
    public TextView webTitle;

    @InjectView(R.id.web_uri)
    public TextView webUrl;

    @InjectView(R.id.web_description)
    public TextView webDescription;

    private String webUrlText = null;
    private RequestQueue requestQueue;

    private boolean canDismiss = false;

    public StreamUrlThumbView(Context context) {
        super(context);
        init();
    }

    public StreamUrlThumbView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StreamUrlThumbView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init(){
        View view = LayoutInflater.from(getContext()).inflate(R.layout.view_stream_urlthumb, this, false);
        ButterKnife.inject(this, view);
        addView(view);

        initViews();

        webDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (requestQueue != null) {
                    requestQueue.cancelAll("search");
                    requestQueue.stop();
                }
                setVisibility(View.GONE);
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (webUrlText != null) {
                    Uri uri = Uri.parse(webUrlText);

                    if (uri != null) {
//                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
//                        getContext().startActivity(intent);
                        Utils.clickUrl(StreamUrlThumbView.this, webUrlText);
                    }
                }
            }
        });
    }

    private void initViews(){
        webImage.setVisibility(GONE);
        webTitle.setVisibility(GONE);
        webUrl.setVisibility(GONE);
        webDescription.setVisibility(GONE);
    }

    public void canDismiss(){
        webDel.setVisibility(VISIBLE);
    }

    public void setContent(final String url, String title, String image, String description){
        setVisibility(View.VISIBLE);
        if (image != null && image.length() > 0) {
            requestQueue = Volley.newRequestQueue(getContext());
            ImageRequest request = new ImageRequest(image, new Response.Listener<Bitmap>() {
                @Override
                public void onResponse(Bitmap response) {
                    webImage.setVisibility(View.VISIBLE);
                    webImage.setImageBitmap(response);
                }
            }, 300, 300, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            });
            request.setTag("search");
            requestQueue.add(request);
        } else {
            webImage.setVisibility(View.GONE);
        }

        webTitle.setText(title);
        webUrl.setText(url);
        webUrlText = url;
        webDescription.setText(description);
        webTitle.setVisibility(VISIBLE);
        webUrl.setVisibility(VISIBLE);
        webDescription.setVisibility(VISIBLE);
    }

    public void setContent(final String url, final LoadUrlDelegate delegate){
        this.setTag(url);
        webUrlText = null;
        setVisibility(GONE);
        initViews();
        AsyncTask<String, OpenGraph, OpenGraph> task = new AsyncTask<String, OpenGraph, OpenGraph>() {
            @Override
            protected OpenGraph doInBackground(String... params) {
                OpenGraph openGraph = null;
                try {
                    openGraph = new OpenGraph(params[0], true);
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return openGraph;
            }

            @Override
            protected void onPostExecute(OpenGraph openGraph) {
                if(getTag().equals(url)) {
                    if (openGraph == null) {
                        setVisibility(View.GONE);
                        if(delegate != null)
                            delegate.loadResult(null, null, null, null);
                        return;
                    }
                    String image = openGraph.getContent("image");
                    String title = openGraph.getContent("title");
                    String description = openGraph.getContent("description");
                    if(title == null) {
                        setVisibility(GONE);
                        if(delegate != null)
                            delegate.loadResult(null, title, image, description);
                        return;
                    }

                    setVisibility(View.VISIBLE);
                    if (image != null && image.length() > 0) {
                        requestQueue = Volley.newRequestQueue(getContext());
                        ImageRequest request = new ImageRequest(openGraph.getContent("image"), new Response.Listener<Bitmap>() {
                            @Override
                            public void onResponse(Bitmap response) {
                                webImage.setVisibility(View.VISIBLE);
                                webImage.setImageBitmap(response);
                            }
                        }, 300, 300, Bitmap.Config.ARGB_8888, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                            }
                        });
                        request.setTag("search");
                        requestQueue.add(request);
                    } else {
                        webImage.setVisibility(View.GONE);
                    }
                    webTitle.setVisibility(VISIBLE);
                    webUrl.setVisibility(VISIBLE);
                    webDescription.setVisibility(VISIBLE);

                    webTitle.setText(title);
                    webUrl.setText(url);
                    webUrlText = url;
                    webDescription.setText(description);
                    if(delegate != null)
                        delegate.loadResult(url, title, image, description);
                }
            }
        };
        task.executeOnExecutor(Executors.newCachedThreadPool(), url);
    }

    public void cancel(){
        if (requestQueue != null) {
            requestQueue.cancelAll("search");
            requestQueue.stop();
        }
    }

    public interface LoadUrlDelegate {
        public void loadResult(String url, String title, String image, String description);
    }

    public String getUrl(){
        return webUrlText;
    }
}
