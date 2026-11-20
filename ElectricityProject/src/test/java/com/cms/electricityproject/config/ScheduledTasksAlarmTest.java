package com.cms.electricityproject.config;

import com.cms.electricityproject.server.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import static org.mockito.Mockito.*;

class ScheduledTasksAlarmTest {
    private static Map<Object, Object> reading(String value) {
        Map<Object, Object> result = new LinkedHashMap<>();
        result.put("3AA16-2.DI1", value);
        return result;
    }

    @Test
    void failedReadBreaksConsecutiveConfirmation() {
        BacnetTool bacnet = mock(BacnetTool.class);
        UserService service = mock(UserService.class);
        ScheduledTasks task = new ScheduledTasks();
        ReflectionTestUtils.setField(task, "bacnetTool", bacnet);
        ReflectionTestUtils.setField(task, "userService", service);
        when(bacnet.baojing()).thenReturn(reading("1.0"), reading("1.0"), reading("0.0"),
                Collections.emptyMap(), reading("0.0"), reading("0.0"));
        task.KGbaojing();
        task.KGbaojing();
        task.KGbaojing();
        task.KGbaojing();
        task.KGbaojing();
        verify(service, never()).handleDiAlarmChange("3AA16-2.DI1", "1", "0");
        task.KGbaojing();
        verify(service, times(1)).handleDiAlarmChange("3AA16-2.DI1", "1", "0");
    }

    @Test
    void initialZeroDoesNotCreateAlarmAndLaterTransitionDoes() {
        BacnetTool bacnet = mock(BacnetTool.class);
        UserService service = mock(UserService.class);
        ScheduledTasks task = new ScheduledTasks();
        ReflectionTestUtils.setField(task, "bacnetTool", bacnet);
        ReflectionTestUtils.setField(task, "userService", service);
        when(bacnet.baojing()).thenReturn(reading("0.0"), reading("0.0"),
                reading("1.0"), reading("1.0"), reading("0.0"), reading("0.0"));
        task.KGbaojing();
        task.KGbaojing();
        verify(service, never()).handleDiAlarmChange(anyString(), anyString(), anyString());
        verify(service, never()).reconcileInitialDiNormal(anyString());
        task.KGbaojing();
        task.KGbaojing();
        verify(service).handleDiAlarmChange("3AA16-2.DI1", "0", "1");
        task.KGbaojing();
        task.KGbaojing();
        verify(service).handleDiAlarmChange("3AA16-2.DI1", "1", "0");
    }

    @Test
    void initialOneReconcilesOnlyAfterTwoSuccessfulReadings() {
        BacnetTool bacnet = mock(BacnetTool.class);
        UserService service = mock(UserService.class);
        ScheduledTasks task = new ScheduledTasks();
        ReflectionTestUtils.setField(task, "bacnetTool", bacnet);
        ReflectionTestUtils.setField(task, "userService", service);
        when(bacnet.baojing()).thenReturn(reading("1.0"), Collections.emptyMap(), reading("1.0"), reading("1.0"));
        task.KGbaojing();
        task.KGbaojing();
        task.KGbaojing();
        verify(service, never()).reconcileInitialDiNormal(anyString());
        task.KGbaojing();
        verify(service).reconcileInitialDiNormal("3AA16-2.DI1");
        verify(service, never()).handleDiAlarmChange(anyString(), anyString(), anyString());
        verify(service, never()).broadcastMessage(anyMap());
    }
    @Test
    void blockedPointDoesNotReconcileOrRaiseAlarm() {
        BacnetTool bacnet = mock(BacnetTool.class);
        UserService service = mock(UserService.class);
        ScheduledTasks task = new ScheduledTasks();
        ReflectionTestUtils.setField(task, "bacnetTool", bacnet);
        ReflectionTestUtils.setField(task, "userService", service);
        Map<Object, Object> normal = new LinkedHashMap<>();
        Map<Object, Object> trip = new LinkedHashMap<>();
        normal.put("3AA16-3.DI1", "1.0");
        trip.put("3AA16-3.DI1", "0.0");
        when(bacnet.baojing()).thenReturn(normal, normal, trip, trip);
        task.KGbaojing(); task.KGbaojing(); task.KGbaojing(); task.KGbaojing();
        verify(service, never()).reconcileInitialDiNormal(anyString());
        verify(service, never()).handleDiAlarmChange(anyString(), anyString(), anyString());
        verify(service, never()).broadcastMessage(anyMap());
    }
}
