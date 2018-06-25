package alex.rxtestproject;

import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AppManager {

    private static AppManager instance;

    public static AppManager getInstance() {
        if (instance == null) {
            instance = new AppManager();
        }
        return instance;
    }

    private RxRetrofitService rxRetrofitService;
    private ListItemsCache<Repository> repositoryMemoryCache;

    private AppManager() {
        initRxRetrofitClient();
        repositoryMemoryCache = new ListItemsCache<>();
    }

    private void initRxRetrofitClient() {
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .build();
        GsonBuilder builder = new GsonBuilder();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.github.com")
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(builder.create()))
                .client(client)
                .build();
        rxRetrofitService = retrofit.create(RxRetrofitService.class);
    }

    public RxRetrofitService getRxRetrofitService() {
        return rxRetrofitService;
    }

    public ListItemsCache<Repository> getRepositoryCache() {
        return repositoryMemoryCache;
    }
}
