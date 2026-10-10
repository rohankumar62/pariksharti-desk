package in.pariksharti.desk.repository;

import in.pariksharti.desk.model.PlannerRecord;
import java.util.Optional;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlannerRepository extends JpaRepository<PlannerRecord, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PlannerRecord p where p.userId = :id")
    Optional<PlannerRecord> findForUpdate(@Param("id") String id);
}