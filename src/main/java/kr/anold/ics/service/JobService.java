package kr.anold.ics.service;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import kr.andold.utils.Utility;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobService {
	@Getter private static ConcurrentLinkedDeque<Job> queue0 = new ConcurrentLinkedDeque<>();
	@Getter private static ConcurrentLinkedDeque<Job> queue1 = new ConcurrentLinkedDeque<>();
	@Getter private static ConcurrentLinkedDeque<Job> queue2 = new ConcurrentLinkedDeque<>();
	@Getter private static ConcurrentLinkedDeque<Job> queue3 = new ConcurrentLinkedDeque<>();

	@Autowired private IcsService icsService;

	public static interface Job {
		default int run() {
			return -1;
		}
	}
	@Data
	@Builder
	public static class BackupJob implements Job {
		private String dataPath;
	}

	@Async
	public int run() {
		log.info("{} run()", Utility.indentStart());
		long started = System.currentTimeMillis();
		
		for (int cx = 0;; cx++) {
			try {
				if (queue0.peek() != null) {
					Job job = queue0.poll();
					int result = run(job);

					log.debug("{} 『{}』 run() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
					continue;
				}
				if (queue1.peek() != null) {
					Job job = queue1.poll();
					int result = run(job);
					
					log.debug("{} 『{}』 run() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
					continue;
				}
				if (queue2.peek() != null) {
					Job job = queue2.poll();
					int result = run(job);

					log.debug("{} 『{}』 run() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
					continue;
				}
				if (queue3.peek() != null) {
					Job job = queue3.poll();
					int result = run(job);

					log.debug("{} 『{}』 run() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
					continue;
				}
			} catch (Exception e) {
				log.error("{} run() - #{} 『{}』 - Exception:: {}", Utility.indentMiddle(), cx, e.getLocalizedMessage(), Utility.toStringPastTimeReadable(started), e);
			}

			Utility.sleep(1000);
			log.trace("{} run() - #{} 『{}』", Utility.indentMiddle(), cx, Utility.toStringPastTimeReadable(started));
		}
//		log.info("{} {} run() - {}", Utility.indentEnd(), -1, Utility.toStringPastTimeReadable(started));
//		return -1;
	}

	private int run(Job job) {
		log.trace("{} run(『{}』)", Utility.indentStart(), job);
		long started = System.currentTimeMillis();

		if (job == null) {
			log.trace("{} 『NULL:{}』 run(『{}』) - {}", Utility.indentEnd(), -1, job, Utility.toStringPastTimeReadable(started));
			return -1;
		}
		
		if (job instanceof BackupJob) {
			int result = backup((BackupJob)job);

			log.debug("{} 『{}』 run(『{}』) - {}", Utility.indentEnd(), result, job, Utility.toStringPastTimeReadable(started));
			return result;
		}

		return 0;
	}

	private int backup(BackupJob job) {
		log.info("{} backup()", Utility.indentStart());
		long started = System.currentTimeMillis();

		String dataPath = job.getDataPath();

		//	calendar.ics
		String text = icsService.downloadIcs(1028);
		String filename = String.format("%s/calendar.ics", dataPath);
		Utility.write(filename, text);
		log.debug("{} backup(『{}』) - 『{}』『{}』", Utility.indentMiddle(), job, filename, Utility.ellipsis(text, 32, 32));
		
		log.info("{} {} backup() - {}", Utility.indentEnd(), -1, Utility.toStringPastTimeReadable(started));
		return 0;
	}

}
