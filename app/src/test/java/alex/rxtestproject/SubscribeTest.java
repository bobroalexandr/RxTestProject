package alex.rxtestproject;

import org.junit.Test;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;
import io.reactivex.subjects.PublishSubject;

public class SubscribeTest {

    @Test
    public void disposeTest() {
        PublishSubject<String> subject = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();

        Disposable disposable = subject.subscribeWith(observer);
        subject.onNext("a");
        subject.onNext("b");
        disposable.dispose();
        subject.onNext("c");
        subject.onNext("d");

        observer.assertValues("a","b");
    }

    @Test
    public void composeDisposeTest() {
        PublishSubject<String> subject = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();
        TestObserver<String> observer2 = new TestObserver<>();

        CompositeDisposable disposable = new CompositeDisposable();

        disposable.add(subject.subscribeWith(observer));
        disposable.add(subject.subscribeWith(observer2));
        subject.onNext("a");
        subject.onNext("b");
        disposable.dispose();
        subject.onNext("c");
        subject.onNext("d");

        observer.assertValues("a","b");
        observer2.assertValues("a","b");
    }


    @Test
    public void takeUntilObservableTest() {
        PublishSubject<String> subject = PublishSubject.create();
        PublishSubject<String> stopper = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();

        subject.takeUntil(stopper).subscribeWith(observer);
        subject.onNext("a");
        subject.onNext("b");
        stopper.onNext("stop");
        subject.onNext("c");
        subject.onNext("d");

        observer.assertValues("a","b");
    }

    @Test
    public void takeUntilTest() {
        PublishSubject<String> subject = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();

        subject.takeUntil((Predicate<String>) "c"::equals).subscribeWith(observer);
        subject.onNext("a");
        subject.onNext("b");
        subject.onNext("c");
        subject.onNext("d");

        observer.assertValues("a","b","c");
    }

    @Test
    public void takeWhileTest() {
        PublishSubject<String> subject = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();

        subject.takeWhile(val -> !val.equals("c")).subscribeWith(observer);
        subject.onNext("a");
        subject.onNext("b");
        subject.onNext("c");
        subject.onNext("d");

        observer.assertValues("a","b");
    }
}

