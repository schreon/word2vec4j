import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class AsyncIterator<T> implements Iterator<T> {

    private BlockingQueue<T> queue = new ArrayBlockingQueue<T>(100);
    private T sentinel = (T) new Object();
    private T next;

    public AsyncIterator(final Iterator<T> delegate) {
        new Thread() {
            @Override
            public void run() {
                try {
                    while (delegate.hasNext()) {
                        queue.put(delegate.next());
                    }
                    queue.put(sentinel);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }.start();
    }

    @Override
    public boolean hasNext() {
        if (next != null) {
            return true;
        }
        try {
            next = queue.take(); // blocks if necessary
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        if (next == sentinel) {
            return false;
        }
        return true;
    }

    @Override
    public T next() {
        T tmp = next;
        next = null;
        return tmp;
    }

}