package gov.fda.nctr.models.dto.drugrelents;

import org.checkerframework.checker.nullness.qual.Nullable;

@SuppressWarnings("nullness") // fields will be set directly by the deserializer
public class Sbd
{
  private String rxcui;
  private String name;
  private @Nullable String prescribableName;
  private @Nullable String rxtermForm;
  private String dosageForm;
  private @Nullable String availableStrengths;
  private @Nullable String qualDistinct;
  private @Nullable String quantity;
  private boolean humanDrug;
  private boolean vetDrug;
  private @Nullable String unquantifiedFormRxcui;
  private String suppress;

  private Sbd() {}

  public String getRxcui() { return rxcui; }

  public String getName() { return name; }

  public String getPrescribableName() { return prescribableName; }

  public String getRxtermForm() { return rxtermForm; }

  public String getDosageForm() { return dosageForm; }

  public String getAvailableStrengths() { return availableStrengths; }

  public String getQualDistinct() { return qualDistinct; }

  public String getQuantity() { return quantity; }

  public boolean isHumanDrug() { return humanDrug; }

  public boolean isVetDrug() { return vetDrug; }

  public String getUnquantifiedFormRxcui() { return unquantifiedFormRxcui; }

  public String getSuppress() { return suppress; }
}
