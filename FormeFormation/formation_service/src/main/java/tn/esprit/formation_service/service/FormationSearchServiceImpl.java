package tn.esprit.formation_service.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.formation_service.entity.Formation;
import tn.esprit.formation_service.repository.FormationRepository;

@Service
public class FormationSearchServiceImpl implements FormationSearchService {

    private final FormationRepository formationRepository;

    public FormationSearchServiceImpl(FormationRepository formationRepository) {
        this.formationRepository = formationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Formation> findAllFiltered(String status, String category, String level, String search, Pageable pageable) {
        return formationRepository.findByFilters(status, category, level, search, pageable);
    }
}
