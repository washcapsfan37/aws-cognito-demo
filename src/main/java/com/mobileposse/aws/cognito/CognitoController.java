package com.mobileposse.aws.cognito;

import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentity;
import com.amazonaws.services.cognitoidentity.model.ListIdentitiesRequest;
import com.amazonaws.services.cognitoidentity.model.ListIdentitiesResult;
import com.amazonaws.services.cognitosync.AmazonCognitoSync;
import com.amazonaws.services.cognitosync.model.DeleteDatasetRequest;
import com.amazonaws.services.cognitosync.model.DeleteDatasetResult;
import com.amazonaws.services.cognitosync.model.ListDatasetsRequest;
import com.amazonaws.services.cognitosync.model.ListDatasetsResult;
import com.amazonaws.services.cognitosync.model.ListRecordsRequest;
import com.amazonaws.services.cognitosync.model.ListRecordsResult;
import com.amazonaws.services.cognitosync.model.Operation;
import com.amazonaws.services.cognitosync.model.RecordPatch;
import com.amazonaws.services.cognitosync.model.UpdateRecordsRequest;
import com.amazonaws.services.cognitosync.model.UpdateRecordsResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.config.ResourceNotFoundException;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;

@RestController
public class CognitoController {

    @Value("${com.mobileposse.aws.cognito.identityPoolId}")
    String identityPoolId = null;

    @Autowired AmazonCognitoIdentity amazonCognitoIdentity;
    @Autowired AmazonCognitoSync amazonCognitoSync;

    @RequestMapping(value="/rest/api/cognito", method= RequestMethod.GET, produces="application/json")
    public ListIdentitiesResult list() {
        ListIdentitiesRequest request = new ListIdentitiesRequest()
                .withIdentityPoolId(identityPoolId)
                .withMaxResults(10);
        ListIdentitiesResult result = amazonCognitoIdentity.listIdentities(request);
        return result;
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}", method= RequestMethod.GET, produces="application/json")
    public ListDatasetsResult getDatasets(@PathVariable String identityId) {
        if (StringUtils.hasLength(identityId)) {
            ListDatasetsRequest request = new ListDatasetsRequest()
                    .withIdentityId(identityId)
                    .withIdentityPoolId(identityPoolId)
                    .withMaxResults(10);
            return amazonCognitoSync.listDatasets(request);
        }
        throw new ResourceNotFoundException("Invalid identity ID", null);
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}/dataset/{datasetName}", method= RequestMethod.DELETE, produces="application/json")
    public DeleteDatasetResult deleteDatasets(@PathVariable String identityId, @PathVariable String datasetName) {
        if (StringUtils.hasLength(identityId)) {
            DeleteDatasetRequest request = new DeleteDatasetRequest()
                    .withIdentityId(identityId)
                    .withIdentityPoolId(identityPoolId)
                    .withDatasetName(datasetName);
            DeleteDatasetResult result = amazonCognitoSync.deleteDataset(request);
            return result;
        }
        throw new ResourceNotFoundException("Invalid identity ID", null);
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}/dataset/{datasetName}",
            method= RequestMethod.GET, produces="application/json")
    public ListRecordsResult listRecords(@PathVariable String identityId, @PathVariable String datasetName) {
        if (StringUtils.hasLength(identityId)) {
            ListRecordsRequest request = new ListRecordsRequest()
                    .withIdentityId(identityId)
                    .withIdentityPoolId(identityPoolId)
                    .withDatasetName(datasetName)
                    .withMaxResults(10);
            return amazonCognitoSync.listRecords(request);
        }
        throw new ResourceNotFoundException("Invalid identity ID or data set name", null);
    }

    private UpdateRecordsResult createOrUpdateRecord(String identityId, String datasetName, RecordModel model) {
        UpdateRecordsRequest request = new UpdateRecordsRequest()
                .withIdentityId(identityId)
                .withIdentityPoolId(identityPoolId)
                .withSyncSessionToken(model.getSyncSessionToken())
                .withDatasetName(datasetName);

        RecordPatch patch = new RecordPatch();
        patch.setOp(Operation.Replace);
        patch.setKey(model.getKey());
        patch.setValue(model.getValue());
        patch.setSyncCount(Long.parseLong(model.getSyncCount()));

        ArrayList<RecordPatch> patches = new ArrayList<>();
        patches.add(patch);

        request.setRecordPatches(patches);

        return amazonCognitoSync.updateRecords(request);
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}/dataset/{datasetName}",
            method= RequestMethod.POST, produces="application/json", consumes="application/json")
    public UpdateRecordsResult updateRecord(@PathVariable String identityId, @PathVariable String datasetName,
                                          @RequestBody RecordModel model) {
        if (StringUtils.hasLength(identityId) && StringUtils.hasLength(datasetName)) {
            return createOrUpdateRecord(identityId, datasetName, model);
        }
        throw new ResourceNotFoundException("Invalid identity ID or data set name", null);
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}/dataset/{datasetName}",
            method= RequestMethod.PUT, produces="application/json", consumes="application/json")
    public UpdateRecordsResult addRecord(@PathVariable String identityId, @PathVariable String datasetName,
                                         @RequestBody RecordModel model) {
        if (StringUtils.hasLength(identityId) && StringUtils.hasLength(datasetName)) {
            return createOrUpdateRecord(identityId, datasetName, model);
        }
        throw new ResourceNotFoundException("Invalid identity ID or data set name", null);
    }

    @RequestMapping(value="/rest/api/cognito/id/{identityId}/dataset/{datasetName}/record/{key}",
            method= RequestMethod.DELETE, produces="application/json")
    public UpdateRecordsResult deleteRecord(@PathVariable String identityId, @PathVariable String datasetName,
                                            @PathVariable String key, HttpServletRequest req) {
        String syncSessionToken = req.getParameter("syncSessionToken");
        Long syncCount = Long.parseLong(req.getParameter("syncCount"));
        if (StringUtils.hasLength(identityId) && StringUtils.hasLength(datasetName)
                && StringUtils.hasLength(syncSessionToken)) {
            UpdateRecordsRequest request = new UpdateRecordsRequest()
                    .withIdentityId(identityId)
                    .withIdentityPoolId(identityPoolId)
                    .withSyncSessionToken(syncSessionToken)
                    .withDatasetName(datasetName);

            RecordPatch patch = new RecordPatch();
            patch.setOp(Operation.Remove);
            patch.setKey(key);
            patch.setSyncCount(syncCount);

            ArrayList<RecordPatch> patches = new ArrayList<>();
            patches.add(patch);

            request.setRecordPatches(patches);

            return amazonCognitoSync.updateRecords(request);
        }
        throw new ResourceNotFoundException("Invalid identity ID or data set name", null);
    }

}
