package com.orion.mobile.gateway.domain;


import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/14 10:33
 * @Version 1.0.0
 */
public class StaticInfo {
    private AtomicInteger minute = new AtomicInteger(0);
    private AtomicLong suc = new AtomicLong(0);
    private AtomicLong excep = new AtomicLong(0);
    private AtomicLong validateFail = new AtomicLong(0);

    public void suc() {
        this.suc.incrementAndGet();
    }

    public void excep() {
        this.excep.incrementAndGet();
    }

    public void validateFail() {
        this.validateFail.incrementAndGet();
    }

    public boolean needPrint(int v) {
        if (minute.get() == 0) {
            minute.set(v);
            return false;
        }
        if (minute.get() == v) {
            return false;
        }
        minute.set(v);
        return true;
    }

    public long getSuc() {
        return suc.getAndSet(0);
    }

    public long getExcep() {
        return excep.getAndSet(0);
    }

    public long getValidateFail() {
        return validateFail.getAndSet(0);
    }

}
