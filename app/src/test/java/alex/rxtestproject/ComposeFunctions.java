package alex.rxtestproject;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Function;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;

public class ComposeFunctions {

    @Test
    public void groupByTest() {
        Observable.range(0,20).groupBy(val -> val % 2).flatMapSingle(Observable::toList)
                .subscribe(list -> System.out.println("first " + list.get(0) + " size " + list.size()));
    }

    @Test
    public void flatMap() {
        final List<String> items = Arrays.asList("a", "b", "c", "d", "e", "f");

        final TestScheduler scheduler = new TestScheduler();

        Observable.fromIterable(items)
                .flatMap(s -> Observable.just(s + "x")
                        .delay(getRand(), TimeUnit.SECONDS, scheduler))
                .subscribe(System.out::println);

        scheduler.advanceTimeBy(1, TimeUnit.MINUTES);
    }

    @Test
    public void switchMap() {
        final List<String> items = Arrays.asList("a", "b", "c", "d", "e", "f");

        final TestScheduler scheduler = new TestScheduler();

        Observable.fromIterable(items)
                .switchMap(s -> Observable.just(s + "x")
                        .delay(getRand(), TimeUnit.SECONDS, scheduler))
                .subscribe(System.out::println);

        scheduler.advanceTimeBy(1, TimeUnit.MINUTES);
    }

    @Test
    public void concatMap() {
        final List<String> items = Arrays.asList("a", "b", "c", "d", "e", "f");

        final TestScheduler scheduler = new TestScheduler();

        Observable.fromIterable(items)
                .concatMap(s -> Observable.just(s + "x")
                        .delay(getRand(), TimeUnit.SECONDS, scheduler))
                .subscribe(System.out::println);

        scheduler.advanceTimeBy(1, TimeUnit.MINUTES);
    }

    @Test
    public void compose() {
        final List<String> items = Arrays.asList("a", "b", "c", "d", "e", "f");

        final TestScheduler scheduler = new TestScheduler();

        //compose
        ObservableTransformer<String, List<String>> transformer =
                upstream -> upstream.map(s -> s + "x")
                        .delay(getRand(), TimeUnit.SECONDS, scheduler)
                        .toList()
                        .toObservable();

        Observable.fromIterable(items)
                .compose(transformer)
                .subscribe(System.out::println);

        scheduler.advanceTimeBy(1, TimeUnit.MINUTES);

        //flatmap
        Function<String, Observable<List<String>>> mapFunction =
                s -> Observable.just(s + "x")
                        .delay(getRand(), TimeUnit.SECONDS, scheduler)
                        .toList()
                        .toObservable();

        Observable.fromIterable(items)
                .concatMap(mapFunction)
                .subscribe(System.out::println);

        scheduler.advanceTimeBy(1, TimeUnit.MINUTES);
    }

    @Test
    public void zipExample() {
        TestScheduler testScheduler1 = new TestScheduler();
        Observable<Long> first = Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler1);
        Observable<String> second = Observable.fromArray("a","b","c").delay(10, TimeUnit.MILLISECONDS,testScheduler1);

        TestObserver<String> observer = new TestObserver<>();
        Observable.zip(first, second, (num, let) -> let + num)
                .doOnNext(System.out::println)
                .subscribeWith(observer);

        testScheduler1.advanceTimeBy(40, TimeUnit.MILLISECONDS);

        observer.assertValueCount(3);
        observer.assertValues("a0","b1","c2");
    }

    @Test
    public void latestExample() {
        TestScheduler testScheduler1 = new TestScheduler();
        Observable<Long> first = Observable.interval(10, TimeUnit.MILLISECONDS, testScheduler1);
        Observable<String> second = Observable.fromArray("a","b","c").delay(10, TimeUnit.MILLISECONDS,testScheduler1);

        TestObserver<String> observer = new TestObserver<>();
        Observable.combineLatest(first, second, (num, let) -> let + num)
                .doOnNext(System.out::println)
                .subscribeWith(observer);

        testScheduler1.advanceTimeBy(40, TimeUnit.MILLISECONDS);

        observer.assertValueCount(6);
        observer.assertValues("a0","b0","c0","c1","c2","c3");
    }

    @Test
    public void concatExample() {
        TestObserver<String> observer = new TestObserver<>();
        Observable<String> cacheObservable = Observable.just("cached");
        Observable<String> requestObservable = Observable.fromCallable(() -> "new");

        Observable.concat(cacheObservable, requestObservable)
                .subscribeWith(observer);

        observer.assertValueCount(2);
        observer.assertValues("cached", "new");
    }

    private int getRand() {
        return new Random().nextInt(10);
    }

}
