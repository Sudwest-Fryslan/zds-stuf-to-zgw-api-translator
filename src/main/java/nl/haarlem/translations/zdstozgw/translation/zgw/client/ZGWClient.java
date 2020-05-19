package nl.haarlem.translations.zdstozgw.translation.zgw.client;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import nl.haarlem.translations.zdstozgw.translation.zgw.services.HttpService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import nl.haarlem.translations.zdstozgw.config.ZaakType;
import nl.haarlem.translations.zdstozgw.translation.zgw.client.ZGWClient.ZGWClientException;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.QueryResult;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.Rol;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwEnkelvoudigInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwInformatieObjectType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwMedewerker;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwNatuurlijkPersoon;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwRolType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatus;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwStatusType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaak;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakInformatieObject;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZgwZaakType;
import nl.haarlem.translations.zdstozgw.translation.zgw.model.ZwgZaakInformatieObject;

@Service
public class ZGWClient {

	@SuppressWarnings("serial")
	public class ZGWClientException extends Exception {
		protected String details;

		public ZGWClientException(String message, String details, Throwable err) {
			super(message, err);
			this.details = details;
		}

		public String getDetails() {
			return this.details;
		}
	}

	private static final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

	@Value("${openzaak.baseUrl}")
	private String baseUrl;

	@Autowired
    HttpService httpService;

    @Autowired
	RestTemplateService restTemplateService;
	
	private String post(String url, String json) throws ZGWClientException {
		log.debug("POST: " + url + ", json: " + json);
		HttpEntity<String> request = new HttpEntity<String>(json, this.restTemplateService.getHeaders());
		String zgwResponse = null;
		try {
			zgwResponse = this.restTemplateService.getRestTemplate().postForObject(url, request, String.class);
		} catch (HttpStatusCodeException hsce) {

			log.warn("fout met verzendende-json:" + json + "\n" + hsce.getResponseBodyAsString().replace("{", "{\n").replace("\",", "\",\n").replace("\"}", "\"\n}"));
			throw new ZGWClientException("POST naar OpenZaak: " + url + " gaf foutmelding:" + hsce.getMessage(), json,hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			throw new ZGWClientException("POST naar OpenZaak: " + url + " niet geslaagd", json, rae);
		}
		log.debug("POST response: " + zgwResponse);
		return zgwResponse;
	}

	private String get(String url, Map<String, String> parameters) throws ZGWClientException {
		log.info("GET: " + url);

		if (parameters != null) {
			url = getUrlWithParameters(url, parameters);
		}

		HttpEntity entity = new HttpEntity(this.restTemplateService.getHeaders());
		ResponseEntity<String> response = null;
		
		try {
			response = this.restTemplateService.getRestTemplate().exchange(url, HttpMethod.GET, entity, String.class);
		} catch (HttpStatusCodeException hsce) {
			throw new ZGWClientException("GET naar OpenZaak: " + url + " gaf foutmelding" + hsce.getStatusText(), url,
					hsce);
		} catch (org.springframework.web.client.ResourceAccessException rae) {
			throw new ZGWClientException("GET naar OpenZaak: " + url + " niet geslaagd", url, rae);
		}

		log.info("GET response:\n\t" + response.getBody());

		return response.getBody();
	}
	
    public String getBas64Inhoud(String url) throws ZGWClientException {
		log.info("GET BASE64 INHOUD: " + url);    	
    	String result = null;
        try {
            result  = httpService.downloadFile(url);
		} catch (IOException ioe) {
			throw new ZGWClientException("GET BASE64 INHOUD naar OpenZaak: " + url + " gaf foutmelding" + ioe.getMessage(), url, ioe);
		} 
		log.info("GET BASE64 INHOUD:" + result.length() +  " bytes");
        return result;
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

	public ZgwEnkelvoudigInformatieObject getZgwEnkelvoudigInformatieObject(String identificatie) throws ZGWClientException {
		ZgwEnkelvoudigInformatieObject result = null;
		var documentJson = get(
				this.baseUrl + "/documenten/api/v1/enkelvoudiginformatieobjecten?identificatie=" + identificatie, null);
//		try {
			Type type = new TypeToken<QueryResult<ZgwEnkelvoudigInformatieObject>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwEnkelvoudigInformatieObject> queryResult = gson.fromJson(documentJson, type);

			if (queryResult.getResults().size() == 1) {
				result = queryResult.getResults().get(0);
			}
//		} catch (Exception ex) {
//			log.error("ZgwEnkelvoudigInformatieObject: " + ex.getMessage());
//			throw ex;
//		}
		return result;
	}

    public ZgwZaak getZaakByUrl(String url) throws ZGWClientException {
        ZgwZaak result = null;
        var zaakJson = get(url, null);
        Gson gson = new Gson();
        result = gson.fromJson(zaakJson, ZgwZaak.class);
        return result;
    }

    public ZgwZaakType getZaakTypeByUrl(String url) throws ZGWClientException {
    	ZgwZaakType result = null;
        var zaakTypeJson = get(url, null);
        Gson gson = new Gson();
        result = gson.fromJson(zaakTypeJson, ZgwZaakType.class);
        return result;
    }

    //public ZgwZaak getZaakDetails(Map<String, String> parameters) {
	public ZgwZaakType getZaakTypeByIdentiticatie(String identificatie) throws ZGWClientException {
        // url = self.config.GEMMA_ZAKEN_ZTC + 'zaaktypen?status=alles&identificatie=' + str(zaaktypecode) + '&catalogus=' + catalogus.url
        // url = self.config.GEMMA_ZAKEN_ZTC + 'zaaktypen?status=alles&identificatie=' + str(zaaktypecode)
		Map<String, String> parameters = new HashMap();
		parameters.put("identificatie", identificatie);

		var zaakTypeJson = get(this.baseUrl + "/catalogi/api/v1/zaaktypen", parameters);
		ZgwZaakType result = null;
//		try {
			Type type = new TypeToken<QueryResult<ZgwZaakType>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwZaakType> queryResult = gson.fromJson(zaakTypeJson, type);
			if (queryResult.getResults().size() == 1) {
				result = queryResult.getResults().get(0);
			}
//		} catch (Exception ex) {
//			log.error("Exception in getZaakTypeByIdentiticatie: " + ex.getMessage());
//			throw ex;
//		}
		return result;
	}		


	public ZgwRolType getRolTypeByOmschrijving(String zaaktype, String omschrijving) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		//parameters.put("identificatie", identificatie);

		var rolTypeJson = get(this.baseUrl + "/catalogi/api/v1/roltypen", parameters);
		ZgwRolType result = null;
//		try {
			Type type = new TypeToken<QueryResult<ZgwRolType>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwRolType> queryResult = gson.fromJson(rolTypeJson, type);
			for(ZgwRolType found : queryResult.getResults()) {
				if(found.zaaktype.equals(zaaktype) &&  found.omschrijving.equals(omschrijving)) {
					result = found;
				}
			}
//		} catch (Exception ex) {
//			log.error("Exception in getRolTypeByIdentiticatie: " + ex.getMessage());
//			throw ex;
//		}
		return result;
	}	
	
	
	public ZgwZaak getZaakByIdentificatie(String zaakIdentificatie) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		parameters.put("identificatie", zaakIdentificatie);
		return getZaakDetails(parameters);
	}		
	
