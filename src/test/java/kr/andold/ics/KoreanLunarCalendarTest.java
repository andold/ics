package kr.andold.ics;

import java.text.ParseException;
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
	public void test() throws ParseException {
		DateTime from = new DateTime(Utility.parseDateTime("2025-10-26"));
		DateTime to = new DateTime(Utility.parseDateTime("2025-12-07"));

		KoreanLunarCalendar lcalendar = KoreanLunarCalendar.getInstance();
		java.util.Calendar scalendar = java.util.Calendar.getInstance();
		scalendar.setTime(from);
		lcalendar.setSolarDate(scalendar.get(java.util.Calendar.YEAR), scalendar.get(java.util.Calendar.MONTH) + 1, scalendar.get(java.util.Calendar.DAY_OF_MONTH));
		DateTime startLunarDateTime = new DateTime(Utility.parseDateTime(lcalendar.getLunarIsoFormat()));
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), lcalendar.getLunarIsoFormat(), lcalendar.getSolarIsoFormat());

		java.util.Calendar ecalendar = java.util.Calendar.getInstance();
		ecalendar.setTime(to);
		lcalendar.setSolarDate(ecalendar.get(java.util.Calendar.YEAR), ecalendar.get(java.util.Calendar.MONTH) + 1, ecalendar.get(java.util.Calendar.DAY_OF_MONTH));
		DateTime endLunarDateTime = new DateTime(Utility.parseDateTime(lcalendar.getLunarIsoFormat()));
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), lcalendar.getLunarIsoFormat(), lcalendar.getSolarIsoFormat());

		log.info("{} 『{}』『{}』", Utility.indentMiddle(), startLunarDateTime, endLunarDateTime);
	}

	@Test
	public void test1() {
		KoreanLunarCalendar calendar = KoreanLunarCalendar.getInstance();
		calendar.setSolarDate(2025, 10, 26);
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getChineseGapJaString(), calendar.getGapjaString());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getLunarIsoFormat(), calendar.getSolarIsoFormat());

		calendar.setSolarDate(2025, 12, 7);
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getChineseGapJaString(), calendar.getGapjaString());
		log.info("{} 『{}』『{}』", Utility.indentMiddle(), calendar.getLunarIsoFormat(), calendar.getSolarIsoFormat());

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
