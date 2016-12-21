package org.recap.controller.swagger;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.annotations.*;
import org.recap.ReCAPConstants;
import org.recap.model.ItemInformationResponse;
import org.recap.model.SearchRecordsRequest;
import org.recap.model.SearchResultRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.*;

/**
 * Created by sudhish on 13/10/16.
 */
@RestController
@RequestMapping("/searchService")
@Api(value="search", description="Search Records", position = 1)
public class SearchReordsRestController {

    private Logger logger = LoggerFactory.getLogger(SearchReordsRestController.class);

    @Value("${server.protocol}")
    String serverProtocol;

    @Value("${scsb.solr.client.url}")
    String scsbSolrClient;


//    @RequestMapping(value="/search", method = RequestMethod.GET)
//    @ApiOperation(value = "search",notes = "Search Books in ReCAP - Using Method Post, Request data is String", nickname = "search", position = 0, consumes="application/json")
//    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public List<SearchResultRow> searchRecordsServiceGetParam(@ApiParam(value = "Paramerters for Searching Records" , required = true, name="requestJson") @RequestParam String requestJson) {

//        logger.info("Get " +requestJson);
        HttpEntity<List> responseEntity = null;
        RestTemplate restTemplate = new RestTemplate();
        ObjectMapper mapper = new ObjectMapper();
        SearchRecordsRequest searchRecordsRequest = null;
        try {
            searchRecordsRequest = mapper.readValue(requestJson, SearchRecordsRequest.class);
        } catch (IOException e) {
            logger.info("search : "+e.getMessage());
        }
        if (searchRecordsRequest ==null){
            searchRecordsRequest = new SearchRecordsRequest();
        }
        List<SearchResultRow> searchResultRows = null;
        try {
            HttpEntity request = new HttpEntity(searchRecordsRequest);
            responseEntity = restTemplate.exchange(serverProtocol + scsbSolrClient + ReCAPConstants.URL_SEARChBYJSON , HttpMethod.GET, request, List.class);
            searchResultRows = responseEntity.getBody();
        } catch (Exception e) {
            searchResultRows = new ArrayList<>();
        }
        return searchResultRows;
    }

    @RequestMapping(value="/searchByParam", method = RequestMethod.GET)
    @ApiOperation(value = "searchParam",notes = "Search Books in ReCAP - Using Method GET, Request data as parameter", nickname = "search", position = 0)
    @ApiResponses(value = {@ApiResponse(code = 200, message = "Successful Search")})
    public List<SearchResultRow> searchRecordsServiceGet(
            @RequestParam(name="fieldValue", required = false)  String fieldValue,
            @ApiParam(name="fieldName",required = false,allowableValues = "Author_search,Title_search,TitleStartsWith,Publisher,PublicationPlace,PublicationDate,Subject,ISBN,ISSN,OCLCNumber,Notes,CallNumber_search,Barcode") @RequestParam(name="fieldName", value = "fieldName" , required = false)  String fieldName,
            @ApiParam(name="owningInstitutions", value= "Owning Institutions : PUL, CUL, NYPL")@RequestParam(name="owningInstitutions",required = false ) String owningInstitutions,
            @ApiParam(name="collectionGroupDesignations", value = "collection Designations : Shared,Private,Open") @RequestParam(name="collectionGroupDesignations", value = "collectionGroupDesignations" , required = false)  String collectionGroupDesignations,
            @ApiParam(name="availability", value = "Availability: Available, NotAvailable") @RequestParam(name="availability", value = "availability" , required = false)  String availability,
            @ApiParam(name="materialTypes", value = "MaterialTypes: Monograph, Serial, Other") @RequestParam(name="materialTypes", value = "materialTypes" , required = false)  String materialTypes,
            @ApiParam(name="useRestrictions", value = "Use Restrictions: NoRestrictions, InLibraryUse, SupervisedUse") @RequestParam(name="useRestrictions", value = "useRestrictions" , required = false)  String useRestrictions,
            @ApiParam(name="pageSize", value = "Page Size in Numers - 10, 20 30...") @RequestParam(name="pageSize", required = false) Integer pageSize
    ) {
        HttpEntity<List> responseEntity = null;
        HttpEntity request = new HttpEntity(getHttpHeaders());
        RestTemplate restTemplate = new RestTemplate();
        List<SearchResultRow> searchResultRows = null;
        try {

            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(serverProtocol + scsbSolrClient + ReCAPConstants.URL_SEARChBYPARAM)
                    .queryParam("fieldValue", fieldValue)
                    .queryParam("fieldName", fieldName)
                    .queryParam("owningInstitutions", owningInstitutions)
                    .queryParam("collectionGroupDesignations", collectionGroupDesignations)
                    .queryParam("availability", availability)
                    .queryParam("materialTypes", materialTypes)
                    .queryParam("useRestrictions", useRestrictions)
                    .queryParam("pageSize", pageSize);

            responseEntity = restTemplate.exchange(builder.build().encode().toUri(), HttpMethod.GET, request, List.class);
            searchResultRows = responseEntity.getBody();
        } catch (Exception e) {
            searchResultRows = new ArrayList<>();
            logger.error("Exception",e);
        }
        return searchResultRows;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("api_key", "recap");
        return headers;
    }
}