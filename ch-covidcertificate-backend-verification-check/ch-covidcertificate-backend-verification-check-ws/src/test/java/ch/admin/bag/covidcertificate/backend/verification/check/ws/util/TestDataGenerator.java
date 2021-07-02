package ch.admin.bag.covidcertificate.backend.verification.check.ws.util;

import ch.admin.bag.covidcertificate.sdk.core.models.healthcert.eu.Eudgc;
import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.Moshi.Builder;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.text.StringSubstitutor;

public class TestDataGenerator {

    private static final JsonAdapter<Eudgc> adapter =
            new Builder()
                    .add(Date.class, new Rfc3339DateJsonAdapter())
                    .build()
                    .adapter(Eudgc.class);

    /**
     * @param dn dose number
     * @param sd total series of doses
     * @param ma marketing authorization holder code
     * @param mp medical product code
     * @param tg target disease code
     * @param vp vaccine prophylaxis code
     * @param vaccinationDate
     */
    public static Eudgc generateVaccineCert(
            Integer dn,
            Integer sd,
            String ma,
            String mp,
            String tg,
            String vp,
            LocalDateTime vaccinationDate) {
        String json =
                "{\n"
                        + "                 \"v\": [\n"
                        + "                   {\n"
                        + "                     \"ci\": \"urn:uvci:01:CH:9C595501BBC294450BD0F6E2\",\n"
                        + "                     \"co\": \"BE\",\n"
                        + "                     \"dn\": ${dn},\n"
                        + "                     \"dt\": \"${vaccinationDate}\",\n"
                        + "                     \"is\": \"Bundesamt für Gesundheit (BAG)\",\n"
                        + "                     \"ma\": \"${ma}\",\n"
                        + "                     \"mp\": \"${mp}\",\n"
                        + "                     \"sd\": ${sd},\n"
                        + "                     \"tg\": \"${tg}\",\n"
                        + "                     \"vp\": \"${vp}\"\n"
                        + "                   }\n"
                        + "                 ],\n"
                        + "                 \"dob\": \"1990-12-12\",\n"
                        + "                 \"nam\": {\n"
                        + "                   \"fn\": \"asdf\",\n"
                        + "                   \"gn\": \"asdf\",\n"
                        + "                   \"fnt\": \"ASDF\",\n"
                        + "                   \"gnt\": \"ASDF\"\n"
                        + "                 },\n"
                        + "                 \"ver\": \"1.0.0\"\n"
                        + "               }";
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("dn", dn.toString());
        valuesMap.put("sd", sd.toString());
        valuesMap.put("ma", ma);
        valuesMap.put("mp", mp);
        valuesMap.put("tg", tg);
        valuesMap.put("vp", vp);
        valuesMap.put("vaccinationDate", vaccinationDate.format(DateTimeFormatter.ISO_DATE));
        StringSubstitutor stringSubstitutor = new StringSubstitutor(valuesMap);
        String subbedJson = stringSubstitutor.replace(json);
        try {
            return adapter.fromJson(subbedJson);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