	public ZgwZaak getZaakDetails(Map<String, String> parameters) throws ZGWClientException {

		ZgwZaak result = null;
		var zaakJson = get(this.baseUrl + "/zaken/api/v1/zaken", parameters);
		//try {
			Type type = new TypeToken<QueryResult<ZgwZaak>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwZaak> queryResult = gson.fromJson(zaakJson, type);
			if (queryResult.getResults().size() == 1) {
				result = queryResult.getResults().get(0);
			}
		//} catch (Exception ex) {
		//	log.error("Exception in getZaak: " + ex.getMessage());
		//	throw ex;
		//}
		return result;
	}

	public ZgwZaak addZaak(ZgwZaak zgwZaak) throws ZGWClientException {
		Gson gson = new Gson();
		String json = gson.toJson(zgwZaak);
		String response = this.post(this.baseUrl + "/zaken/api/v1/zaken", json);
		return gson.fromJson(response, ZgwZaak.class);
	}

	public Rol addRolNPS(Rol rolNPS) throws ZGWClientException {
		Rol result = null;
//		try {
			Gson gson = new Gson();
			String json = gson.toJson(rolNPS);
			String response = this.post(this.baseUrl + "/zaken/api/v1/rollen", json);
			result = gson.fromJson(response, Rol.class);
//		} catch (HttpStatusCodeException ex) {
//			log.error("Exception in addRolNPS: " + ex.getMessage());
//			throw ex;
//		}
		return result;
	}

