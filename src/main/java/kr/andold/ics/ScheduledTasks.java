package kr.andold.ics;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import kr.andold.ics.service.BackupJob;
import kr.andold.ics.service.IcsService;
import kr.andold.ics.service.JobService;
import kr.andold.ics.service.ZookeeperClient;
import kr.andold.utils.Utility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
public class ScheduledTasks {
	@Autowired private JobService jobService;
	@Autowired private ZookeeperClient zookeeperClient;

	@Scheduled(initialDelay = 1000 * 8, fixedDelay = Long.MAX_VALUE)
	public void once() {
		log.info("{} once()", Utility.indentStart());
		long started = System.currentTimeMillis();
		
		zookeeperClient.run();
		
		log.info("{} once() - {}", Utility.indentEnd(), Utility.toStringPastTimeReadable(started));
	}

	// 매초마다
	@Scheduled(fixedDelay = 1000, initialDelay = 1000 * 8)
	public void secondly() {
		jobService.run();
	}

	// 매분마다
	@Scheduled(cron = "0 * * * * *")
	public void minutely() {
		log.trace("{} minutely()", Utility.indentStart());
		long started = System.currentTimeMillis();

		jobService.status();

		log.trace("{} minutely() - {}", Utility.indentEnd(), Utility.toStringPastTimeReadable(started));
	}

	// 매시마다
	@Scheduled(cron = "1 0 * * * *")
	public void hourly() {
		if (ZookeeperClient.isMaster()) {
			
		}
	}

	// 매일
	@Scheduled(cron = "0 0 0 * * *")
	public void daily() {
		if (ZookeeperClient.isMaster()) {
			JobService.getQueue2().offer(BackupJob.builder().dataPath(IcsService.getUserDataPath()).build());
		}
	}

}
