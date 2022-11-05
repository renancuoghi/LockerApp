package org.renan.locker.test.locker;

import org.junit.Test;
import org.renan.locker.EscalationEntityLocker;

import static org.junit.Assert.*;

public class EscalationEntityLockerTest {

    @Test
    public void simpleEscalationTillGlobalTest(){
        // with one thread get more the 4 entities, will become a global lock
        final EscalationEntityLocker<Integer> escalationEntityLocker = new EscalationEntityLocker<>(4);



        new Thread(() -> {

            escalationEntityLocker.lock(1);
            assertFalse(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.lock(2);
            escalationEntityLocker.lock(3);
            assertFalse(escalationEntityLocker.isEscalate());
            escalationEntityLocker.lock(4);
            assertTrue(escalationEntityLocker.isEscalate());
            assertTrue(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.globalunLock();
            escalationEntityLocker.unlock(1);
            escalationEntityLocker.unlock(2);
            escalationEntityLocker.unlock(3);
            escalationEntityLocker.unlock(4);
            assertFalse(escalationEntityLocker.isEscalate());
            assertFalse(escalationEntityLocker.isGlobalLocked());


        }).start();



    }


    @Test
    public void multiThreadEscalationTest(){
        // with one thread get more the 4 entities, will become a global lock
        final EscalationEntityLocker<Integer> escalationEntityLocker = new EscalationEntityLocker<>(5);

        new Thread(() -> {

            escalationEntityLocker.lock(1);
            assertFalse(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.lock(2);
            escalationEntityLocker.lock(3);
            assertFalse(escalationEntityLocker.isEscalate());
            escalationEntityLocker.unlock(1);
            escalationEntityLocker.unlock(2);
            escalationEntityLocker.unlock(3);

        }).start();

        new Thread(() -> {

            escalationEntityLocker.lock(1);
            assertFalse(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.lock(2);
            escalationEntityLocker.lock(3);
            assertFalse(escalationEntityLocker.isEscalate());
            escalationEntityLocker.lock(4);
            assertFalse(escalationEntityLocker.isEscalate());
            assertFalse(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.unlock(1);
            escalationEntityLocker.unlock(2);
            escalationEntityLocker.unlock(3);
            escalationEntityLocker.unlock(4);


        }).start();

    }


    @Test
    public void multiThreadEscalationGlobalLockTest(){
        // with one thread get more the 4 entities, will become a global lock
        final EscalationEntityLocker<Integer> escalationEntityLocker = new EscalationEntityLocker<>(3);

        new Thread(() -> {

            escalationEntityLocker.lock(1);
            assertFalse(escalationEntityLocker.isGlobalLocked());
            escalationEntityLocker.lock(2);
            escalationEntityLocker.lock(3);
            assertTrue(escalationEntityLocker.isEscalate());
            try {
                Thread.sleep(900l);

            } catch (InterruptedException e) {
                e.printStackTrace();
            }finally {
                escalationEntityLocker.unlock(1);
                escalationEntityLocker.unlock(2);
                escalationEntityLocker.unlock(3);
            }


        }).start();

        new Thread(() -> {

            escalationEntityLocker.lock(1);

            escalationEntityLocker.lock(2);
            escalationEntityLocker.lock(3);
            assertTrue(escalationEntityLocker.isEscalate());
            escalationEntityLocker.lock(4);
            try {

                Thread.sleep(1000);
                escalationEntityLocker.globalunLock();
            } catch (InterruptedException e) {
                fail();
            }finally {
                escalationEntityLocker.unlock(1);
                escalationEntityLocker.unlock(2);
                escalationEntityLocker.unlock(3);
                escalationEntityLocker.unlock(4);

            }


        }).start();

        try {
            Thread.sleep(800);
            assertTrue(escalationEntityLocker.isEscalate());
            new Thread(() -> {
                escalationEntityLocker.lock(1);
                escalationEntityLocker.unlock(1);
            }).start();
            Thread.sleep(3000l);
            assertFalse(escalationEntityLocker.isEscalate());

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

    }


}
