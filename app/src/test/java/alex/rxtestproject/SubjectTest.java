package alex.rxtestproject;

import org.junit.Test;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;
import io.reactivex.schedulers.TestScheduler;
import io.reactivex.subjects.AsyncSubject;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;
import io.reactivex.subjects.ReplaySubject;

public class SubjectTest {

    @Test
    public void publishSubjectTest() {
        PublishSubject<String> subject = PublishSubject.create();

        subject.onNext("one");

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        subject.onNext("two");
        subject.onNext("three");

        observer.assertValueCount(2);
        observer.assertValues("two","three");
    }

    @Test
    public void behaviorSubjectTest() {
        BehaviorSubject<String> subject = BehaviorSubject.create();

        subject.onNext("one");

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        subject.onNext("two");
        subject.onNext("three");

        observer.assertValueCount(3);
        observer.assertValues("one", "two","three");
    }

    @Test
    public void replaySubjectTest() {
        ReplaySubject<String> subject = ReplaySubject.create();

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        observer.assertValueCount(3);
        observer.assertValues("one", "two","three");
    }

    @Test
    public void asyncSubjectTest() {
        AsyncSubject<String> subject = AsyncSubject.create();

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        subject.onNext("one");
        subject.onNext("two");
        subject.onNext("three");

        observer.assertNoValues();

        subject.onComplete();

        observer.assertValueCount(1);
        observer.assertValues("three");
    }

    @Test
    public void publishSubjectErrorTest() {
        PublishSubject<String> subject = PublishSubject.create();

        subject.onNext("one");

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        subject.onNext("two");
        subject.onNext("three");

        subject.onError(new Exception("sad story"));
        subject.onNext("four");

        observer.assertValueCount(2);
        observer.assertValues("two","three");
        observer.assertError(error -> "sad story".equals(error.getMessage()));
    }

    @Test
    public void behaviorSubjectCompleteTest() {
        BehaviorSubject<String> subject = BehaviorSubject.create();

        subject.onNext("one");

        TestObserver<String> observer = new TestObserver<>();
        subject.subscribeWith(observer);

        subject.onNext("two");
        subject.onNext("three");
        subject.onComplete();

        TestObserver<String> observer1 = new TestObserver<>();
        subject.subscribeWith(observer1);

        subject.onNext("four");

        observer.assertComplete();
        observer.assertValueCount(3);
        observer.assertValues("one", "two","three");

        observer1.assertNoValues();
        observer1.assertComplete();
    }

    @Test
    public void subjectScheduler() {
        TestScheduler scheduler = new TestScheduler();
        PublishSubject<String> subject = PublishSubject.create();
        TestObserver<String> observer = new TestObserver<>();
        subject.delay(500, TimeUnit.MILLISECONDS, scheduler)
                .subscribeOn(scheduler)
                .subscribeWith(observer);

        subject.onNext("val");

        scheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS);

        observer.assertNoValues();
        observer.assertNoErrors();

        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);

        observer.assertNoValues();
        observer.assertNoErrors();
    }

    @Test
    public void justScheduler() {
        TestScheduler scheduler = new TestScheduler();
        TestObserver<String> observer = new TestObserver<>();
        Observable.just("val").delay(500, TimeUnit.MILLISECONDS, scheduler)
                .subscribeWith(observer);

        scheduler.advanceTimeBy(499, TimeUnit.MILLISECONDS);

        observer.assertNoValues();
        observer.assertNoErrors();

        scheduler.advanceTimeBy(1, TimeUnit.MILLISECONDS);

        observer.assertValue("val");
        observer.assertNoErrors();
    }
}
