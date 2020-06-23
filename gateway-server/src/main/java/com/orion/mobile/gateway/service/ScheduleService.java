package com.orion.mobile.gateway.service;

import com.google.inject.Singleton;
import com.orion.logger.BusinessLoggerFactory;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @Description TODO
 * @Author potsmart
 * @Date 2019/11/15 9:02
 * @Version 1.0.0
 */
@Singleton
public class ScheduleService {
    private Logger logger = BusinessLoggerFactory.getBusinessLogger("MGATEWAY", ScheduleService.class);

    ScheduledThreadPoolExecutor schedule = null;

    @PostConstruct
    public void init() {
        schedule = new ScheduledThreadPoolExecutor(3,
                new BasicThreadFactory.Builder().namingPattern("service-daemon-%d").priority(1).daemon(true).build());
    }

    public void scheduleAtFixedRate(Runnable run, long delay, long period, TimeUnit timeUnit) {
        logger.info("submit task");
        schedule.scheduleAtFixedRate(run, delay, period, timeUnit);
    }
}
