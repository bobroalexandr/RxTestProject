package alex.rxtestproject;

import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.jakewharton.rxbinding2.view.RxView;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import java.util.Collections;
import java.util.List;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("all")
public class MainActivityRx extends RxAppCompatActivity {

    private RxRetrofitService retrofitService;
    private ListItemsCache<Repository> repositoryCache;

    private CheckBox searchCB;
    private EditText repoName;
    private RecyclerView reposList;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        retrofitService = AppManager.getInstance().getRxRetrofitService();
        repositoryCache = AppManager.getInstance().getRepositoryCache();

        setContentView(R.layout.activity_main);

        searchCB = findViewById(R.id.searchCB);
        progress = findViewById(R.id.progress);
        repoName = findViewById(R.id.name);
        reposList = findViewById(R.id.list);
        reposList.setLayoutManager(new LinearLayoutManager(this));

        ObservableTransformer<String, List<Repository>> searchRepos =
                actions -> actions.switchMap(this::loadRepos)
                        .compose(bindToLifecycle())
                        .observeOn(AndroidSchedulers.mainThread());

        Observable<String> textObservable =
                RxTextView.afterTextChangeEvents(repoName)
                        .map(event -> event.editable().toString())
                        .map(String::trim)
                        .filter(this::isValid);

        textObservable
                .filter(ignore -> searchCB.isChecked())
                .doOnNext(ignore -> showProgress())
                .toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.single(), false, 1)
                .toObservable()
                .compose(searchRepos)
                .subscribe(this::setAdapter);

        RxView.clicks(findViewById(R.id.search))
                .filter(ignore -> !searchCB.isChecked())
                .withLatestFrom(textObservable, (view, text) -> text)
                .doOnNext(ignore -> showProgress())
                .observeOn(Schedulers.single())
                .compose(searchRepos)
                .subscribe(this::setAdapter);

        RxView.clicks(findViewById(R.id.load_more_screen))
                .flatMap(ignore -> textObservable)
                .subscribe(this::startLoadMoreScreen);
    }

    void startLoadMoreScreen(String query) {
        startActivity(CacheActivityRx.createIntent(MainActivityRx.this, query));
    }

    void setAdapter(List<Repository> repositories) {
        hideProgress();
        reposList.setAdapter(new ReposAdapter(repositories));
    }

    boolean isValid(String query) {
        return !TextUtils.isEmpty(query) && query.length() > 2;
    }

    Observable<List<Repository>> loadRepos(String query) {
        return retrofitService.getRepositories(query, 0)
                .map(resp -> resp.body().items)
                .doOnNext(this::saveToDb)
                .onErrorReturn(error -> Collections.emptyList());
    }

    void saveToDb(List<Repository> repositories) throws Exception {
        Thread.sleep(500);
        repositoryCache.updateItems(repositories);
    }

    void showProgress() {
        progress.setVisibility(View.VISIBLE);
    }

    void hideProgress() {
        progress.setVisibility(View.GONE);
    }
}
