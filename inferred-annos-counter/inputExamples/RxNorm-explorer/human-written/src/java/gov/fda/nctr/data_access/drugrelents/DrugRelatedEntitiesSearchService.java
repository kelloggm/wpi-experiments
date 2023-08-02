package gov.fda.nctr.data_access.drugrelents;

import java.util.List;
import java.util.Set;

import gov.fda.nctr.models.dto.NdcRelatedEntities;

public interface DrugRelatedEntitiesSearchService
{
  List<NdcRelatedEntities> getNdcRelatedEntities
    (
      Set<String> ndcs
    );
}
