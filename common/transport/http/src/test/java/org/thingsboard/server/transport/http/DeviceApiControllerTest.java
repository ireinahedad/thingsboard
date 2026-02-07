/**
 * Copyright © 2016-2026 The Thingsboard Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thingsboard.server.transport.http;

import com.google.gson.JsonParseException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.context.request.async.DeferredResult;
import org.thingsboard.server.common.data.DeviceTransportType;
import org.thingsboard.server.common.transport.TransportContext;
import org.thingsboard.server.common.transport.TransportServiceCallback;
import org.thingsboard.server.common.transport.auth.ValidateDeviceCredentialsResponse;
import org.thingsboard.server.gen.transport.TransportProtos;
import org.thingsboard.server.gen.transport.TransportProtos.ValidateDeviceTokenRequestMsg;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.function.Consumer;

class DeviceApiControllerTest {

    @Test
    void deviceAuthCallbackTest() {
        TransportContext transportContext = Mockito.mock(TransportContext.class);
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        Consumer<TransportProtos.SessionInfoProto> onSuccess = x -> {
        };
        var callback = new DeviceApiController.DeviceAuthCallback(transportContext, responseWriter, onSuccess);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

    @Test
    void deviceProvisionCallbackTest() {
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        var callback = new DeviceApiController.DeviceProvisionCallback(responseWriter);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

    @Test
    void getOtaPackageCallback() {
        TransportContext transportContext = Mockito.mock(TransportContext.class);
        DeferredResult<ResponseEntity> responseWriter = Mockito.mock(DeferredResult.class);
        String title = "Title";
        String version = "version";
        int chunkSize = 11;
        int chunk = 3;

        var callback = new DeviceApiController.GetOtaPackageCallback(transportContext, responseWriter, title, version, chunkSize, chunk);

        callback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));

        callback.onError(new JsonParseException("Json ; expected"));

        callback.onError(new IOException("not found"));

        callback.onError(new RuntimeException("oops it is run time error"));
    }

//TEST FOR NEW FUNCTIONALITY

@Test
void getServerTimeCallbackTest() throws Exception {
    // Mock des objets
    var transportContext = Mockito.mock(org.thingsboard.server.transport.http.HttpTransportContext.class);
    var transportService = Mockito.mock(org.thingsboard.server.common.transport.TransportService.class);
    when(transportContext.getTransportService()).thenReturn(transportService);
    when(transportContext.getDefaultTimeout()).thenReturn(1000L);

    var controller = new DeviceApiController();

    // Injecter le mock dans le champ privé avec reflection
    java.lang.reflect.Field field = DeviceApiController.class.getDeclaredField("transportContext");
    field.setAccessible(true);
    field.set(controller, transportContext);

    String deviceToken = "validToken";
    DeferredResult<ResponseEntity> result = controller.getServerTime(deviceToken);

    // Capturer le TransportServiceCallback passé à process()
    ArgumentCaptor<org.thingsboard.server.common.transport.TransportServiceCallback> captor =
            ArgumentCaptor.forClass(org.thingsboard.server.common.transport.TransportServiceCallback.class);

    verify(transportService).process(
            eq(DeviceTransportType.DEFAULT),
            any(ValidateDeviceTokenRequestMsg.class),
            captor.capture()
    );

    // Simuler l'appel réussi du callback
    var callback = captor.getValue();
    callback.onSuccess(Mockito.mock(org.thingsboard.server.gen.transport.TransportProtos.SessionInfoProto.class));

    // Simuler différentes erreurs comme dans les autres tests
    var deviceAuthCallback = new DeviceApiController.DeviceAuthCallback(transportContext, Mockito.mock(DeferredResult.class), x -> {});
    deviceAuthCallback.onError(new HttpMessageNotReadableException("JSON incorrect syntax"));
    deviceAuthCallback.onError(new com.google.gson.JsonParseException("Json ; expected"));
    deviceAuthCallback.onError(new IOException("not found"));
    deviceAuthCallback.onError(new RuntimeException("oops it is run time error"));
}
}
