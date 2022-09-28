package co.lecompany.app.data.service;

import co.lecompany.app.data.entity.SampleBook;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SampleBookRepository extends JpaRepository<SampleBook, UUID> {

}