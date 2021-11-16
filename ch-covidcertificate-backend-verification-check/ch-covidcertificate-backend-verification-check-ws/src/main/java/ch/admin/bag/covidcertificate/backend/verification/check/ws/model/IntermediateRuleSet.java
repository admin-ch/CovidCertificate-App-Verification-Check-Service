package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

//import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.RuleValueSets;
import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Description;
import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.NotNull;

public class IntermediateRuleSet {

    @NotNull private List<Rule> rules;
    @NotNull private Map<String, String[]> valueSets;
    @NotNull private Long validDuration;
    @NotNull private List<DisplayRule> displayRules;

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }

    public Long getValidDuration() {
        return validDuration;
    }

    public void setValidDuration(Long validDuration) {
        this.validDuration = validDuration;
    }

    public List<DisplayRule> getDisplayRules() {
        return displayRules;
    }

    public void setDisplayRules(
            List<DisplayRule> displayRules) {
        this.displayRules = displayRules;
    }

    public Map<String, String[]> getValueSets() {
        return valueSets;
    }

    public void setValueSets(Map<String, String[]> valueSets) {
        this.valueSets = valueSets;
    }

    public static class Rule {

        private static final ObjectMapper objectMapper = new ObjectMapper();
        @NotNull private List<String> affectedFields;
        @NotNull private String certificateType;
        @NotNull private String country;
        @NotNull private List<ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Description> description;
        @NotNull private String engine;
        @NotNull private String engineVersion;
        @NotNull private String identifier;
        @NotNull @JsonRawValue private Object logic;
        @NotNull private String schemaVersion;
        @NotNull private String type;

        public List<String> getAffectedFields() {
            return affectedFields;
        }

        public void setAffectedFields(List<String> affectedFields) {
            this.affectedFields = affectedFields;
        }

        public String getCertificateType() {
            return certificateType;
        }

        public void setCertificateType(String certificateType) {
            this.certificateType = certificateType;
        }

        public String getCountry() {
            return country;
        }

        public void setCountry(String country) {
            this.country = country;
        }

        public List<ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Description> getDescription() {
            return description;
        }

        public void setDescription(
                List<ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Description> description) {
            this.description = description;
        }

        public String getEngine() {
            return engine;
        }

        public void setEngine(String engine) {
            this.engine = engine;
        }

        public String getEngineVersion() {
            return engineVersion;
        }

        public void setEngineVersion(String engineVersion) {
            this.engineVersion = engineVersion;
        }

        public String getSchemaVersion() {
            return schemaVersion;
        }

        public void setSchemaVersion(String schemaVersion) {
            this.schemaVersion = schemaVersion;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getValidFrom() {
            return validFrom;
        }

        public void setValidFrom(String validFrom) {
            this.validFrom = validFrom;
        }

        public String getValidTo() {
            return validTo;
        }

        public void setValidTo(String validTo) {
            this.validTo = validTo;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @NotNull private String validFrom;
        @NotNull private String validTo;
        @NotNull private String version;

        public String getIdentifier() {
            return identifier;
        }

        public void setIdentifier(String identifier) {
            this.identifier = identifier;
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

        public static class Description{
            private String desc;
            private String lang;

            public String getDesc() {
                return desc;
            }

            public void setDesc(String desc) {
                this.desc = desc;
            }

            public String getLang() {
                return lang;
            }

            public void setLang(String lang) {
                this.lang = lang;
            }
        }
    }

    public static class DisplayRule {
        private static final ObjectMapper objectMapper = new ObjectMapper();
        @NotNull private String id;
        @NotNull @JsonRawValue private Object logic;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
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
