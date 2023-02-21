package bgu.spl.net.srv;

import java.util.concurrent.atomic.AtomicLong;

public class tryouts {
    public static void main(String[] args) {
        for (int j = 1; j < 100; j++) {
            final Counter counter = new Counter();
            Thread t1 = new Thread(() -> {
                for (int i = 0; i < 2500; i++)
                    counter.inc();
            });
            Thread t2 = new Thread(() -> {
                for (int i = 0; i < 2500; i++)
                    counter.inc();
            });
            t1.start();

            t2.start();

            try {
                t1.join();
                t2.join();
            } catch (Exception e) {
            }

            System.out.println(counter.get());
        }
    }
}

class Counter {
    // @INV: get() >= 0;
    private AtomicLong count = new AtomicLong(0);

    public long get() {
        return count.get();
    }

    public void inc() {
        while (!count.compareAndSet(count.get(), count.get() + 1))
            ;
    }
}