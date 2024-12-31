package brk.oussama.prj.repo;

import brk.oussama.prj.module.Projet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjetRepo extends JpaRepository<Projet, Long> {
    Projet findByNom(String name);
}
