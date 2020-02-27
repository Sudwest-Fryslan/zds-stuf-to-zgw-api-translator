package nl.haarlem.translations.zdstozgw.translation.zgw.client;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class ZGWClient {

    private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Value("${openzaak.baseUrl}")
    private String baseUrl;

    @Autowired
    RestTemplateService restTemplateService;

    private String post(String url, String json) throws HttpStatusCodeException {
        log.debug("POST: " + url + ", json: " + json);
        HttpEntity<String> request = new HttpEntity<String>(json, restTemplateService.getHeaders());
        String zgwResponse = null;
        zgwResponse = restTemplateService.getRestTemplate().postForObject(url, request, String.class);
        log.debug("POST response: " + zgwResponse);
        return zgwResponse;
    }

    private String get(String url, Map<String, String> parameters) throws HttpStatusCodeException {
        log.debug("GET: " + url);

        if (parameters != null) {
            url = getUrlWithParameters(url, parameters);
        }

        HttpEntity entity = new HttpEntity(restTemplateService.getHeaders());

        ResponseEntity<String> response = restTemplateService.getRestTemplate().exchange(
                url, HttpMethod.GET, entity, String.class);
        log.debug("GET response: " + response.getBody());

        return response.getBody();
    }

    private String getUrlWithParameters(String url, Map<String, String> parameters) {
        var i = 0;
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (i == 0) {
                url = url + "?" + key + "=" + value;
            } else {
                url = url + "&" + key + "=" + value;
            }
            i++;
        }
        return url;
    }

    public ZgwZaak getZaakDetails(Map<String, String> parameters) {

        ZgwZaak result = null;
        var zaakJson = get(baseUrl + "/zaken/api/v1/zaken", parameters);
        try {
            Gson gson = new Gson();
            QueryResult queryResult = gson.fromJson(zaakJson, QueryResult.class);
            if (queryResult.getResults().size() == 1) {
                result = queryResult.getResults().get(0);
            }
        } catch (Exception ex) {
            log.error("Exception in getZaakDetails: " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public ZgwZaak addZaak(ZgwZaak zgwZaak) throws HttpStatusCodeException {
        ZgwZaak result = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(zgwZaak);
            String response = this.post(baseUrl + "/zaken/api/v1/zaken", json);
            result = gson.fromJson(response, ZgwZaak.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Exception in addZaak: " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public RolNPS addRolNPS(RolNPS rolNPS) {
        RolNPS result = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(rolNPS);
            String response = this.post(baseUrl + "/zaken/api/v1/rollen", json);
            result = gson.fromJson(response, RolNPS.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Exception in addRolNPS: " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public ZgwEnkelvoudigInformatieObject addDocument(ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) {
        ZgwEnkelvoudigInformatieObject result = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(zgwEnkelvoudigInformatieObject);
            String response = this.post(baseUrl + "/documenten/api/v1/enkelvoudiginformatieobjecten", json);
            result = gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Exception in addDocument: " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    public ZgwZaakInformatieObject addDocumentToZaak(ZgwZaakInformatieObject zgwZaakInformatieObject) {
        ZgwZaakInformatieObject result = null;
        try {
            Gson gson = new Gson();
            String json = gson.toJson(zgwZaakInformatieObject);
            String response = this.post(baseUrl + "/zaken/api/v1/zaakinformatieobjecten", json);
            result = gson.fromJson(response, ZgwZaakInformatieObject.class);
        } catch (HttpStatusCodeException ex) {
            log.error("Exception in addDocument: " + ex.getMessage());
            throw ex;
        }

        return result;

    }

    public List<ZgwEnkelvoudigInformatieObject> getLijstZaakDocumenten(Map<String, String> parameters) {
        var result = new ArrayList();

        try {
            var zaakInformatieObjects = getZgwZaakInformatieObjects(parameters);
            result = (ArrayList) getZgwEnkelvoudigInformatieObjectList(result, zaakInformatieObjects);

        } catch (Exception ex) {
            log.error("Exception in getLijstZaakdocumenten: " + ex.getMessage());
            throw ex;
        }

        return result;
    }

    private List<ZgwEnkelvoudigInformatieObject> getZgwEnkelvoudigInformatieObjectList(List<ZgwEnkelvoudigInformatieObject> tempResult, List<ZgwZaakInformatieObject> zaakInformatieObjects) {
        var result = tempResult;
        zaakInformatieObjects.forEach(zaakInformatieObject -> {
            result.add(getZaakDocument(zaakInformatieObject.getInformatieobject()));
        });

        tempResult = result;
        return tempResult;
    }

    private List<ZgwZaakInformatieObject> getZgwZaakInformatieObjects(Map<String, String> parameters) {
        //Fetch EnkelvoudigInformatieObjects
        var zaakInformatieObjectJson = get(baseUrl + "/zaken/api/v1/zaakinformatieobjecten", parameters);

        Gson gson = new Gson();
        Type documentList = new TypeToken<ArrayList<ZgwZaakInformatieObject>>() {
        }.getType();
        return gson.fromJson(zaakInformatieObjectJson, documentList);
    }

    private ZgwEnkelvoudigInformatieObject getZaakDocument(String url) {
        ZgwEnkelvoudigInformatieObject informatieObject = null;

        var zaakInformatieObjectJson = get(url, null);
        Gson gson = new Gson();
        informatieObject = gson.fromJson(zaakInformatieObjectJson, ZgwEnkelvoudigInformatieObject.class);

        return informatieObject;
    }
}