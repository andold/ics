package kr.andold.ics.service;

import org.springframework.stereotype.Service;

import kr.andold.utils.Utility;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class JobService extends kr.andold.utils.job.JobService {
	private static long STARTED = System.currentTimeMillis();

	public void status(String prefix) {
		log.info("{} {} 『{}/{}/{}/{}』 status() - {}", Utility.indentMiddle(), prefix, Utility.size(getQueue0()), Utility.size(getQueue1()), Utility.size(getQueue2()), Utility.size(getQueue3()), Utility.toStringPastTimeReadable(STARTED));
	}

}
