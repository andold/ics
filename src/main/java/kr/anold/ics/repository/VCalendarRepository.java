package kr.anold.ics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import kr.anold.ics.entity.VCalendarEntity;

@Repository
public interface VCalendarRepository extends JpaRepository<VCalendarEntity, Integer> {

}
