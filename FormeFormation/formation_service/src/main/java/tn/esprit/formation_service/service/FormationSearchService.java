package tn.esprit.formation_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.formation_service.entity.Formation;

public interface FormationSearchService {

    Page<Formation> findAllFiltered(String status, String category, String level, String search, Pageable pageable);
}
