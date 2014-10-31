package common;

import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ObjectPool<T>
{
    private ConcurrentLinkedQueue<T> pool;

    public ObjectPool(final int size) {
        // initialize pool
        initialize(size);
    }

    /**
     * Gets the next free object from the pool. If the pool doesn't contain any objects,
     * a new object will be created and given to the caller of this method.
     *
     * @return T borrowed object
     */
    public T borrowObject() {
        T object;
        if ((object = pool.poll()) == null) {
            object = createObject();
        }

        return object;
    }

    /**
     * Returns object back to the pool.
     *
     * @param object object to be returned
     */
    public void returnObject(T object) {
        if (object == null) {
            return;
        }

        this.pool.offer(object);
    }

    /**
     * Creates a new object.
     *
     * @return T new object
     */
    protected abstract T createObject();

    private void initialize(final int minIdle) {
        pool = new ConcurrentLinkedQueue<T>();

        for (int i = 0; i < minIdle; i++) {
            pool.add(createObject());
        }
    }
}