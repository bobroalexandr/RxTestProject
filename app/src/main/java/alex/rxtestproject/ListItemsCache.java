package alex.rxtestproject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.disposables.Disposables;

public class ListItemsCache<T> implements CollectionCache<T, List<T>>{

    private final Set<ItemsChangeListener<T>> listeners = new HashSet<>();
    private final List<T> items = Collections.synchronizedList(new ArrayList<>());

    @Override
    public Flowable<List<T>> getItems() {
        return Flowable.create(emitter -> {
            ItemsChangeListener<T> listener = emitter::onNext;
            if (!emitter.isCancelled()) {
                addListener(listener);
                emitter.setDisposable(Disposables.fromAction(() -> removeListener(listener)));
                emitter.onNext(items);
            }
        }, BackpressureStrategy.MISSING);
    }

    @Override
    public void insertItems(List<T> item) {
        items.addAll(item);
        notifyListeners(items);
    }

    @Override
    public void updateItems(List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyListeners(items);
    }

    @Override
    public int size() {
        return items.size();
    }

    private void notifyListeners(List<T> items) {
        for (ItemsChangeListener<T> listener : listeners) {
            listener.onItemsChanged(items);
        }
    }

    private void addListener(ItemsChangeListener<T> listener) {
        listeners.add(listener);
    }

    private void removeListener(ItemsChangeListener<T> listener) {
        listeners.remove(listener);
    }

    private interface ItemsChangeListener<T> {
        void onItemsChanged(List<T> values);
    }
}
