package ch.admin.bag.covidcertificate.backend.verification.check.ws.model;

import ch.admin.bag.covidcertificate.sdk.core.models.trustlist.Rule;
import java.util.List;

public class RulesResponse {

    private List<Rule> rules;

    public RulesResponse() {}

    public RulesResponse(List<Rule> rules) {
        this.rules = rules;
    }

    public List<Rule> getRules() {
        return rules;
    }

    public void setRules(List<Rule> rules) {
        this.rules = rules;
    }
}
