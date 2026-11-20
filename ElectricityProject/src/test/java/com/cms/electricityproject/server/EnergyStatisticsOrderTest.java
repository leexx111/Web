package com.cms.electricityproject.server;

import com.cms.electricityproject.dao.HDBDao;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EnergyStatisticsOrderTest {
    @Test void responseFollowsRequestEvenWhenDatabaseReturnsAnotherOrder() {
        HDBDao dao=mock(HDBDao.class);
        UserService service=new UserService();
        ReflectionTestUtils.setField(service,"hdbDao",dao);
        List<String> requested=Arrays.asList("1AA6-2","1AA6-10","1AA6-1","1AA6-2");
        List<Map<String,Object>> rows=new ArrayList<>();
        for(String name:Arrays.asList("1AA6-1","1AA6-2")) {
            Map<String,Object> row=new HashMap<>();row.put("name",name);row.put("startValue",10);row.put("endValue",20);rows.add(row);
        }
        when(dao.batchGetNhtj(anyList(),anyString(),anyString())).thenReturn(rows);
        List<Map<String,String>> result=service.getnhtj(requested,"2026-01-01 00:00:00","2026-01-02 00:00:00",null);
        assertEquals(requested,result.stream().map(item->item.get("name")).collect(Collectors.toList()));
        assertEquals("N/A",result.get(1).get("起始表读"));
    }
}
