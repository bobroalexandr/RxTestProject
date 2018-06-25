package alex.rxtestproject;

import org.junit.Test;

import java.util.Arrays;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ImperativeDeclarative {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void impDeclSum() {
        int[] data = {1,2,3,4,5};

        int sum = 0;
        for (int val : data) {
            sum += val;
        }

        int sumDecl = Arrays.stream(data).reduce((a, b) -> a + b).getAsInt();

        assertEquals(sum, sumDecl);
    }

    @Test
    public void transformIssue() {
        //wrong way
        Observable<String> dataObservable = Observable.just("wrong");
        subscribeForUI(dataObservable);
        dataObservable.subscribe(this::updateUI);

        //reactive way
        Observable.just("correct")
                .compose(uiSubscribeTransformer())
                .subscribe(this::updateUI);
    }

    <T> ObservableTransformer<T, T> uiSubscribeTransformer() {
        return upstream -> upstream
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    void updateUI(String value) {}

    void subscribeForUI(Observable<String> observable) {
        observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    Observable<String> grabData() {
        return Observable.just("value");
    }

    public void tokenValidationIssue() {
        //wrong way
        String token = "someToken";
        if (isTokenValid(token)) {
            grabData().subscribe(this::doRequest);
        }

        //reactive way
        tokenStream()
                .filter(this::isTokenValid)
                .firstOrError()
                .flatMapObservable(value -> grabData())
                .subscribe(this::doRequest);
    }

    private Observable<String> tokenStream() {
        return Observable.just("someToken");
    }

    private boolean isTokenValid(String token) {
        return token.startsWith("correct");
    }

    public void doRequest(String data) {
        //dummy method
    }

    @Test
    public void someTest() {
        TestObserver<String> subscriber = new TestObserver<>();
        BehaviorSubject<String> subject = BehaviorSubject.create();

        subject.filter(this::isTokenValid)
                .firstOrError()
                .subscribe(subscriber);

        subject.onNext("a");
        subject.onNext("correct");
        subject.onNext("b");
        subject.onNext("correct1");
        subject.onNext("e");

        subscriber.assertSubscribed();
        subscriber.assertNoErrors();
        subscriber.assertValueCount(1);
    }
}