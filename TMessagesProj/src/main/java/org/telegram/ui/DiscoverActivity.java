package org.telegram.ui;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.tinfinite.ui.fragment.DiscoverGroupActivity;
import com.tinfinite.ui.fragment.PublicStreamFragment;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Adapters.BaseFragmentAdapter;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Components.AvatarDrawable;

/**
 * Created by PanJiafang on 15/2/15.
 */
public class DiscoverActivity extends BaseFragment{
    ListView listView;
    private ListAdapter listAdapter;

    private int rowCount = 0;
    private int postStream;
    private int discoverGroup;
    private int myVoteMessage;

    @Override
    public boolean onFragmentCreate() {
        updateRowsIds();
        return true;
    }

    @Override
    public void onFragmentDestroy() {
        super.onFragmentDestroy();
    }

    @Override
    public View createView(Context context, LayoutInflater inflater) {
        if(fragmentView == null){
            listAdapter = new ListAdapter(getParentActivity());

            fragmentView = new LinearLayout(getParentActivity());
            fragmentView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            ((LinearLayout) fragmentView).setOrientation(LinearLayout.VERTICAL);
            fragmentView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });

            LinearLayout frameLayout = (LinearLayout) fragmentView;

            actionBar.setBackButtonImage(R.drawable.ic_ab_back);
            actionBar.setTitle(LocaleController.getString("Discover", R.string.Discover));
            actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
                @Override
                public void onItemClick(int id) {
                    if (id == -1) {
                        finishFragment();
                    }
                }
            });

            listView = new ListView(getParentActivity());
            listView.setDivider(null);
            listView.setDividerHeight(0);
            listView.setVerticalScrollBarEnabled(false);
            AndroidUtilities.setListViewEdgeEffectColor(listView, AvatarDrawable.getProfileBackColorForId(5));
            frameLayout.addView(listView);
            LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) listView.getLayoutParams();
            layoutParams.width = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.height = FrameLayout.LayoutParams.MATCH_PARENT;
            layoutParams.gravity = Gravity.TOP;
            listView.setLayoutParams(layoutParams);

            listView.setAdapter(listAdapter);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    if (getParentActivity() == null) {
                        return;
                    }
                    if (position == postStream) {
                        PublicStreamFragment publicStreamFragment = new PublicStreamFragment(null);
                        presentFragment(publicStreamFragment);
                    } else if (position == discoverGroup) {
                        DiscoverGroupActivity fragment = new DiscoverGroupActivity(null);
                        presentFragment(fragment);
                    } else if (position == myVoteMessage) {
                        MyVotedMessagesActivity fragment = new MyVotedMessagesActivity();
                        presentFragment(fragment);
                    }
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

    private void updateRowsIds() {
        rowCount = 0;
        postStream = rowCount++;
        discoverGroup = rowCount++;
        myVoteMessage = rowCount++;
    }

    private class ListAdapter extends BaseFragmentAdapter {
        private Context mContext;

        public ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getCount() {
            return rowCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean isEnabled(int position) {
            return true;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                view = new TextCell(mContext);
            }
            TextCell textCell = (TextCell) view;
            if (i == postStream) {
                textCell.setTextAndIcon(LocaleController.getString("PublicStream", R.string.PublicStream), R.drawable.menu_upvoted, true);
            } else if (i == discoverGroup) {
                textCell.setTextAndIcon(LocaleController.getString("DiscoverGroup", R.string.DiscoverGroup), R.drawable.menu_newgroup, true);
            } else if (i == myVoteMessage) {
                textCell.setTextAndIcon(LocaleController.getString("MyVoteMessage", R.string.MyVoteMessage), R.drawable.menu_upvoted, true);
            }
            return view;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }
    }

}
