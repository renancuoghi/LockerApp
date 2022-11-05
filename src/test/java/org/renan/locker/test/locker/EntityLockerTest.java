package org.renan.locker.test.locker;


import org.junit.Test;
import org.renan.locker.EntityLocker;
import org.renan.locker.test.locker.utils.product.entity.Product;
import org.renan.locker.test.locker.utils.product.thread.ProductPriceMultiplier;
import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class EntityLockerTest {

    private final EntityLocker<Integer> entityLocker = new EntityLocker<>();
    @Test
    public void testSimpleLock(){

        entityLocker.lock(1);

        assertTrue(entityLocker.isLocked(1));

        assertEquals(1, entityLocker.getBlockedList().size());

        entityLocker.unlock(1);

        assertFalse(entityLocker.isLocked(1));

        assertEquals(0, entityLocker.getBlockedList().size());

    }
    @Test
    public void testNotExistedId() {

        assertFalse(entityLocker.isLocked(10));

        entityLocker.unlock(10);

        assertFalse(entityLocker.isLocked(10));

        assertEquals(0, entityLocker.getBlockedList().size());

    }


    @Test
    public void testHoldCount() {

        entityLocker.lock(2);
        entityLocker.lock(2);
        entityLocker.lock(2);

        assertEquals(3, entityLocker.getHoldCount(2));

        entityLocker.unlock(2);

        assertEquals(0, entityLocker.getHoldCount(2));

    }


    @Test
    public void testThreads() {
        long millis = 2000l; // 3 seconds
        long millis2 = 1000l; // 1 second

        Product product = new Product(30,"Product 30", BigDecimal.valueOf(30));
        Product product2 = new Product(40,"Product 40", BigDecimal.valueOf(40));

        ProductPriceMultiplier thread = new ProductPriceMultiplier(entityLocker, product, "Thread1", millis).withMultiplier(BigDecimal.TEN);
        ProductPriceMultiplier thread2 = new ProductPriceMultiplier(entityLocker, product2, "Thread2", millis2).withMultiplier(BigDecimal.TEN);

        thread.start();
        thread2.start();

        try {
            Thread.sleep(1200l);
            // product 1 must be locked yet
            assertTrue(entityLocker.isLocked(product.getId()));
            // product 2 must be unlocked
            assertFalse(entityLocker.isLocked(product2.getId()));
            // product 1 must be with same price (is processing)
            assertEquals(BigDecimal.valueOf(30), product.getPrice());
            // product 2 must be with new price
            assertEquals(BigDecimal.valueOf(400), product2.getPrice());

            Thread.sleep(2200l);
            // now product 1 must be unlocked
            assertFalse(entityLocker.isLocked(product.getId()));
            // new price
            assertEquals(BigDecimal.valueOf(300), product.getPrice());

        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            fail();
        }

    }
    @Test
    public void testConcurrencyThread() {
        long millis = 1000l; // 3 seconds

        Product product = new Product(30,"Product 30", BigDecimal.valueOf(30));


        ProductPriceMultiplier thread = new ProductPriceMultiplier(entityLocker, product, "Thread1", millis).withMultiplier(BigDecimal.TEN);
        ProductPriceMultiplier thread2 = new ProductPriceMultiplier(entityLocker, product, "Thread2", millis).withMultiplier(BigDecimal.TEN);

        thread.start();
        thread2.start();
        try {
            Thread.sleep(1500l);
            assertEquals(BigDecimal.valueOf(300), product.getPrice());
            Thread.sleep(3000l);
            assertEquals(BigDecimal.valueOf(3000), product.getPrice());
            // assertTrue(true);
        } catch (InterruptedException e) {
            // throw new RuntimeException(e);
            fail();
        }

    }
    @Test
    public void testTryLock(){
        Product p1 = new Product(1,"Product one", BigDecimal.TEN);
        new Thread(() -> {
            boolean lockAcquired = false;
            try {
                lockAcquired = entityLocker.lock(p1.getId(), 1l, TimeUnit.SECONDS);
                assertTrue(lockAcquired);
                // sleep for seconds for another thread try do get locked
                Thread.sleep(2000l);
            } catch (InterruptedException e) {
                fail();
            }finally {
                entityLocker.unlock(p1.getId());
            }

        }).start();

        new Thread(() -> {
            boolean lockAcquired = false;
            try {
                // first thread must be locked yet
                lockAcquired = entityLocker.lock(p1.getId(), 1l, TimeUnit.SECONDS);
                assertFalse(lockAcquired);
            } catch (InterruptedException e) {
                fail();
            }finally {
                entityLocker.unlock(p1.getId());
            }

        }).start();


    }

    @Test
    public void testGloballock(){
        Product product = new Product(30,"Product 30", BigDecimal.valueOf(30));
        Product product2 = new Product(40,"Product 40", BigDecimal.valueOf(40));

        ProductPriceMultiplier thread = new ProductPriceMultiplier(entityLocker, product, "Thread1", 200l).withMultiplier(BigDecimal.TEN);
        ProductPriceMultiplier thread2 = new ProductPriceMultiplier(entityLocker, product2, "Thread2", 200l).withMultiplier(BigDecimal.TEN);

        entityLocker.globalLock();
        assertTrue(entityLocker.isGlobalLocked());
        thread.start();
        thread2.start();

        try {
            Thread.sleep(2000l);
            // is global locked
            assertEquals(BigDecimal.valueOf(30), product.getPrice());
            assertEquals(BigDecimal.valueOf(40), product2.getPrice());
            assertTrue(entityLocker.getBlockedList().size() > 0);
            System.out.println("Global unlock");
            entityLocker.globalunLock();
            assertFalse(entityLocker.isGlobalLocked());
            Thread.sleep(2000l);
            assertEquals(BigDecimal.valueOf(300), product.getPrice());
            Thread.sleep(2000l);
            assertEquals(BigDecimal.valueOf(400), product2.getPrice());
        } catch (InterruptedException e) {
            fail();
        }

    }


}
