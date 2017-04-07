package test.jiyun.com.pulltoactivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;

import com.androidkun.PullToRefreshRecyclerView;
import com.androidkun.adapter.BaseAdapter;
import com.androidkun.adapter.ViewHolder;
import com.androidkun.callback.PullToRefreshListener;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;
import test.jiyun.com.pulltoactivity.bean.News;


public class MainActivity extends Activity {

    private PullToRefreshRecyclerView pullToRefreshRV;

    private ModeAdapter adapter;

    private List<News> data = new ArrayList<>();

    private int pageIndex = 0;

    private ImageButton mBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resquest("1", "0", "20");
        pullToRefreshRV = (PullToRefreshRecyclerView) findViewById(R.id.pullToRefreshRV_One);

        mBtn = (ImageButton) findViewById(R.id.Main_Button);

        mBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SearchActivity.class);
                startActivity(intent);
            }
        });

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
                        resquest("1", "0", "20");
                        pageIndex++;
                    }
                }, 2000);
            }

            @Override
            public void onLoadMore() {
                pullToRefreshRV.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        pullToRefreshRV.setLoadMoreComplete();
                        resquest("1", String.valueOf(pageIndex), "20");
                        pageIndex++;
                    }
                }, 2000);
            }
        });
    }

    private void resquest(String catalog, String pageIndex, String pageSize) {

        Retrofit retrofit = App.getRetrofit();
        Demo demo = retrofit.create(Demo.class);
        Call<ResponseBody> call = demo.call(catalog, pageIndex, pageSize);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    // 先构建sax解析器工厂实例
                    SAXParserFactory spf = SAXParserFactory.newInstance();
                    // 通过解析器工厂获取解析器对象
                    SAXParser sp = null;
                    try {
                        sp = spf.newSAXParser();
                        // 获取读取事件源实例
                        XMLReader xmlr = sp.getXMLReader();
                        // 实例化事件处理器对象
                        MyDefaultHandler handler = new MyDefaultHandler();
                        // 将事件处理器设置给事件源
                        xmlr.setContentHandler(handler);
                        // 录入数据 就是指向那个文件
                        xmlr.parse(new InputSource(response.body().byteStream()));
                        // xmlr.parse("src/cat.xml");
                        ArrayList<News> list = handler.getList();
                        if (list != null) {
                            data.addAll(list);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error", t + "");
            }
        });

    }

    private interface Demo {
        @GET("action/api/news_list")
        Call<ResponseBody> call(@Query("catalog") String catalog, @Query("pageIndex") String pageIndex, @Query("pageSize") String pageSize);

    }


    class ModeAdapter extends BaseAdapter<News> {


        public ModeAdapter(Context context, int layoutId, List<News> datas) {
            super(context, layoutId, datas);
        }

        @Override
        public void convert(ViewHolder holder, final News news) {
            holder.setText(R.id.Title, news.getTitle());
            holder.setText(R.id.News, news.getBody());
            holder.setText(R.id.Time, news.getPubDate());
            holder.setOnclickListener(R.id.mode_Lin, new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(MainActivity.this, WebViewDemo.class);
                    intent.putExtra("id", news.getId());
                    Log.i("id--->", news.getId());
                    startActivity(intent);
                }
            });
        }

    }
}
