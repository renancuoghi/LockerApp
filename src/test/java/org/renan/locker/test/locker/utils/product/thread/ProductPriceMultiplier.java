package org.renan.locker.test.locker.utils.product.thread;

import org.renan.locker.EntityLocker;
import org.renan.locker.test.locker.utils.product.entity.Product;
import java.math.BigDecimal;
import java.util.logging.Logger;

public class ProductPriceMultiplier extends Thread{

    private final EntityLocker<Integer> locker;
    private final Product product;

    private final Long milliseconds;

    private BigDecimal multiplier = BigDecimal.TEN;

    private final Logger log = Logger.getLogger("entity_locker");

    public ProductPriceMultiplier(EntityLocker<Integer> locker,Product product, String name){
        this(locker,product, name, 3000l);

    }
    public ProductPriceMultiplier(EntityLocker<Integer> locker, Product product, String name, Long millisecond){
        this.locker = locker;
        this.product = product;
        this.setName(name);
        this.milliseconds = millisecond;
    }

    public void run(){

        locker.lock(this.product.getId());

        try {

            log.info(String.format("Thread %s: with Product %s is working hard.\n", this.getName(), this.product));
            Thread.sleep(this.milliseconds);
            this.product.setPrice(product.getPrice().multiply(this.multiplier));
            log.info(String.format("Thread %s: with Product %s new price: %.2f.\n", this.getName(), this.product, this.product.getPrice().doubleValue()));

        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally {
            this.locker.unlock(product.getId());
            log.info(String.format("Thread %s: with Product %s is unlocked.\n", this.getName(), this.product));
        }

    }
    public ProductPriceMultiplier withMultiplier(BigDecimal multiplier){
        this.multiplier = multiplier;
        return this;
    }
}