	public ZgwEnkelvoudigInformatieObject addDocument(ZgwEnkelvoudigInformatieObject zgwEnkelvoudigInformatieObject) throws ZGWClientException {
		ZgwEnkelvoudigInformatieObject result = null;
		//try {
			Gson gson = new Gson();
			String json = gson.toJson(zgwEnkelvoudigInformatieObject);
			String response = this.post(this.baseUrl + "/documenten/api/v1/enkelvoudiginformatieobjecten", json);
			result = gson.fromJson(response, ZgwEnkelvoudigInformatieObject.class);
		//} catch (HttpStatusCodeException ex) {
		//	log.error("Exception in addDocument: " + ex.getMessage());
		//	throw ex;
		//}
		return result;
	}

	public ZgwZaakInformatieObject addDocumentToZaak(ZgwZaakInformatieObject zgwZaakInformatieObject)
			throws ZGWClientException {
		ZgwZaakInformatieObject result = null;
//		try {
			Gson gson = new Gson();
			String json = gson.toJson(zgwZaakInformatieObject);
			String response = this.post(this.baseUrl + "/zaken/api/v1/zaakinformatieobjecten", json);
			result = gson.fromJson(response, ZgwZaakInformatieObject.class);
//		} catch (HttpStatusCodeException ex) {
//			log.error("Exception in addDocument: " + ex.getMessage());
//			throw ex;
//		}
		return result;

	}
/*
	private List<ZgwEnkelvoudigInformatieObject> getZgwEnkelvoudigInformatieObjectList(
			List<ZgwEnkelvoudigInformatieObject> tempResult, List<ZgwZaakInformatieObject> zaakInformatieObjects)
			throws ZGWClientException {
		var result = tempResult;
		for (ZgwZaakInformatieObject zaakInformatieObject : zaakInformatieObjects) {
			result.add(getZaakDocument(zaakInformatieObject.getInformatieobject()));
		}
		tempResult = result;
		return tempResult;
	}
*/
    public List<ZgwZaakInformatieObject> getZgwZaakInformatieObjects(Map<String, String> parameters) throws ZGWClientException {
	//private List<ZgwZaakInformatieObject> getZgwZaakInformatieObjects(Map<String, String> parameters)
	//		throws ZGWClientException {
		// Fetch EnkelvoudigInformatieObjects
		var zaakInformatieObjectJson = get(this.baseUrl + "/zaken/api/v1/zaakinformatieobjecten", parameters);

		Gson gson = new Gson();
		Type documentList = new TypeToken<ArrayList<ZgwZaakInformatieObject>>() {
		}.getType();
		return gson.fromJson(zaakInformatieObjectJson, documentList);
	}

	private ZgwEnkelvoudigInformatieObject getZaakDocument(String url) throws ZGWClientException {
		ZgwEnkelvoudigInformatieObject informatieObject = null;

		var zaakInformatieObjectJson = get(url, null);
		Gson gson = new Gson();
		informatieObject = gson.fromJson(zaakInformatieObjectJson, ZgwEnkelvoudigInformatieObject.class);

		return informatieObject;
	}

	public List<ZgwStatusType> getStatusTypes(Map<String, String> parameters) throws ZGWClientException {
		var statusTypeJson = get(this.baseUrl + "/catalogi/api/v1/statustypen", parameters);
//		try {
			Type type = new TypeToken<QueryResult<ZgwStatusType>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwStatusType> queryResult = gson.fromJson(statusTypeJson, type);
			return queryResult.getResults();
//		} catch (Exception ex) {
//			log.error("Exception in getStatusTypes: " + ex.getMessage());
//			throw ex;
//		}
	}

	public ZgwStatus actualiseerZaakStatus(ZgwStatus zgwSatus) throws ZGWClientException {
		ZgwStatus result = null;
//		try {
			Gson gson = new Gson();
			String json = gson.toJson(zgwSatus);
			String response = this.post(this.baseUrl + "/zaken/api/v1/statussen", json);
			result = gson.fromJson(response, ZgwStatus.class);
//		} catch (HttpStatusCodeException ex) {
//			log.error("Exception in actualiseerZaakStatus: " + ex.getMessage());
//			throw ex;
//		}

		return result;
	}

