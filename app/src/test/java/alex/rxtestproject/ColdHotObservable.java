package alex.rxtestproject;

import org.junit.Test;

import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observables.ConnectableObservable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertSame;

public class ColdHotObservable {

    @Test
    public void coldObservable() {
        TestObserver<Integer> observer1 = new TestObserver<>();
        TestObserver<Integer> observer2 = new TestObserver<>();
        Observable<Integer> cold = Observable.create(e -> {
            e.onNext(new Random().nextInt());
            e.onComplete();
        });
        cold.subscribe(observer1);
        cold.subscribe(observer2);
        assertNotSame(observer1.values().get(0), observer2.values().get(0));
    }

    @Test
    public void hotObservable() {
        TestObserver<Integer> observer1 = new TestObserver<>();
        TestObserver<Integer> observer2 = new TestObserver<>();
        Observable<Integer> cold = Observable.create(e -> {
            e.onNext(new Random().nextInt());
            e.onComplete();
        });
        ConnectableObservable<Integer> hot = cold.publish();
        hot.subscribe(observer1);
        hot.subscribe(observer2);
        hot.connect();
        assertSame(observer1.values().get(0), observer2.values().get(0));
    }

    @Test
    public void observableRestartAfterAllDisposedFromShare() {
        TestScheduler testScheduler = new TestScheduler();
        Observable<Long> observable = Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler)
                .doOnNext(val -> System.out.println("val " + val))
                .share();

        Disposable disposable = observable.subscribe();
        for (int i = 0; i < 10; i++) {
            testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        }
        TestObserver<Long> observer = new TestObserver<>();
        observable.firstElement().subscribe(observer);
        testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        observer.assertValue(10L);
        disposable.dispose();

        TestObserver<Long> observer1 = new TestObserver<>();
        observable.firstElement().subscribe(observer1);
        testScheduler.advanceTimeBy(10, TimeUnit.MILLISECONDS);
        observer1.assertValue(0L);
    }

    @Test
    public void tokenTest() {
        TestScheduler testScheduler = new TestScheduler();
        ConnectableObservable<String> tokenObservable = Observable
                .fromCallable(this::getToken)
                .map(this::updateToken)
                .subscribeOn(testScheduler)
                .replay(1);

        TestObserver<String> observer1 = new TestObserver<>();
        TestObserver<String> observer2 = new TestObserver<>();

        tokenObservable.subscribeWith(observer1);
        tokenObservable.connect();

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);

        tokenObservable.subscribeWith(observer2);

        testScheduler.advanceTimeBy(1000, TimeUnit.MILLISECONDS);


        observer1.assertValues("new");
        observer2.assertValues("new");
    }


    String token = "old";

    String getToken() {
        return token;
    }

    String updateToken(String token) throws Exception {
        System.out.println("update token " + Thread.currentThread());
        this.token = "new";
        return this.token;
    }
}
