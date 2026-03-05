package tn.esprit.shop.shopservice.modules.formation.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.shop.shopservice.modules.formation.entity.Formation;
import tn.esprit.shop.shopservice.modules.formation.entity.FormationStatus;
import tn.esprit.shop.shopservice.modules.formation.service.IFormationService;

import java.util.List;

@RestController
@RequestMapping("/formation")
@AllArgsConstructor
public class FormationController {

    IFormationService iformationservice;

    @GetMapping("/listFormations")
    public List<Formation> listFormations() {
        return iformationservice.getAllFormations();
    }

    @PostMapping("/addFormation")
    public Formation addFormation(@RequestBody Formation formation) {
        return iformationservice.addFormation(formation);
    }

    @GetMapping("/getFormation/{id}")
    public Formation getFormation(@PathVariable long id) {
        return iformationservice.getFormationBy(id);
    }

    @PutMapping("/updateFormation/{id}")
    public Formation updateFormation(@PathVariable long id, @RequestBody Formation formation) {
        formation.setId(id);
        return iformationservice.updateFormation(formation);
    }

    @DeleteMapping("/deleteFormation/{id}")
    public void deleteFormation(@PathVariable long id) {
        iformationservice.deleteFormation(id);
    }

    @GetMapping("/getByStatus/{status}")
    public List<Formation> getByStatus(@PathVariable FormationStatus status) {
        return iformationservice.findByStatus(status);
    }

    @GetMapping("/getByCreatedBy/{createdBy}")
    public List<Formation> getByCreatedBy(@PathVariable Long createdBy) {
        return iformationservice.findByCreatedBy(createdBy);
    }

    @GetMapping("/getByCategory/{category}")
    public List<Formation> getByCategory(@PathVariable String category) {
        return iformationservice.findByCategory(category);
    }
}
