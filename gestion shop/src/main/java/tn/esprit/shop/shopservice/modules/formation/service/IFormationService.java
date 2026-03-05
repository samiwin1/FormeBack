package tn.esprit.shop.shopservice.modules.formation.service;

import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.formation.entity.FormationStatus;

import java.util.List;

public interface IFormationService {

    List<Formation> getAllFormations();

    Formation addFormation(Formation formation);

    Formation getFormationBy(long id);

    Formation updateFormation(Formation formation);

    void deleteFormation(long id);

    List<Formation> findByStatus(FormationStatus status);

    List<Formation> findByCreatedBy(Long createdBy);

    List<Formation> findByCategory(String category);
}
