package gov.fda.nctr.data_access.drugrelents;

import java.util.List;
import java.util.Set;

import org.checkerframework.checker.nullness.qual.Nullable;

import gov.fda.nctr.models.dto.NdcRelatedEntities;

public interface DrugRelatedEntitiesSearchDbOpsService
{
  List<NdcRelatedEntities> getNdcRelatedEntities
    (
      Set<String> ndcs
    );
}
