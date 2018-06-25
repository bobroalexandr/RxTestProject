package alex.rxtestproject;

import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.TestObserver;

public class ErrorHandling {

    @Test
    public void withoutOnError() {
        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .subscribe(System.out::println);
    }

    @Test
    public void doOnError() {
        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .doOnError(error -> System.out.println(error.getMessage()))
                .subscribe(System.out::println);
    }

    @Test
    public void defaultOnError() {
        TestObserver<String> observer = new TestObserver<>();

        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .subscribeWith(observer);

        observer.assertValueCount(2);
        observer.assertValues("a","b");
        observer.assertErrorMessage("I hate 'c' character");
    }

    @Test
    public void withOnErrorReturn() {
        TestObserver<String> observer = new TestObserver<>();

        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .onErrorReturn(error -> "z")
                .subscribeWith(observer);

        observer.assertValueCount(3);
        observer.assertValues("a","b","z");
        observer.assertNoErrors();
    }

    @Test
    public void withOnErrorMap() {
        TestObserver<String> observer = new TestObserver<>();

        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .concatMap(val -> Observable.just(val).doOnNext(actionWithError).onErrorReturnItem("z"))
                .subscribeWith(observer);

        observer.assertValueCount(5);
        observer.assertValues("a","b","z", "d", "e");
        observer.assertNoErrors();
    }

    @Test
    public void withOnErrorResumeNext() {
        TestObserver<String> observer = new TestObserver<>();

        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .onErrorResumeNext(Observable.fromArray("x","y","z"))
                .subscribeWith(observer);

        observer.assertValueCount(5);
        observer.assertValues("a","b","x","y", "z");
        observer.assertNoErrors();
    }


    @Test
    public void onErrorRetry() {
        TestObserver<String> observer = new TestObserver<>();

        Consumer<String> actionWithError = value -> {
            if ("c".equals(value)) {
                throw new Exception("I hate 'c' character");
            }
        };
        Observable.fromArray("a","b","c","d","e")
                .doOnNext(actionWithError)
                .retry(2)
                .subscribeWith(observer);

        observer.assertValueCount(6);
        observer.assertValues("a","b","a","b","a","b");
    }
}
