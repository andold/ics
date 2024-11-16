package kr.andold.ics;

import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import kr.andold.ics.service.JobService;
import kr.andold.ics.service.JobService.BackupJob;
import kr.andold.ics.service.JobService.Job;
import kr.andold.utils.Utility;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableScheduling
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

	@Getter private static Boolean appSchedulingEnable = true;
	@Value("${app.scheduling.enable}")
	public void setAppSchedulingEnable(Boolean appSchedulingEnable) {
		ScheduledTasks.appSchedulingEnable = appSchedulingEnable;
	}

	// 매초마다
	@Scheduled(fixedDelay = 1000, initialDelay = 1000 * 8)
	public void scheduleTaskEverySeconds() {
		jobService.run();
	}

	// 매분마다
	@Scheduled(cron = "0 * * * * *")
	public void scheduleTaskEveryMinutes() {
		jobService.status();
	}

	// 매일
	@Scheduled(cron = "0 0 0 * * *")
	public void scheduleTaskEveryDays() {
		if (!getAppSchedulingEnable()) {
			return;
		}

		Job job = BackupJob.builder().dataPath(dataPath).build();
		JobService.getQueue3().offer(job);
	}

}
