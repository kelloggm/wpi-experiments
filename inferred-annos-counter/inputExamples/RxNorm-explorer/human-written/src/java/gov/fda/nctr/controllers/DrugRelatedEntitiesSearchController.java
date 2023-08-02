package gov.fda.nctr.controllers;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import gov.fda.nctr.models.dto.NdcRelatedEntities;
import gov.fda.nctr.data_access.drugrelents.DrugRelatedEntitiesSearchService;

@RestController
@RequestMapping("/api/drug-rel-ents")
public class DrugRelatedEntitiesSearchController
{
  private final DrugRelatedEntitiesSearchService searchSvc;

  private final Logger log = LoggerFactory.getLogger(DrugRelatedEntitiesSearchController.class);

  public DrugRelatedEntitiesSearchController
    (
      DrugRelatedEntitiesSearchService searchSvc
    )
  {
    this.searchSvc = searchSvc;
  }

  @PostMapping("for-ndcs")
  public List<NdcRelatedEntities> getNdcRelatedEntities
    (
      @RequestBody Set<String> ndcs
    )
  {
    return searchSvc.getNdcRelatedEntities(ndcs);
  }
}
