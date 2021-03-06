package com.skilex.customer.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.Gson;
import com.skilex.customer.R;
import com.skilex.customer.adapter.MainServiceListAdapter;
import com.skilex.customer.adapter.SubCategoryTabAdapter;
import com.skilex.customer.bean.support.Category;
import com.skilex.customer.bean.support.SubCategory;
import com.skilex.customer.bean.support.SubCategoryList;
import com.skilex.customer.helper.AlertDialogHelper;
import com.skilex.customer.helper.ProgressDialogHelper;
import com.skilex.customer.interfaces.DialogClickListener;
import com.skilex.customer.servicehelpers.ServiceHelper;
import com.skilex.customer.serviceinterfaces.IServiceListener;
import com.skilex.customer.utils.CommonUtils;
import com.skilex.customer.utils.PreferenceStorage;
import com.skilex.customer.utils.SkilExConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static android.util.Log.d;

public class SubCategoryActivity extends AppCompatActivity implements IServiceListener, DialogClickListener, AdapterView.OnItemClickListener, View.OnClickListener {

    private static final String TAG = SubCategoryActivity.class.getName();

    private ServiceHelper serviceHelper;
    private ProgressDialogHelper progressDialogHelper;
    Handler mHandler = new Handler();
    int totalCount = 0, checkrun = 0;
    protected boolean isLoadingForFirstTime = true;
    ArrayList<SubCategory> categoryArrayList;
    MainServiceListAdapter categoryListAdapter;
    ListView loadMoreListView;
    Category category;
    TabLayout tab;
    ViewPager viewPager;
    String res = "";
    int tabPosition;
    private TextView rateCount, summary;
    private RelativeLayout summaryLayout;

    private Timer timer;
    private TimerTask timerTask;
    private Handler handler = new Handler();

    //To start timer
    public void startTimer() {
        handler.postDelayed(new Runnable() {
            public void run() {
                setrates();
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category);
        serviceHelper = new ServiceHelper(this);
        serviceHelper.setServiceListener(this);
        progressDialogHelper = new ProgressDialogHelper(this);
        findViewById(R.id.back_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.removeCallbacksAndMessages(null);
                finish();
            }
        });

        rateCount = (TextView) findViewById(R.id.service_count);

        summary = (TextView) findViewById(R.id.view_summary);
        summary.setOnClickListener(this);

        summaryLayout = (RelativeLayout) findViewById(R.id.bot_layout);

        categoryArrayList = new ArrayList<>();

        category = (Category) getIntent().getSerializableExtra("cat");
//        loadMoreListView = (LinearLayout) findViewById(R.id.layout_member_list);
//        loadMoreListView = (ListView) findViewById(R.id.listView_sub_categories);
//        loadMoreListView.setOnItemClickListener(this);
        callGetSubCategoryService();

        tab = (TabLayout) findViewById(R.id.tab_layout);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

    }

    @Override
    public void onAlertPositiveClicked(int tag) {

    }

    @Override
    public void onAlertNegativeClicked(int tag) {

    }

