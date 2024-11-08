package kr.anold.ics;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import kr.andold.utils.Utility;
import kr.anold.ics.service.JobService;
import kr.anold.ics.service.JobService.BackupJob;
import kr.anold.ics.service.JobService.Job;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
@ConditionalOnProperty(value = "app.scheduling.enable", havingValue = "true", matchIfMissing = true)
public class ScheduledTasks {
	@Autowired private JobService jobService;

	@Getter private static String dataPath;
	@Value("${data.path:C:/tmp}")
	public void setDataPath(String dataPath) {
		log.info("{} setDataPath({})", Utility.indentMiddle(), dataPath);
		ScheduledTasks.dataPath = dataPath;
		File directory = new File(dataPath);
		if (!directory.exists()) {
			log.info("{} NOT EXIST PATH setDataPath({})", Utility.indentMiddle(), dataPath);
			directory.mkdir();
		}
	}

	@Scheduled(initialDelay = 1000 * 10, fixedDelay = Long.MAX_VALUE)
	public void scheduleTaskOnce() {
		jobService.run();
	}

	// 매일
	@Scheduled(cron = "0 0 0 * * *")
	public void scheduleTaskEveryDays() {
		Job job = BackupJob.builder().dataPath(dataPath).build();
		JobService.getQueue3().offer(job);
	}

}
