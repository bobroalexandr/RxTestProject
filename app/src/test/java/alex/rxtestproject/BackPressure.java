package alex.rxtestproject;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subscribers.TestSubscriber;

public class BackPressure {

    @Test
    public void distinct() {
        TestObserver<Integer> testObserver = new TestObserver<>();
        Observable.fromArray(1,1,1,2,3,3,1,2,4)
                .distinct()
                .subscribeWith(testObserver);

        testObserver.assertValueCount(4);
        testObserver.assertValues(1,2,3,4);
    }

    @Test
    public void debounce() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<Integer> testObserver = new TestObserver<>();

        Observable.fromArray(1,2,3,4,5)
                .concatMap(val -> Observable.just(val).delay(val % 2 == 1 ? 10 : 20, TimeUnit.MILLISECONDS, testScheduler))
                .debounce(15, TimeUnit.MILLISECONDS, testScheduler)
                .subscribeWith(testObserver);

        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        testObserver.assertValueCount(3);
        testObserver.assertValues(1,3,5);
    }

    @Test
    public void buffer() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<Long> testObserver = new TestObserver<>();

        Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler)
                .buffer(5)
                .map(list -> list.get(0))
                .subscribeWith(testObserver);

        testScheduler.advanceTimeBy(100, TimeUnit.MILLISECONDS);

        testObserver.assertValueCount(2);
        testObserver.assertValues(0L, 5L);
    }

    @Test
    public void bufferTime() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<Long> testObserver = new TestObserver<>();

        Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler)
                .buffer(55, TimeUnit.MILLISECONDS, testScheduler)
                .map(list -> list.get(list.size() - 1))
                .subscribeWith(testObserver);

        testScheduler.advanceTimeBy(110, TimeUnit.MILLISECONDS);

        testObserver.assertValueCount(2);
        testObserver.assertValues(4L, 9L);
    }

    @Test
    public void defaultBehaviorTest() {
        TestObserver<String> testObserver = new TestObserver<>();
        Observable.interval(30, TimeUnit.MILLISECONDS)
                .takeWhile(val -> val < 20)
                .observeOn(Schedulers.single())
                .map(this::doLongOperation)
                .blockingSubscribe(testObserver);

        testObserver.assertValueCount(20);
    }

    @Test
    public void flowableDropTest() {
        TestSubscriber<String> testObserver = new TestSubscriber<>();
        Observable.interval(30, TimeUnit.MILLISECONDS)
                .takeWhile(val -> val < 20)
                .toFlowable(BackpressureStrategy.DROP)
                .observeOn(Schedulers.single(), false, 1)
                .map(this::doLongOperation)
                .blockingSubscribe(testObserver);

        testObserver.assertValueCount(4);
        testObserver.assertValues("changed0", "changed6", "changed12", "changed18");
    }

    @Test
    public void flowableLatestTest() {
        TestSubscriber<String> testObserver = new TestSubscriber<>();
        Observable.interval(30, TimeUnit.MILLISECONDS)
                .takeWhile(val -> val < 20)
                .toFlowable(BackpressureStrategy.LATEST)
                .observeOn(Schedulers.single(), false, 1)
                .map(this::doLongOperation)
                .blockingSubscribe(testObserver);

        testObserver.assertValueCount(5);
        testObserver.assertValues("changed0", "changed5", "changed10", "changed15", "changed19");
    }

    private String doLongOperation(long val) {
        try {
            Thread.sleep(151);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "changed" + val;
    }
}
