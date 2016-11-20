package com.telekom.m2m.cot.restsdk;

import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceControlApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentials;
import com.telekom.m2m.cot.restsdk.devicecontrol.DeviceCredentialsApi;
import com.telekom.m2m.cot.restsdk.devicecontrol.Operation;
import com.telekom.m2m.cot.restsdk.identity.ExternalId;
import com.telekom.m2m.cot.restsdk.identity.IdentityApi;
import com.telekom.m2m.cot.restsdk.inventory.InventoryApi;
import com.telekom.m2m.cot.restsdk.inventory.ManagedObject;
import com.telekom.m2m.cot.restsdk.util.CotSdkException;
import com.telekom.m2m.cot.restsdk.util.TestHelper;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Created by breucking on 31.01.16.
 */
public class CotDeviceRegisterIT {


    @Test
    public void testDeviceRegister() throws Exception {
        String deviceId = "mydevice-name";

        CloudOfThingsPlatform platform = new CloudOfThingsPlatform(TestHelper.TEST_HOST, TestHelper.TEST_TENANT, TestHelper.TEST_USERNAME, TestHelper.TEST_PASSWORD);
        DeviceControlApi deviceControlApi = platform.getDeviceControlApi();


        DeviceCredentialsApi unregDevCred = CloudOfThingsPlatform.getPlatformToRegisterDevice(TestHelper.TEST_HOST).getDeviceCredentialsApi();


        // Step 1: (devicemanager) Register Device
        Operation operation = new Operation(deviceId);
        deviceControlApi.createNewDevice(operation);

        // Step 2: (device) Device request
        try {
            unregDevCred.getCredentials(deviceId);
            Assert.fail("Should throw 404 Exceptions");
        } catch (CotSdkException e) {
            Assert.assertEquals(e.getHttpStatus(), 404);
        }

        // Step 3: (devicemanager) Accept request
        deviceControlApi.acceptDevice(deviceId);

        // Step 4: (device) Create MO
        DeviceCredentials devCred = unregDevCred.getCredentials(deviceId);

        Assert.assertNotNull(devCred);
        Assert.assertNotNull(devCred.getId());
        Assert.assertEquals(devCred.getId(), deviceId);
        Assert.assertNotNull(devCred.getPassword());
        Assert.assertNotNull(devCred.getTenantId());
        Assert.assertNotNull(devCred.getUsername());

        CloudOfThingsPlatform platformForDevice = new CloudOfThingsPlatform(TestHelper.TEST_HOST, devCred.getTenantId(), devCred.getUsername(), devCred.getPassword());

        //IdentityApi identityApi = platform.getIdentityApi();
        //ExternalId identity = identityApi.getExternalId("");
        //Assert.assertNull(identity, "Should be null, else before running test run leaved garbadge");

        InventoryApi inventory = platformForDevice.getInventoryApi();

        ManagedObject mo = new ManagedObject();
        mo.setName("test_" + deviceId);
        mo.set("c8y_IsDevice", new Object());
        ManagedObject newMo = inventory.create(mo);

        Assert.assertNotNull(newMo.getId());

        // Step 6: (device) Delete Device
        inventory.delete(newMo.getId());

        try {
            ManagedObject object = inventory.get(newMo.getId());
            Assert.fail("Should throw 404 Exception because it is deleted.");
        } catch (CotSdkException e) {
        }


    }

    @Test
    public void testDeviceRegisterWithIdentity() throws Exception {
        String deviceId = "mydevice-name";

        CloudOfThingsPlatform platform = new CloudOfThingsPlatform(TestHelper.TEST_HOST, TestHelper.TEST_TENANT, TestHelper.TEST_USERNAME, TestHelper.TEST_PASSWORD);
        DeviceControlApi deviceControlApi = platform.getDeviceControlApi();


        DeviceCredentialsApi unregDevCred = CloudOfThingsPlatform.getPlatformToRegisterDevice("https://management.int2-ram.m2m.telekom.com").getDeviceCredentialsApi();


        // Step 1: (devicemanager) Register Device
        Operation operation = new Operation(deviceId);
        deviceControlApi.createNewDevice(operation);

        // Step 2: (device) Device request
        try {
            unregDevCred.getCredentials(deviceId);
        } catch (CotSdkException e) {

        }

        // Step 3: (devicemanager) Accept request
        deviceControlApi.acceptDevice(deviceId);

        // Step 4: (device) Create MO
        DeviceCredentials devCred = unregDevCred.getCredentials(deviceId);

        Assert.assertNotNull(devCred);
        Assert.assertNotNull(devCred.getId());
        Assert.assertNotNull(devCred.getPassword());
        Assert.assertNotNull(devCred.getTenantId());
        Assert.assertNotNull(devCred.getUsername());

        CloudOfThingsPlatform platformForDevice = new CloudOfThingsPlatform(TestHelper.TEST_HOST, devCred.getTenantId(), devCred.getUsername(), devCred.getPassword());

        InventoryApi inventory = platformForDevice.getInventoryApi();

        ManagedObject mo = new ManagedObject();
        mo.setName("test_" + deviceId);
        mo.set("c8y_IsDevice", new Object());
        ManagedObject newMo = inventory.create(mo);

        Assert.assertNotNull(newMo.getId());

        // Step 5: (device) Register the device
        String idString = "fantasy-" + TestHelper.getRandom(10);
        IdentityApi devIdentityApi = platformForDevice.getIdentityApi();
        ExternalId externalId = new ExternalId();
        externalId.setExternalId(new String(idString));
        externalId.setType("com_telekom_SerialNumber");
        externalId.setManagedObject(newMo);

        ExternalId retrievedExtId = devIdentityApi.create(externalId);
        Assert.assertEquals(retrievedExtId.getExternalId(), idString);
        Assert.assertEquals(retrievedExtId.getType(), "com_telekom_SerialNumber");

        ExternalId checkExtId = devIdentityApi.getExternalId(retrievedExtId);
        Assert.assertEquals(checkExtId.getExternalId(), retrievedExtId.getExternalId());
        Assert.assertEquals(checkExtId.getType(), retrievedExtId.getType());
        Assert.assertEquals(checkExtId.getManagedObject().getId(), retrievedExtId.getManagedObject().getId());
        Assert.assertEquals(checkExtId.getManagedObject().getName(), retrievedExtId.getManagedObject().getName());

        // Step 6: (device) Delete Identity & Device
        devIdentityApi.delete(retrievedExtId);

        try {
            devIdentityApi.getExternalId(retrievedExtId);
            Assert.fail("Should throw Exception, because is deleted.");
        } catch (CotSdkException e) {

        }
        try {
            inventory.delete(newMo.getId());
            Assert.fail("Should throw Exception, because is deleted.");
        } catch (CotSdkException e) {

        }

    }

