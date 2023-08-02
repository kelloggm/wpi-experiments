package gov.fda.nctr.data_access.drugrelents;

import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;

import gov.fda.nctr.models.dto.NdcRelatedEntities;

@Service
public class TheDrugRelatedEntitiesSearchService implements DrugRelatedEntitiesSearchService
{
  private final DrugRelatedEntitiesSearchDbOpsService dbOps;

  public TheDrugRelatedEntitiesSearchService(DrugRelatedEntitiesSearchDbOpsService dbOps)
  {
    this.dbOps = dbOps;
  }

  public List<NdcRelatedEntities> getNdcRelatedEntities
    (
      Set<String> ndcs
    )
  {
    return dbOps.getNdcRelatedEntities(ndcs);
  }
}
