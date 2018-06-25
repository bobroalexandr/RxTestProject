package alex.rxtestproject;

import java.util.Collection;

import io.reactivex.Flowable;

public interface CollectionCache<T, C extends Collection<T>> {

    Flowable<C> getItems();

    void insertItems(C additionalItems);

    void updateItems(C newItems);

    int size();
}
