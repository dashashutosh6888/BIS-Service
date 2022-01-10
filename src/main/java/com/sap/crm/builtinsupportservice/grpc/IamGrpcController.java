package com.sap.crm.builtinsupportservice.grpc;

import com.sap.crm.iamserviceproto.IamServiceGrpc;
import com.sap.crm.iamserviceproto.IamServiceProto;
import com.sap.crm.iamserviceproto.IamServiceProto.TenantInfoByIdResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
public class IamGrpcController {
    long DEADLINE_DURATION = 60000;
    @Value("${iam-service.grpc.host}")
    private String serverHost;
    @Value("${iam-service.grpc.port}")
    private int serverPort;
    private ManagedChannel iamServiceChannel;
    List<Integer> testList = new ArrayList<>();

    @GetMapping("/tenants/{tenantId}")
    public TenantInfoByIdResponse getTenantInfoById(@PathVariable String tenantId) {
        try {
            IamServiceProto.TenantInfoByIdRequest request =
                    IamServiceProto.TenantInfoByIdRequest.newBuilder()
                            .setTenantId(tenantId)
                            .build();

            IamServiceProto.TenantInfoByIdResponse response =
                    iamServiceBlockingStubWrapper(serverHost, serverPort, null, null, null)
                            .getTenantInfoById(request);

            if (response != null) {
                System.out.println(response);
                return response;
            }
        } catch (StatusRuntimeException ex) {
            System.out.println("IAM grpc error: " + ex);
        }
        catch (Exception ex) {
            System.out.println("IAM grpc error: " + ex);
        }

        return null;
    }

    // @GetMapping("/get/{id}")
    // public int getNumById(@PathVariable int id){
    //     return testList.get(id);
    // }

    @RequestMapping(value = "/test", method=RequestMethod.GET)
    public int test(){
        return 123;
    }

    @RequestMapping(value = "/addnum", produces = "application/json", method=RequestMethod.POST)
    public List<Integer> addNum(@RequestBody String obj){
        JSONObject jsonObj = new JSONObject(obj);
        testList.add(jsonObj.getInt("num"));
        return testList;
    }

    // @PutMapping("/update/{id}")
    // public List<Integer> updateNumById(@RequestBody String obj, @PathVariable int id){
    //     JSONObject jsonObj = new JSONObject(obj);
    //     testList.set(id-1, jsonObj.getInt("num"));
    //     return testList;
    // }

    public IamServiceGrpc.IamServiceBlockingStub iamServiceBlockingStubWrapper(
            String serverHost, int serverPort, String authToken, String requestId, String b3TraceId) {

        if (this.iamServiceChannel == null) {
            iamServiceChannel = ManagedChannelBuilder.forAddress(serverHost, serverPort).usePlaintext().build();
        }
        return IamServiceGrpc.newBlockingStub(iamServiceChannel)
                .withDeadlineAfter(DEADLINE_DURATION, TimeUnit.MILLISECONDS);
    }

}
