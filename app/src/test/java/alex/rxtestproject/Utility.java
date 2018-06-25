package alex.rxtestproject;

import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class Utility {

    @Test
    public void delayConcurrent() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<String> testObserver = new TestObserver<>();

        Observable.fromArray("a","b","c")
                .delay(10, TimeUnit.MILLISECONDS, testScheduler)
                .map(value -> value + testScheduler.now(TimeUnit.MILLISECONDS))
                .subscribeWith(testObserver);

        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);

        testObserver.assertValues("a10","b10","c10");
    }

    @Test
    public void delaySequence() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<String> testObserver = new TestObserver<>();

        Observable.fromArray("a","b","c")
                .concatMap(value -> Observable.just(value).delay(10, TimeUnit.MILLISECONDS, testScheduler))
                .map(value -> value + testScheduler.now(TimeUnit.MILLISECONDS))
                .subscribeWith(testObserver);

        testScheduler.advanceTimeBy(50, TimeUnit.MILLISECONDS);

        testObserver.assertValues("a10","b20","c30");
    }


    @Test
    public void timeout() {
        TestScheduler testScheduler = new TestScheduler();
        TestObserver<Long> testObserver = new TestObserver<>();

        Observable.interval(10, 100, TimeUnit.MILLISECONDS, testScheduler)
                .timeout(50, TimeUnit.MILLISECONDS, testScheduler)
                .subscribe(testObserver);

        testScheduler.advanceTimeBy(1, TimeUnit.MINUTES);

        testObserver.assertValueCount(1);
        testObserver.assertError(TimeoutException.class);
    }

}
