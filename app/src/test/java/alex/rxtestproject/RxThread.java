package alex.rxtestproject;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;

import static junit.framework.Assert.assertTrue;

public class RxThread {

    @Test
    public void subscribeOnTest() {
        Scheduler first = Schedulers.single();
        Scheduler second = Schedulers.newThread();
        Observable<String> observable = Observable.just("value")
                .subscribeOn(first)
                .subscribeOn(second)
                .map(val -> Thread.currentThread().getName());
        String name = observable.blockingFirst();
        assertTrue(isFirstScheduler(name));
    }

    @Test
    public void observeOnTest() {
        Scheduler first = Schedulers.single();
        Scheduler second = Schedulers.newThread();
        Observable<String> observable = Observable.just("value")
                .observeOn(first)
                .observeOn(second)
                .map(val -> Thread.currentThread().getName());
        String name = observable.blockingFirst();
        assertTrue(isSecondScheduler(name));
    }

    @Test
    public void observeOnAfterSubscribeOnTest() {
        Scheduler first = Schedulers.single();
        Scheduler second = Schedulers.newThread();
        Observable<String> observable = Observable.just("value")
                .subscribeOn(first)
                .observeOn(second)
                .map(value -> Thread.currentThread().getName());
        String name = observable.blockingFirst();
        assertTrue(isSecondScheduler(name));
    }

    @Test
    public void observeOnBeforeSubscribeOnTest() {
        TestObserver<String> testObserver = new TestObserver<>();
        Scheduler first = Schedulers.single();
        Scheduler second = Schedulers.newThread();
        Observable<String> observable = Observable.just("value")
                .map(value -> Thread.currentThread().getName())
                .doOnNext(testObserver::onNext)
                .observeOn(first)
                .subscribeOn(second)
                .map(value -> Thread.currentThread().getName())
                .doOnNext(testObserver::onNext);
        observable.blockingFirst();
        testObserver.assertValueCount(2);
        testObserver.assertValueAt(0, this::isSecondScheduler);
        testObserver.assertValueAt(1, this::isFirstScheduler);
    }

    @Test
    public void flatMapTest() {
        Scheduler first = Schedulers.single();
        Scheduler second = Schedulers.newThread();
        Observable<String> observable1 = Observable.just("val1").observeOn(first);
        Observable<String> observable2 = Observable.just("val2").subscribeOn(second);
        String name = observable1.flatMap(val -> observable2)
                .map(val -> Thread.currentThread().getName())
                .blockingFirst();
        assertTrue(isSecondScheduler(name));
    }

    private boolean isFirstScheduler(String value) {
        return value.startsWith("RxSingle");
    }

    private boolean isSecondScheduler(String value) {
        return value.startsWith("RxNew");
    }

}
