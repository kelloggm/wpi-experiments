package gov.fda.nctr.data_access.drugrelents;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import gov.fda.nctr.models.dto.NdcRelatedEntities;
import static gov.fda.nctr.data_access.JdbcUtils.Params.params;
import static gov.fda.nctr.data_access.JdbcUtils.jsonObjectRowMapper;
import static gov.fda.nctr.util.Json.makeJsonObjectMapper;

@Service
public class JdbcDrugRelatedEntitiesSearchDbOpsService implements DrugRelatedEntitiesSearchDbOpsService
{
  private final NamedParameterJdbcTemplate jdbc;

  private final ObjectMapper jsonMapper;

  public JdbcDrugRelatedEntitiesSearchDbOpsService(JdbcTemplate jdbcTemplate)
  {
    this.jdbc = new NamedParameterJdbcTemplate(jdbcTemplate);
    this.jsonMapper = makeJsonObjectMapper();
  }

  @Override
  public List<NdcRelatedEntities> getNdcRelatedEntities
    (
      Set<String> ndcs
    )
  {
    return jdbc.query(
      "select row_to_json(r) from relents_ndc_mv r where r.ndc in (:ndcs)",
      params("ndcs", ndcs),
      jsonObjectRowMapper(NdcRelatedEntities.class, jsonMapper)
    );
  }
}