    @Test
    public void testErrorHandlingDeviceRegisterWithIdentity() throws Exception {
        String deviceId = "mydevice-name";

        CloudOfThingsPlatform platform = new CloudOfThingsPlatform(TestHelper.TEST_HOST, TestHelper.TEST_TENANT, TestHelper.TEST_USERNAME, TestHelper.TEST_PASSWORD);
        DeviceControlApi deviceControlApi = platform.getDeviceControlApi();


        DeviceCredentialsApi unregDevCred = CloudOfThingsPlatform.getPlatformToRegisterDevice("https://management.int2-ram.m2m.telekom.com").getDeviceCredentialsApi();


        // Step 1: (devicemanager) Register Device
        Operation operation = new Operation(deviceId);
        try {
            deviceControlApi.createNewDevice(operation);
        } catch (CotSdkException e) {

        }

        // Step 2: (device) Device request
        try {
            unregDevCred.getCredentials(deviceId);
        } catch (CotSdkException e) {

        }

        // Step 3: (devicemanager) Accept request
        deviceControlApi.acceptDevice(deviceId + "failme");

        // Step 4: (device) Create MO
        DeviceCredentials devCred;
        try {
            devCred = unregDevCred.getCredentials(deviceId + "foo");
            Assert.fail("Expected Exception");
        } catch (CotSdkException e) {
            Assert.assertEquals(e.getHttpStatus(), 404);
            deviceControlApi.acceptDevice(deviceId);
            devCred = unregDevCred.getCredentials(deviceId);
        }

        Assert.assertNotNull(devCred);
        Assert.assertNotNull(devCred.getId());
        Assert.assertNotNull(devCred.getPassword());
        Assert.assertNotNull(devCred.getTenantId());
        Assert.assertNotNull(devCred.getUsername());

        CloudOfThingsPlatform platformForDevice = new CloudOfThingsPlatform(TestHelper.TEST_HOST, devCred.getTenantId(), devCred.getUsername(), devCred.getPassword());

        InventoryApi inventory = platformForDevice.getInventoryApi();

        ManagedObject mo = new ManagedObject();
        mo.setName("test_" + deviceId);
        mo.set("c8y_IsDevice", new Object());
        ManagedObject newMo = inventory.create(mo);

        Assert.assertNotNull(newMo.getId());

        // Step 5: (device) Register the device
        String idString = "fantasy-" + TestHelper.getRandom(10);
        IdentityApi devIdentityApi = platformForDevice.getIdentityApi();
        ExternalId externalId = new ExternalId();
        externalId.setExternalId(new String(idString));
        externalId.setType("com_telekom_SerialNumber");
        externalId.setManagedObject(newMo);

        ExternalId retrievedExtId = devIdentityApi.create(externalId);
        Assert.assertEquals(retrievedExtId.getExternalId(), idString);
        Assert.assertEquals(retrievedExtId.getType(), "com_telekom_SerialNumber");

        ExternalId failingExtId = new ExternalId();
        failingExtId.setExternalId("fail");

        try {
            ExternalId checkExtId = devIdentityApi.getExternalId(failingExtId);
        } catch (CotSdkException e) {

        }

        // Step 6: (device) Delete Identity & Device
        //devIdentityApi.delete(retrievedExtId);

        //Assert.assertNull(devIdentityApi.getExternalId(retrievedExtId));

        inventory.delete(newMo.getId());

        try {
            ManagedObject object = inventory.get(newMo.getId());
        } catch (CotSdkException e) {

        }

    }
}
