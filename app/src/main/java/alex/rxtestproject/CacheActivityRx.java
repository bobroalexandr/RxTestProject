package alex.rxtestproject;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Pair;
import android.widget.Button;

import com.jakewharton.rxbinding2.view.RxView;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.Collections;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("all")
public class CacheActivityRx extends RxAppCompatActivity {

    private static final String EXTRA_QUERY = "EXTRA_QUERY";

    public static Intent createIntent(@NonNull Context context, @NonNull String queryString) {
        Intent intent = new Intent(context, CacheActivityRx.class);
        intent.putExtra(EXTRA_QUERY, queryString);
        return intent;
    }

    private Button loadMore;
    private RecyclerView reposList;

    private String queryString;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        queryString = getIntent().getStringExtra(EXTRA_QUERY);
        RxRetrofitService retrofitService = AppManager.getInstance().getRxRetrofitService();
        ListItemsCache<Repository> repositoryCache = AppManager.getInstance().getRepositoryCache();

        setContentView(R.layout.activity_cached);

        reposList = findViewById(R.id.list);
        reposList.setLayoutManager(new LinearLayoutManager(this));


        repositoryCache.getItems()
                .toObservable()
                .compose(this::lifeCycleTransform)
                .subscribe(this::setAdapter);

        Observable<List<Repository>> loadMoreRepos =
                Observable.zip(Observable.just(queryString), Observable.fromCallable(() -> repositoryCache.size() / RxRetrofitService.ITEMS_ON_PAGE), Pair::create)
                        .switchMap(pair -> retrofitService.getRepositories(pair.first, pair.second))
                        .map(resp -> resp.body().items)
                        .doOnNext(repositoryCache::insertItems)
                        .onErrorReturn(error -> Collections.emptyList())
                        .compose(this::lifeCycleTransform)
                        .share();

        RxView.clicks(findViewById(R.id.load_more))
                .flatMap(ignore -> loadMoreRepos)
                .subscribe();
    }

    void setAdapter(List<Repository> repositories) {
        reposList.setAdapter(new ReposAdapter(repositories));
    }

    <T> Observable<T> lifeCycleTransform(Observable<T> upstream) {
        return upstream.compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
