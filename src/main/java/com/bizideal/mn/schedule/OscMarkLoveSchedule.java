package com.bizideal.mn.schedule;

import com.bizideal.mn.core.OscCrawler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author: liulq
 * @Date: 2018/3/23 12:31
 * @Description:
 * @version: 1.0
 */
@Component
public class OscMarkLoveSchedule {

    private static Logger logger = LoggerFactory.getLogger(OscMarkLoveSchedule.class);

    @Scheduled(cron = "0/30 * * * * ?")
    public void markLove() {
        try {
            OscCrawler.start();
        } catch (Exception e) {
            logger.error("系统异常..", e);
        }
    }
}
