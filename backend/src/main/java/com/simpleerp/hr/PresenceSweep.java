package com.simpleerp.hr;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * End-of-day job that auto-checks-out anyone still checked in, so a forgotten check-in does not
 * linger as PRESENT into the next day. The schedule is configurable; it defaults to 23:55 daily in
 * the server's time zone.
 */
@Component
public class PresenceSweep {

    private static final Logger log = LoggerFactory.getLogger(PresenceSweep.class);

    private final PresenceService presence;

    public PresenceSweep(PresenceService presence) {
        this.presence = presence;
    }

    /** Closes the day's open check-ins. */
    @Scheduled(cron = "${simpleerp.presence.auto-checkout-cron:0 55 23 * * *}")
    public void closeOpenCheckIns() {
        int closed = presence.autoCheckOutOpen();
        if (closed > 0) {
            log.info("End-of-day sweep auto-checked-out {} open presence record(s)", closed);
        }
    }
}