	private ZgwZaakInformatieObject addZaakInformatieObject(ZgwEnkelvoudigInformatieObject doc, String zaakUrl) throws Exception {
		ZgwZaakInformatieObject result = null;
//		try {
			var zgwZaakInformatieObject = new ZgwZaakInformatieObject();
			zgwZaakInformatieObject.setZaak(zaakUrl);
			zgwZaakInformatieObject.setInformatieobject(doc.getUrl());
			zgwZaakInformatieObject.setTitel(doc.getTitel());
			result = addDocumentToZaak(zgwZaakInformatieObject);

//		} catch (Exception e) {
//			throw e;
//		}
		return result;
	}
	public ZgwStatusType getStatusTypeByZaakTypeAndVolgnummer(String zaakTypeUrl, int volgnummer) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaaktype", zaakTypeUrl);

		return getStatusTypes(parameters).stream().filter(zgwStatusType -> zgwStatusType.volgnummer == volgnummer).findFirst().orElse(null);
	}

	public ZgwInformatieObjectType getZgwInformatieObjectTypeByOmschrijving(String omschrijving) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		parameters.put("status", "definitief");

		var zaakTypeJson = get(this.baseUrl + "/catalogi/api/v1/informatieobjecttypen", parameters);
//		try {
			Type type = new TypeToken<QueryResult<ZgwInformatieObjectType>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<ZgwInformatieObjectType> queryResult = gson.fromJson(zaakTypeJson, type);
			for(ZgwInformatieObjectType current: queryResult.results) {
				log.debug("gevonden ZgwInformatieObjectType met omschrijving: '" + current.omschrijving + "'");
				if(omschrijving.equals(current.omschrijving)) {
					return current;
				}
			}
//		} catch (Exception ex) {
//			log.error("Exception in getZaakTypeByIdentiticatie: " + ex.getMessage());
//			throw ex;
//		}
		return null;
	}

	public List<Rol> getRollenByZaak(String url) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", url);

		var zaakTypeJson = get(this.baseUrl + "/zaken/api/v1/rollen", parameters);
//		try {
			Type type = new TypeToken<QueryResult<Rol>>() {
			}.getType();
			Gson gson = new Gson();
			QueryResult<Rol> queryResult = gson.fromJson(zaakTypeJson, type);
			var result = new ArrayList<Rol>();
			for(Rol current: queryResult.results) {
				log.debug("gevonden rol met omschrijving: '" + current.roltoelichting + "'");
				result.add(current);
			}
//		} catch (Exception ex) {
//			log.error("Exception in getZaakTypeByIdentiticatie: " + ex.getMessage());
//			throw ex;
//		}
		return result;
	}

    public ZgwNatuurlijkPersoon getNatuurlijkPersoonByUrl(String url) throws ZGWClientException {
    	ZgwNatuurlijkPersoon result = null;
        var json = get(url, null);
        Gson gson = new Gson();
        result = gson.fromJson(json, ZgwNatuurlijkPersoon.class);
        return result;
    }

	public ZgwMedewerker getMedewerkerByUrl(String url) throws ZGWClientException {
		ZgwMedewerker result = null;
        var json = get(url, null);
        Gson gson = new Gson();
        result = gson.fromJson(json, ZgwMedewerker.class);
        return result;
	}
/*

	public List<ZgwEnkelvoudigInformatieObject> getLijstZaakDocumenten(Map<String, String> parameters)
			throws ZGWClientException {
		var result = new ArrayList();

//		try {
			var zaakInformatieObjects = getZgwZaakInformatieObjects(parameters);
			result = (ArrayList) getZgwEnkelvoudigInformatieObjectList(result, zaakInformatieObjects);

//		} catch (Exception ex) {
//			log.error("Exception in getLijstZaakdocumenten: " + ex.getMessage());
//			throw ex;
//		}

		return result;
	} */
	
	public List<ZwgZaakInformatieObject> getZaakInformatieObjectenByZaakUrl(String url) throws ZGWClientException {
		Map<String, String> parameters = new HashMap();
		parameters.put("zaak", url);
		var json = get(this.baseUrl + "zaken/api/v1/zaakinformatieobjecten", parameters);
		Type type = new TypeToken<QueryResult<ZwgZaakInformatieObject>>() {}.getType();
		Gson gson = new Gson();
		
		ZwgZaakInformatieObject[] objects = gson.fromJson(json, ZwgZaakInformatieObject[].class);
		List<ZwgZaakInformatieObject> result = new  ArrayList<ZwgZaakInformatieObject>();		
		Collections.addAll(result, objects);
		return result;
	}

	public ZgwInformatieObject getInformatieObjectByUrl(String url) throws ZGWClientException {
		var json = get(url, null);
        Gson gson = new Gson();
        ZgwInformatieObject result = gson.fromJson(json, ZgwInformatieObject.class);
        return result;
	}
}

