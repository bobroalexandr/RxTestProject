package alex.rxtestproject;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.gson.GsonBuilder;
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

import java.util.List;
import java.util.function.Consumer;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private Handler handler;
    private EditText repoName;
    private RecyclerView reposList;
    private ProgressBar progress;
    private RetrofitService retrofitService;

    private Runnable searchRepos = () -> searchRepos(repoName.getText().toString());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handler = new Handler(Looper.getMainLooper());

        setContentView(R.layout.activity_main);

        progress = findViewById(R.id.progress);
        repoName = findViewById(R.id.name);
        reposList = findViewById(R.id.list);
        reposList.setLayoutManager(new LinearLayoutManager(this));

        initRetrofitClient();

        findViewById(R.id.search).setOnClickListener(view -> searchRepos(repoName.getText().toString()));
//        searchOnTextChanges();

    }

    private boolean isValid(String query) {
        return !TextUtils.isEmpty(query) && query.length() > 2;
    }

    void searchRepos(String query) {
        if (!isValid(query)) return;
        showProgress();
        Call<ReposResponse> call = retrofitService.getRepositories(query);
        call.enqueue(new Callback<ReposResponse>() {
            @Override
            public void onResponse(Call<ReposResponse> call, retrofit2.Response<ReposResponse> response) {
                hideProgress();
                storeToDb(response.body().items);
            }

            @Override
            public void onFailure(Call<ReposResponse> call, Throwable t) {
                hideProgress();
                reposList.setAdapter(null);
                t.printStackTrace();
            }
        });
    }

    void storeToDb(List<Repository> repositories) {
        new DbSaver(repositories, repos -> {
            reposList.setAdapter(new ReposAdapter(repos));
            hideProgress();
        }).execute();
    }

    static class DbSaver extends AsyncTask<Void,Void,List<Repository>> {

        private List<Repository> repositories;
        private Consumer<List<Repository>>  listConsumer;

        DbSaver(List<Repository> repositories, Consumer<List<Repository>> listConsumer) {
            this.repositories = repositories;
            this.listConsumer = listConsumer;
        }

        @Override
        protected List<Repository> doInBackground(Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return repositories;
        }

        @Override
        protected void onPostExecute(List<Repository> repositories) {
            super.onPostExecute(repositories);
            listConsumer.accept(repositories);
        }
    }

    void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    void hideProgress() {
        progress.setVisibility(View.GONE);
    }


    private void searchOnTextChanges() {
        repoName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                handler.removeCallbacks(searchRepos);
                handler.postDelayed(searchRepos, 500);
            }
        });
    }

    private void initRetrofitClient() {
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
        retrofitService = retrofit.create(RetrofitService.class);
    }

}
