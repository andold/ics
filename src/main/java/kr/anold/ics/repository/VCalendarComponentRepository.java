package kr.anold.ics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import kr.anold.ics.domain.IcsParam;
import kr.anold.ics.entity.VCalendarComponentEntity;

@Repository
public interface VCalendarComponentRepository extends JpaRepository<VCalendarComponentEntity, Integer> {

	List<VCalendarComponentEntity> findByContentContains(String keyword);

	@Query(value = ""
		+ "	SELECT	x"
		+ "		FROM	VCalendarComponentEntity		x"
		+ "		WHERE	("
		+ "				:#{#param.keyword}	IS NULL"
		+ "			OR	:#{#param.keyword}	=	''"
		+ "			OR	x.content			LIKE	CONCAT('%', :#{#param.keyword}, '%')"
		//	vcalendarId
		+ "		)	AND	("
		+ "				:#{#param.vcalendarId}	IS NULL"
		+ "			OR	x.vcalendarId	=	:#{#param.vcalendarId}"
/*
		//	start
		+ "		)	AND	("
		+ "				:#{#param.start}	IS NULL"
		+ "			OR	x.end	>=	:#{#param.start}"
		//	end
		+ "		)	AND	("
		+ "				:#{#param.end}	IS NULL"
		+ "			OR	x.start		<	:#{#param.end}"
*/
		+ "		)"
		+ "	ORDER BY	x.start DESC, x.end DESC", nativeQuery = false)
	List<VCalendarComponentEntity> search(@Param("param") IcsParam param);

	List<VCalendarComponentEntity> findAllByVcalendarId(Integer vcalendarId);

}
