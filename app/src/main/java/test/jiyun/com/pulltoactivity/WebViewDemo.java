package test.jiyun.com.pulltoactivity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

public class WebViewDemo extends Activity {

    private WebView webView;

    private String url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view_demo);
        webView = (WebView) findViewById(R.id.Web_View);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        webView.getSettings().setLoadWithOverviewMode(true);

        String id = getIntent().getStringExtra("id");
        String url = getIntent().getStringExtra("url");
        //覆盖WebView默认使用第三方或系统默认浏览器打开网页的行为，使网页用WebView打开
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }

        });
        if (id != null) {
            Log.i("走id---->", id);
            request(id);
        } else if (url != null) {
            Log.i("走url---->", url);
            webView.loadUrl(url);
        }


    }

    private void request(String id) {

        Retrofit retrofit = App.getRetrofit();
        Demo demo = retrofit.create(Demo.class);
        Call<ResponseBody> call = demo.call(id);
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
                        DefaultHandlerTwo handler = new DefaultHandlerTwo();
                        // 将事件处理器设置给事件源
                        xmlr.setContentHandler(handler);
                        // 录入数据 就是指向那个文件
                        xmlr.parse(new InputSource(response.body().byteStream()));
                        // xmlr.parse("src/cat.xml");
                        url = handler.getUrl();
                        webView.loadUrl(url);
                    } catch (Exception e) {
                        e.printStackTrace();
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
        @GET("action/api/news_detail")
        Call<ResponseBody> call(@Query("id") String id);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Log.i("退出---->", "进来了");
            if (webView.canGoBack()) {
                Log.i("退出---->", "GoBack");
                webView.goBack();//返回上一页面
                return true;
            } else {
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
