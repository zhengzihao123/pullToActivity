package test.jiyun.com.pulltoactivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.androidkun.PullToRefreshRecyclerView;
import com.androidkun.adapter.BaseAdapter;
import com.androidkun.adapter.ViewHolder;
import com.androidkun.callback.PullToRefreshListener;
import com.thoughtworks.xstream.XStream;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import test.jiyun.com.pulltoactivity.bean.Software;

public class SearchActivity extends Activity {

    private EditText mEdit;
    private TextView mText;

    private PullToRefreshRecyclerView pullToRefreshRV;

    private ModeAdapter adapter;

    private List<Software.ResultBean> data = new ArrayList<>();

    private int pageIndex = 1;

    private String content;

    private ListView mList;

    private SharedPreferences mShared;
    private SharedPreferences.Editor mEditor;

    private List<String> list = new ArrayList<>();

    private MyAdapter myAdapter;

    private Dialog dia;

    private View view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        mShared = getSharedPreferences("data", MODE_PRIVATE);
        mEditor = mShared.edit();

        Set<String> set = mShared.getStringSet("list", null);
        mList = (ListView) findViewById(R.id.Search_List);
        if (set != null) {
            list.addAll(set);
        } else {
            mList.setVisibility(View.GONE);
        }

        init();

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        pullToRefreshRV.setLayoutManager(layoutManager);
        adapter = new ModeAdapter(this, R.layout.item_mode, data);
        pullToRefreshRV.setAdapter(adapter);
        //是否开启下拉刷新功能
        pullToRefreshRV.setPullRefreshEnabled(true);
        //是否开启上拉加载功能
        pullToRefreshRV.setLoadingMoreEnabled(true);
        //设置是否显示上次刷新的时间
        pullToRefreshRV.displayLastRefreshTime(true);
        //设置刷新回调
        pullToRefreshRV.setPullToRefreshListener(new PullToRefreshListener() {
            @Override
            public void onRefresh() {
                pullToRefreshRV.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshRV.setRefreshComplete();
                        data.clear();
                        resquest("software", content, "0", "20");
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                pullToRefreshRV.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshRV.setLoadMoreComplete();
                        resquest("software", content, String.valueOf(pageIndex), "20");
                        pageIndex++;
                    }
                }, 2000);
            }
        });

    }

    private void init() {
        mEdit = (EditText) findViewById(R.id.Search_Edit);
        mText = (TextView) findViewById(R.id.Search_Text);
        pullToRefreshRV = (PullToRefreshRecyclerView) findViewById(R.id.Search_Pull);
        myAdapter = new MyAdapter();
        dialog();
        mList.addFooterView(view);
        mList.setAdapter(myAdapter);
        mEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    mText.setText("搜索");
                } else {
                    mText.setText("取消");
                    mList.setVisibility(View.VISIBLE);
                    pullToRefreshRV.setVisibility(View.GONE);
                    if (mShared.getStringSet("list", null) != null) {
                        list.clear();
                        list.addAll(mShared.getStringSet("list", null));
                        myAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content = mEdit.getText().toString();
                if (mText.getText().toString().equals("取消")) {
                    finish();
                } else if (mText.getText().toString().equals("搜索")) {
                    Set<String> set = mShared.getStringSet("list", null);
                    if (set == null) {
                        Set<String> set_ = new HashSet<>();
                        set_.add(content);
                        mEditor.putStringSet("list", set_);
                        mEditor.commit();
                    } else {
                        if (!set.contains(content)) {
                            set.add(content);
                            mEditor.putStringSet("list", set);
                            mEditor.commit();
                        }
                    }
                    mList.setVisibility(View.GONE);
                    pullToRefreshRV.setVisibility(View.VISIBLE);
                    data.clear();
                    resquest("software", content, "0", "20");
                }
            }
        });

        mList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == list.size()) {
                    if (dia.isShowing()) {
                        dia.dismiss();
                    } else {
                        dia.show();
                    }
                } else {
                    mEdit.setText(list.get(position));
                }
            }
        });

    }

    private void dialog() {
        view = View.inflate(SearchActivity.this, R.layout.activity_footview, null);
        dia = new AlertDialog.Builder(SearchActivity.this)
                .setTitle("提示")
                .setMessage("确定要清空搜索记录吗？")
                .setNegativeButton("取消", null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mList.setVisibility(View.GONE);
                        list.clear();
                        mEditor.clear();
                        mEditor.commit();
                    }
                }).create();
    }


    private void resquest(String catalog, String content, String pageIndex, String pageSize) {

        Retrofit retrofit = App.getRetrofit();
        Demo demo = retrofit.create(Demo.class);
        Call<ResponseBody> call = demo.call(catalog, content, pageIndex, pageSize);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    XStream xStream = new XStream();

                    xStream.alias("oschina", Software.class);
                    xStream.alias("result", Software.ResultBean.class);

                    Software software = (Software) xStream.fromXML(response.body().byteStream());
                    List<Software.ResultBean> results = software.getResults();
                    if (results != null) {
                        data.addAll(results);
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.i("Error---->", t.toString());
            }
        });


    }

    private interface Demo {
        @GET("action/api/search_list")
        Call<ResponseBody> call(@Query("catalog") String catalog, @Query("content") String content, @Query("pageIndex") String pageIndex, @Query("pageSize") String pageSize);

    }


    class ModeAdapter extends BaseAdapter<Software.ResultBean> {


        public ModeAdapter(Context context, int layoutId, List<Software.ResultBean> datas) {
            super(context, layoutId, datas);
        }

        @Override
        public void convert(ViewHolder holder, final Software.ResultBean resultBean) {
            holder.setText(R.id.Title, resultBean.getTitle());
            holder.setText(R.id.News, resultBean.getDescription());
            holder.setOnclickListener(R.id.mode_Lin, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SearchActivity.this, WebViewDemo.class);
                    intent.putExtra("url", resultBean.getUrl());
                    Log.i("url--->", resultBean.getUrl());
                    startActivity(intent);
                }
            });
        }
    }

    class MyAdapter extends android.widget.BaseAdapter {

        @Override
        public int getCount() {
            return list.isEmpty() ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Holder holder = null;
            if (convertView == null) {
                holder = new Holder();
                convertView = View.inflate(SearchActivity.this, R.layout.search_item, null);
                holder.mText = (TextView) convertView.findViewById(R.id.Search_Item);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.mText.setText(list.get(position) + "");

            return convertView;
        }

        class Holder {
            private TextView mText;
        }
    }

}
