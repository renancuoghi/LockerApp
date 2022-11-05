package org.renan.locker;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class EntityLocker<ID> implements Lockable<ID> {

    private final Map<ID, ReentrantLock> blockList = new ConcurrentHashMap<>();

    private final ReentrantLock globalLock = new ReentrantLock();

    public void lock(ID id){
        ReentrantLock lock = this.createOrGetLock(id);
        lock.lock();
    }

    public boolean lock(ID id,  Long timeout, TimeUnit timeUnit) throws InterruptedException {
        ReentrantLock lock = this.createOrGetLock(id);
        return lock.tryLock(timeout, timeUnit);

    }

    public void unlock(ID id){
        ReentrantLock lock = this.blockList.get(id);
        if(lock != null) {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
            if (!lock.hasQueuedThreads()) {
                this.blockList.remove(id);
            }
        }
    }

    public List<ID> getBlockedList(){
        return this.blockList.entrySet().stream().map(Map.Entry::getKey).collect(Collectors.toList());
    }

    private ReentrantLock createOrGetLock(ID id){
        // if is a global lock every new lock must be the global to keep all threads
        if(this.globalLock.isLocked() && this.blockList.containsKey(id)==false){
            this.blockList.putIfAbsent(id, this.globalLock);
        }else {
            this.blockList.putIfAbsent(id, new ReentrantLock());
        }
        return this.blockList.get(id);
    }

    public boolean isLocked(ID id){
        return (this.blockList.containsKey(id)
                && this.blockList.get(id).isLocked());
    }

    public int getHoldCount(ID id){
        int qty = 0;
        if(isLocked(id)){
            qty = createOrGetLock(id).getHoldCount();
        }
        return qty;
    }

    public void globalLock() {
        this.globalLock.lock();
    }

    public void globalunLock() {
        this.globalLock.unlock();
    }

    public boolean isGlobalLocked(){
        return this.globalLock.isLocked();
    }



}
