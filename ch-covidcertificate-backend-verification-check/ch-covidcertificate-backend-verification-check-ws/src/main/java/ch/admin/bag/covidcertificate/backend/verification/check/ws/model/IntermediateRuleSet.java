package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleValueSets;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import javax.validation.constraints.NotNull;

public class IntermediateRuleSet {

    @NotNull private List<Rule> rules;
    @NotNull private RuleValueSets valueSets;
    @NotNull private Long validDuration;

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public RuleValueSets getValueSets() {
        return valueSets;
    }

    public void setValueSets(RuleValueSets valueSets) {
        this.valueSets = valueSets;
    }

    public Long getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(Long validDuration) {
        this.validDuration = validDuration;
    }

    public static class Rule {
        private static final ObjectMapper objectMapper = new ObjectMapper();
        @NotNull private String id;
        private String businessDescription;
        @NotNull private String description;
        @NotNull private String inputParameter;
        @NotNull @JsonRawValue private Object logic;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getBusinessDescription() {
            return businessDescription;
        }

        public void setBusinessDescription(String businessDescription) {
            this.businessDescription = businessDescription;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getInputParameter() {
            return inputParameter;
        }

        public void setInputParameter(String inputParameter) {
            this.inputParameter = inputParameter;
        }

        @JsonRawValue
        public String getLogic() {
            try {
                return logic == null ? null : objectMapper.writeValueAsString(logic);
            } catch (JsonProcessingException e) {
                return null;
            }
        }

        public void setLogic(Object logic) {
            this.logic = logic;
        }
    }
}
