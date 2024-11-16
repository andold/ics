package kr.andold.ics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.andold.ics.entity.VCalendarEntity;

@Repository
public interface VCalendarRepository extends JpaRepository<VCalendarEntity, Integer> {

}
