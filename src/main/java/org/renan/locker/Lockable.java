package org.renan.locker;

import java.util.List;
import java.util.concurrent.TimeUnit;

public interface Lockable<ID> {

    void lock(ID id);

    boolean lock(ID id, Long timeout, TimeUnit timeUnit) throws InterruptedException;

    void unlock(ID id);

    boolean isLocked(ID id);

    List<ID> getBlockedList();

    int getHoldCount(ID id);

    public void globalLock();

    public void globalunLock();

    public boolean isGlobalLocked();

}