    @Override
    public void onResponse(final JSONObject response) {
        progressDialogHelper.hideProgressDialog();
        if (validateResponse(response)) {
            try {
                if (res.equalsIgnoreCase("clear")) {
                    viewPager.setCurrentItem(tabPosition);
                    PreferenceStorage.saveServiceCount(this, "");
                    PreferenceStorage.saveRate(this, "");
                } else {
                    JSONArray getData = response.getJSONArray("sub_categories");
//                loadMembersList(getData.length());
                    Gson gson = new Gson();
                    SubCategoryList subCategoryList = gson.fromJson(response.toString(), SubCategoryList.class);
                    if (subCategoryList.getCategoryArrayList() != null && subCategoryList.getCategoryArrayList().size() > 0) {
                        totalCount = subCategoryList.getCount();
                        this.categoryArrayList.addAll(subCategoryList.getCategoryArrayList());
                        isLoadingForFirstTime = false;
//                    updateListAdapter(subCategoryList.getCategoryArrayList());
                    } else {
                        if (categoryArrayList != null) {
                            categoryArrayList.clear();
//                        updateListAdapter(subCategoryList.getCategoryArrayList());
                        }
                    }
                    initialiseTabs(getData);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, "Error while sign In");
        }
    }

    @Override
    public void onError(String error) {

    }

    public void callGetSubCategoryService() {
//        if (classTestArrayList != null)
//            classTestArrayList.clear();

        if (CommonUtils.isNetworkAvailable(this)) {
            progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
            loadCat();
        } else {
            AlertDialogHelper.showSimpleAlertDialog(this, getString(R.string.error_no_net));
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

    }

    private void loadCat() {
        JSONObject jsonObject = new JSONObject();
        String id = "";
        id = category.getCat_id();
        PreferenceStorage.saveCatClick(this, id);
        try {
            jsonObject.put(SkilExConstants.MAIN_CATEGORY_ID, id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = SkilExConstants.BUILD_URL + SkilExConstants.GET_SUB_CAT_LIST;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }


    private boolean validateResponse(JSONObject response) {
        boolean signInSuccess = false;
        if ((response != null)) {
            try {
                String status = response.getString("status");
                String msg = response.getString(SkilExConstants.PARAM_MESSAGE);
                String msg_en = response.getString(SkilExConstants.PARAM_MESSAGE_ENG);
                String msg_ta = response.getString(SkilExConstants.PARAM_MESSAGE_TAMIL);
                d(TAG, "status val" + status + "msg" + msg);

                if ((status != null)) {
                    if (((status.equalsIgnoreCase("activationError")) || (status.equalsIgnoreCase("alreadyRegistered")) ||
                            (status.equalsIgnoreCase("notRegistered")) || (status.equalsIgnoreCase("error")))) {
                        signInSuccess = false;
                        d(TAG, "Show error dialog");

                        if (PreferenceStorage.getLang(this).equalsIgnoreCase("tamil")) {
                            AlertDialogHelper.showSimpleAlertDialog(this, msg_ta);
                        } else {
                            AlertDialogHelper.showSimpleAlertDialog(this, msg_en);
                        }

                    } else {
                        signInSuccess = true;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return signInSuccess;
    }

//    protected void updateListAdapter(ArrayList<SubCategory> categoryArrayList) {
//        this.categoryArrayList.addAll(categoryArrayList);
//        if (categoryListAdapter == null) {
//            categoryListAdapter = new MainServiceListAdapter(this, this.categoryArrayList);
//            loadMoreListView.setAdapter(categoryListAdapter);
//        } else {
//            categoryListAdapter.notifyDataSetChanged();
//        }
//    }

    private void initialiseTabs(JSONArray subCategory) {
        startTimer();
        for (int k = 0; k < subCategory.length(); k++) {
            try {
                if (PreferenceStorage.getLang(this).equalsIgnoreCase("tamil")) {
                    tab.addTab(tab.newTab().setText("" + subCategory.getJSONObject(k).get("sub_cat_ta_name")));
                } else {
                    tab.addTab(tab.newTab().setText("" + subCategory.getJSONObject(k).get("sub_cat_name")));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        PreferenceStorage.saveSubCatClick(getApplicationContext(), categoryArrayList.get(0).getSub_cat_id());
        SubCategoryTabAdapter adapter = new SubCategoryTabAdapter
                (getSupportFragmentManager(), tab.getTabCount(), categoryArrayList);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        tab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String id = "";
                id = categoryArrayList.get(tab.getPosition()).getSub_cat_id();
                PreferenceStorage.saveSubCatClick(getApplicationContext(), id);
                if (!PreferenceStorage.getPurchaseStatus(getApplicationContext())) {
                    viewPager.setCurrentItem(tab.getPosition());
                    viewPager.performClick();
                } else {
                    tabPosition = tab.getPosition();
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(SubCategoryActivity.this);
                    alertDialogBuilder.setTitle(R.string.cart);
                    alertDialogBuilder.setMessage(R.string.cart_clear_1);
                    alertDialogBuilder.setPositiveButton(R.string.proceed_more, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            clearCart();
                            PreferenceStorage.savePurchaseStatus(SubCategoryActivity.this, false);
                            viewPager.setCurrentItem(tabPosition);
                            viewPager.performClick();
                             recreate();
                        }
                    });
                    alertDialogBuilder.setNegativeButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();
                }

            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
//                 recreate();
                String id = "";
                id = categoryArrayList.get(tab.getPosition()).getSub_cat_id();
                PreferenceStorage.saveSubCatClick(getApplicationContext(), id);
                if (!PreferenceStorage.getPurchaseStatus(getApplicationContext())) {
                    viewPager.setCurrentItem(tab.getPosition());
                    viewPager.performClick();
                } else {
                    tabPosition = tab.getPosition();
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(SubCategoryActivity.this);
                    alertDialogBuilder.setTitle(R.string.cart);
                    alertDialogBuilder.setMessage(R.string.cart_clear);
                    alertDialogBuilder.setPositiveButton(R.string.alert_button_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            clearCart();
                            PreferenceStorage.savePurchaseStatus(SubCategoryActivity.this, false);
                            viewPager.setCurrentItem(tabPosition);
                            viewPager.performClick();
                            recreate();
                        }
                    });
                    alertDialogBuilder.setNegativeButton(R.string.alert_button_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    alertDialogBuilder.show();
                }
            }
        });
//        tab.removeOnTabSelectedListener(TabLayout.OnTabSelectedListener);
//Bonus Code : If your tab layout has more than 2 tabs then tab will scroll other wise they will take whole width of the screen
        if (tab.getTabCount() <= 2) {
            tab.setTabMode(TabLayout.
                    MODE_FIXED);
        } else {
            tab.setTabMode(TabLayout.
                    MODE_SCROLLABLE);
        }
    }

    private void clearCart() {
        res = "clear";
        JSONObject jsonObject = new JSONObject();
        String id = "";
        id = PreferenceStorage.getUserId(getApplicationContext());
        try {
            jsonObject.put(SkilExConstants.USER_MASTER_ID, id);

        } catch (JSONException e) {
            e.printStackTrace();
        }

//        progressDialogHelper.showProgressDialog(getString(R.string.progress_loading));
        String url = SkilExConstants.BUILD_URL + SkilExConstants.CLEAR_CART;
        serviceHelper.makeGetServiceCall(jsonObject.toString(), url);
    }


    public void setrates() {
        String rate = PreferenceStorage.getRate(this);
        String count = PreferenceStorage.getServiceCount(this);

        rateCount.setText(": " + count + " | ₹" + rate);
        if (count.isEmpty()) {
            summaryLayout.setVisibility(View.GONE);
        } else {
            summaryLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v == summary) {
            Boolean abc;
            if (!PreferenceStorage.getServiceCount(this).equalsIgnoreCase("0")) {
                if (PreferenceStorage.getPurchaseStatus(this)) {
                    Intent i = new Intent(this, BookingSummaryAcivity.class);
                    i.putExtra("page", "summary");
                    i.putExtra("cat", category);
                    startActivity(i);
                    finish();
                }
//            handler.removeCallbacksAndMessages(null);
            }
        }
    }

}