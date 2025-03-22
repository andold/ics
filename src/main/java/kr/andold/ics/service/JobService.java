package kr.andold.ics.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import kr.andold.ics.ApplicationContextProvider;
import kr.andold.utils.Utility;
import kr.andold.utils.job.JobInterface;
import kr.andold.utils.job.STATUS;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobService extends kr.andold.utils.job.JobService {
	@Data
	@Builder
	public static class BackupJob implements JobInterface {
		@Autowired private IcsService icsService;
		private String dataPath;

		@Override
		public STATUS call() throws Exception {
			log.info("{} backup()", Utility.indentStart());
			long started = System.currentTimeMillis();

			BackupJob that = (BackupJob) ApplicationContextProvider.getBean(BackupJob.class);
			that.setDataPath(dataPath);
			STATUS result = that.main();

			log.info("{} {} backup() - {}", Utility.indentEnd(), result, Utility.toStringPastTimeReadable(started));
			return result;
		}

		protected STATUS main() {
			log.info("{} main()", Utility.indentStart());
			long started = System.currentTimeMillis();

			//	calendar.ics
			String text = icsService.downloadIcs(1028);
			String filename = String.format("%s/calendar.ics", dataPath);
			Utility.write(filename, text);
			log.debug("{} main(『{}』) - 『{}』『{}』", Utility.indentMiddle(), dataPath, filename, Utility.ellipsisEscape(text, 32, 32));
			
			log.info("{} {} main() - {}", Utility.indentEnd(), STATUS.SUCCESS, Utility.toStringPastTimeReadable(started));
			return STATUS.SUCCESS;
		}

	}

}
