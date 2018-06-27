package alex.rxtestproject;


import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;

import static junit.framework.Assert.assertTrue;

public class Recursion {

    @Test
    public void recursionTest() {
        Observable<ArrayList<Integer>> recursive = getValuesRecursively(0, 5, 19);
        List<Integer> items = recursive.firstOrError().blockingGet();
        assertTrue(items.size() == 20);
    }

    Observable<ArrayList<Integer>> getValuesRecursively(int pageNumber, int pageSize, int maxItems) {
        return Observable.range(pageNumber * pageSize, pageSize).toList()
                .flatMapObservable(items -> {
                    if (shouldStopLoading(items, maxItems)) {
                        return Observable.just(items);
                    } else {
                        return Observable.just(items)
                                .concatWith(getValuesRecursively(pageNumber + 1, pageSize, maxItems));
                    }
                })
                .collectInto(new ArrayList<Integer>(), ArrayList::addAll)
                .toObservable();
    }

    boolean shouldStopLoading(List<Integer> items, int maxItems) {
        return items.get(items.size() - 1) >= maxItems;
    }
}
