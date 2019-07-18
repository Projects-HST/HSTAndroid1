package com.skilex.customer.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.skilex.customer.R;
import com.skilex.customer.activity.SplashScreenActivity;
import com.skilex.customer.bean.support.CartService;
import com.skilex.customer.bean.support.Service;
import com.skilex.customer.fragment.DynamicSubCatFragment;
import com.skilex.customer.helper.AlertDialogHelper;
import com.skilex.customer.helper.ProgressDialogHelper;
import com.skilex.customer.servicehelpers.ServiceHelper;
import com.skilex.customer.serviceinterfaces.IServiceListener;
import com.skilex.customer.utils.PreferenceStorage;
import com.skilex.customer.utils.SkilExConstants;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;

import static android.util.Log.d;
import static com.android.volley.VolleyLog.TAG;

public class GeneralServiceListAdapter extends BaseAdapter{

    //    private final Transformation transformation;
    private Context context;
    private ArrayList<CartService> services;
    private boolean mSearching = false;
    private boolean mAnimateSearch = false;
    Boolean click = false;
    private ArrayList<Integer> mValidSearchIndices = new ArrayList<Integer>();
    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;

    DynamicSubCatFragment dsf = new DynamicSubCatFragment();

    public GeneralServiceListAdapter(Context context, ArrayList<CartService> services) {
        this.context = context;
        this.services = services;
        Collections.reverse(services);
//        transformation = new RoundedTransformationBuilder()
//                .cornerRadiusDp(0)
//                .oval(false)
//                .build();
        mSearching = false;
    }

    @Override
    public int getCount() {
        if (mSearching) {
            if (!mAnimateSearch) {
                mAnimateSearch = true;
            }
            return mValidSearchIndices.size();
        } else {
            return services.size();
        }
    }

    @Override
    public Object getItem(int position) {
        if (mSearching) {
            return services.get(mValidSearchIndices.get(position));
        } else {
            return services.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final GeneralServiceListAdapter.ViewHolder holder;
        if (convertView == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            convertView = inflater.inflate(R.layout.category_list_item, parent, false);

            holder = new GeneralServiceListAdapter.ViewHolder();
            holder.txtCatName = (TextView) convertView.findViewById(R.id.sub_category_name);
            holder.txtCatName.setText(services.get(position).getservice_name());
            holder.imgCat = (ImageView) convertView.findViewById(R.id.sub_category_image);
            String url = services.get(position).getService_picture();
            if (((url != null) && !(url.isEmpty()))) {
                Picasso.get().load(url).into(holder.imgCat);
            }
            holder.addList = (ImageView) convertView.findViewById(R.id.add_to_list);
            holder.addList.setVisibility(View.GONE);

        } else {
            holder = (GeneralServiceListAdapter.ViewHolder) convertView.getTag();
        }

        if (mSearching) {
            position = mValidSearchIndices.get(position);

        } else {
            Log.d("Event List Adapter", "getview pos called" + position);
        }

        return convertView;
    }

    public void startSearch(String eventName) {
        mSearching = true;
        mAnimateSearch = false;
        Log.d("EventListAdapter", "serach for event" + eventName);
        mValidSearchIndices.clear();
        for (int i = 0; i < services.size(); i++) {
            String homeWorkTitle = services.get(i).getservice_name();
            if ((homeWorkTitle != null) && !(homeWorkTitle.isEmpty())) {
                if (homeWorkTitle.toLowerCase().contains(eventName.toLowerCase())) {
                    mValidSearchIndices.add(i);
                }
            }
        }
        Log.d("Event List Adapter", "notify" + mValidSearchIndices.size());
    }

    public void exitSearch() {
        mSearching = false;
        mValidSearchIndices.clear();
        mAnimateSearch = false;
    }

    public void clearSearchFlag() {
        mSearching = false;
    }

    public class ViewHolder {
        public TextView txtCatName;
        public ImageView imgCat, addList;
    }

    public boolean ismSearching() {
        return mSearching;
    }

    public int getActualEventPos(int selectedSearchpos) {
        if (selectedSearchpos < mValidSearchIndices.size()) {
            return mValidSearchIndices.get(selectedSearchpos);
        } else {
            return 0;
        }
    }

}