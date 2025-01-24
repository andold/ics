package kr.andold.ics;

import java.util.Date;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.usingsky.calendar.KoreanLunarCalendar;

import kr.andold.utils.Utility;
import lombok.extern.slf4j.Slf4j;
import net.fortuna.ical4j.model.DateTime;

@Slf4j
public class KoreanLunarCalendarTest {
	@BeforeEach
	public void setUp() throws Exception {
		log.info(Utility.HR);
	}

	@Test
	public void testSetLunarDate() {
		KoreanLunarCalendar calendar = KoreanLunarCalendar.getInstance();
		calendar.setLunarDate(2025, 1, 16, false);
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getChineseGapJaString(), calendar.getGapjaString());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getLunarIsoFormat(), calendar.getSolarIsoFormat());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getSolarDay(), calendar.getSolarMonth());

		calendar.setSolarDate(2025, 2, 2);
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getChineseGapJaString(), calendar.getGapjaString());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getLunarIsoFormat(), calendar.getSolarIsoFormat());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getSolarDay(), calendar.getSolarMonth());
		
		Date date = Utility.parseDateTime("2025-02-02");
		DateTime dt = new DateTime(date);
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), date, dt);
	}

}
