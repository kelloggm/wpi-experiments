package gov.fda.nctr.models.dto;

import java.util.List;

import gov.fda.nctr.models.dto.drugrelents.Sbd;
import gov.fda.nctr.models.dto.drugrelents.Scd;

@SuppressWarnings("nullness") // fields will be set directly by the deserializer
public class NdcRelatedEntities
{
  private String ndc;
  private List<Scd> scds;
  private List<Sbd> sbds;

  private NdcRelatedEntities() { } // for Jackson serialization

  public String getNdc() { return ndc; }

  public List<Scd> getScds() { return scds; }

  public List<Sbd> getSbds() { return sbds; }
}
