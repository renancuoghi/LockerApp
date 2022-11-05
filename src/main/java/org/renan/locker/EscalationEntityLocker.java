package org.renan.locker;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EscalationEntityLocker<ID> extends EntityLocker<ID>{

    private final Map<Long, List<ID>> threadEntitiesLocked = new ConcurrentHashMap<>();

    private final int escalationDetector;

    /**
     * Define the qty of entities into thread list to escalate for a global lock
     * @param escalationDetector
     */
    public EscalationEntityLocker(int escalationDetector){
        this.escalationDetector = escalationDetector;
    }

    @Override
    public void lock(ID id) {
        super.lock(id);
        this.addEntityInThreadList(id);
    }

    @Override
    public boolean lock(ID id, Long timeout, TimeUnit timeUnit) throws InterruptedException {
        boolean lockedAdquired = super.lock(id, timeout, timeUnit);
        if(lockedAdquired){
            this.addEntityInThreadList(id);
        }
        return lockedAdquired;
    }

    @Override
    public void unlock(ID id) {
        super.unlock(id);
        if(!this.isLocked(id)){
            this.removeEntityInThreadList(id);
        }
    }

    public boolean isEscalate(){
        return this.threadEntitiesLocked
            .entrySet()
            .stream()
            .map(Map.Entry::getValue)
            .filter(l -> l.size() >= this.escalationDetector)
            .findFirst()
            .isPresent();
    }

    public Map<Long, List<ID>> getEscalate(){
        return this.threadEntitiesLocked
                .entrySet()
                .stream()
                .filter(m -> m.getValue().size() >= this.escalationDetector)
                .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
    }

    private List<ID> getOrCreateThreadList(){
        this.threadEntitiesLocked.putIfAbsent(Thread.currentThread().getId(), new CopyOnWriteArrayList<>());
        return this.threadEntitiesLocked.get(Thread.currentThread().getId());
    }

    private void addEntityInThreadList(ID id){
        List<ID> threadEscalation = this.getOrCreateThreadList();
        threadEscalation.add(id);
        if(threadEscalation.size() >= escalationDetector){
            this.globalLock();
        }
    }

    private void removeEntityInThreadList(ID id){
        this.getOrCreateThreadList().remove(id);
    }

}
