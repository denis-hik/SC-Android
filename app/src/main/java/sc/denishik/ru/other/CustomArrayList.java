package sc.denishik.ru.other;

import java.util.ArrayList;

public class CustomArrayList<E> extends ArrayList<E> {
    private static final int MAX_SIZE = 10;

    @Override
    public boolean add(E e) {
        if (this.size() < MAX_SIZE) {
            super.add(0, e);
        } else {
            super.add(0, e);
            super.remove(this.size() - 1);
        }
        return true;
    }

    @Override
    public void add(int index, E element) {
        if (this.size() < MAX_SIZE) {
            super.add(index, element);
        } else {
            throw new IllegalStateException("Exceeded maximum size of the list.");
        }
    }
}
