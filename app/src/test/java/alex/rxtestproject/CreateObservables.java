package alex.rxtestproject;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

public class CreateObservables {

    @Test
    public void just() {
        List<String> values = new ArrayList<>();
        //never call method inside Observable.just
        Observable.just(doSomeAction(values, "just"));
        Observable.fromCallable(() -> doSomeAction(values, "fromCallable"));

        assertTrue(values.contains("just"));
        assertFalse(values.contains("fromCallable"));
    }

    @Test
    public void createDefer() {
        Observable<Long> created = Observable.create(new CreateSubscribe(System.nanoTime()));
        Observable<Long> defered = Observable.defer(() -> Observable.create(new CreateSubscribe(System.nanoTime())));

        long firstValueCreated = created.blockingFirst();
        long secondValueCreated = created.blockingFirst();
        assertEquals(firstValueCreated, secondValueCreated);

        long firstValueDefered = defered.blockingFirst();
        long secondValueDefered = defered.blockingFirst();
        assertNotEquals(firstValueDefered, secondValueDefered);
    }

    class CreateSubscribe implements ObservableOnSubscribe<Long> {

        long time;

        CreateSubscribe(long time) {
            this.time = time;
        }

        @Override
        public void subscribe(ObservableEmitter<Long> e) {
            e.onNext(time);
            e.onComplete();
        }
    }


    private String doSomeAction(List<String> values, String value) {
        values.add(value);
        System.out.println(value);
        return value;
    }

}
