package kr.andold.ics.service;

import java.util.concurrent.ConcurrentLinkedDeque;

import org.springframework.beans.factory.annotation.Autowired;
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

	public int run() {
		log.trace("{} 『{}:{}:{}:{}』 run()", Utility.indentStart(), Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3));
		long started = System.currentTimeMillis();
		
		if (queue0.peek() != null) {
			Job job = queue0.poll();
			int result = run(job);

			log.info("{} 『{}::{}:{}:{}:{}』 run() - {}", Utility.indentEnd()
					, result, Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3), Utility.toStringPastTimeReadable(started));
			return result;
		}
		if (queue1.peek() != null) {
			Job job = queue1.poll();
			int result = run(job);
			
			log.info("{} 『{}::{}:{}:{}:{}』 run() - {}", Utility.indentEnd()
					, result, Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3), Utility.toStringPastTimeReadable(started));
			return result;
		}
		if (queue2.peek() != null) {
			Job job = queue2.poll();
			int result = run(job);

			log.info("{} 『{}::{}:{}:{}:{}』 run() - {}", Utility.indentEnd()
					, result, Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3), Utility.toStringPastTimeReadable(started));
			return result;
		}
		if (queue3.peek() != null) {
			Job job = queue3.poll();
			int result = run(job);

			log.info("{} 『{}::{}:{}:{}:{}』 run() - {}", Utility.indentEnd()
					, result, Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3), Utility.toStringPastTimeReadable(started));
			return result;
		}

		log.trace("{} 『{}::{}:{}:{}:{}』 run() - {}", Utility.indentEnd()
				, -1, Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3), Utility.toStringPastTimeReadable(started));
		return -1;
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
		log.debug("{} backup(『{}』) - 『{}』『{}』", Utility.indentMiddle(), job, filename, Utility.ellipsisEscape(text, 32, 32));
		
		log.info("{} {} backup() - {}", Utility.indentEnd(), 0, Utility.toStringPastTimeReadable(started));
		return 0;
	}

	public void status() {
		log.info("{} 『{}:{}:{}:{}』 status()", Utility.indentMiddle(), Utility.size(queue0), Utility.size(queue1), Utility.size(queue2), Utility.size(queue3));
	}

}
