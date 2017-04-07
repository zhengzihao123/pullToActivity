package test.jiyun.com.pulltoactivity;

import android.app.Application;

import retrofit2.Retrofit;

/**
 * Created by ASUS on 2017/4/5.
 */

public class App extends Application {
    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl("http://www.oschina.net/")
            .build();

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public static Retrofit getRetrofit() {
        return retrofit;
    }
}
