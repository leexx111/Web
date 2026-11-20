package com.cms.electricityproject;

import com.cms.electricityproject.controller.EnergyTreeController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@EnabledIfEnvironmentVariable(named="ENERGY_TREE_TEST_URL", matches=".+")
@SpringBootTest(classes=EnergyTreeIntegrationTest.TestApp.class)
@AutoConfigureMockMvc
class EnergyTreeIntegrationTest {
    @SpringBootConfiguration @EnableAutoConfiguration @Import(EnergyTreeController.class)
    static class TestApp { }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        if (!System.getenv("ENERGY_TREE_TEST_URL").matches("^jdbc:mysql://[^/]+/codex_energy_tree_test_20261110(?:\\?.*)?$"))
            throw new IllegalStateException("Energy tree integration tests require the dedicated test database");
        r.add("spring.datasource.url", () -> System.getenv("ENERGY_TREE_TEST_URL"));
        r.add("spring.datasource.username", () -> System.getenv("ENERGY_TREE_TEST_USER"));
        r.add("spring.datasource.password", () -> System.getenv("ENERGY_TREE_TEST_PASSWORD"));
        r.add("spring.jpa.hibernate.ddl-auto", () -> "none");
    }

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;
    @Autowired JdbcTemplate jdbc;

    @SuppressWarnings("unchecked")
    Map<String,Object> snapshot() throws Exception {
        String body=mvc.perform(get("/energy-tree")).andExpect(status().isOk()).andReturn().getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8);
        return json.readValue(body, Map.class);
    }
    @SuppressWarnings("unchecked")
    List<Map<String,Object>> list(Map<String,Object> snapshot,String key) {return (List<Map<String,Object>>)snapshot.get(key);}
    Set<String> descendants(List<Map<String,Object>> nodes,String root) {
        Set<String> result=new HashSet<>(); boolean changed;
        do { changed=false; for(Map<String,Object> n:nodes) if(!result.contains(n.get("id")) && (root.equals(n.get("parentId"))||result.contains(n.get("parentId")))) {result.add((String)n.get("id"));changed=true;} } while(changed);
        return result;
    }
    String body(Object v) throws Exception {return json.writeValueAsString(v);}
    Map<String,Object> request(String password,String version,List<Map<String,Object>> nodes) {Map<String,Object> b=new HashMap<>();b.put("password",password);b.put("version",version);if(nodes!=null)b.put("nodes",nodes);return b;}

    @Test void saveAndResetPreserveMeterBindingAndProtectOtherRooms() throws Exception {
        Map<String,Object> before=snapshot();
        List<Map<String,Object>> all=list(before,"current");
        Set<String> scatter=descendants(all,"4"), excluded=descendants(all,"hash_3"); excluded.add("hash_3");
        List<Map<String,Object>> edited=new ArrayList<>();
        for(Map<String,Object> n:all) if(scatter.contains(n.get("id"))&&!excluded.contains(n.get("id"))) edited.add(new HashMap<>(n));
        Map<String,Object> leaf=edited.stream().filter(n->"leaf".equals(n.get("nodeType"))).findFirst().orElseThrow();
        String id=(String)leaf.get("id"), oldMeter=(String)leaf.get("meterKey"), oldPds=(String)leaf.get("pds");
        leaf.put("name","测试名称");leaf.put("displayExtra","测试附加信息");
        Map<String,Object> source=all.stream().filter(n->"leaf".equals(n.get("nodeType"))&&"2#配电室".equals(n.get("pds"))&&n.get("meterKey")!=null&&!n.get("meterKey").toString().isEmpty()).findFirst().orElseThrow();
        Map<String,Object> copy=new HashMap<>(source);copy.put("id","codex-test-copy");copy.put("parentId","hash_2");copy.put("sortOrder",999);copy.put("displayExtra","复制显示信息");edited.add(copy);
        Map<String,Object> missing=new HashMap<>(copy);missing.put("id","codex-test-missing");missing.put("meterKey","2-1AA10-8");missing.put("displayExtra","2-1AA10-8");edited.add(missing);
        String version=(String)before.get("version");
        mvc.perform(post("/energy-tree/save").contentType(MediaType.APPLICATION_JSON).content(body(request("wrong",version,edited)))).andExpect(status().isForbidden());
        assertEquals(version,snapshot().get("version"));
        mvc.perform(post("/energy-tree/save").contentType(MediaType.APPLICATION_JSON).content(body(request("yth",version,edited)))).andExpect(status().isOk());
        Map<String,Object> saved=snapshot();
        Map<String,Object> stored=list(saved,"current").stream().filter(n->id.equals(n.get("id"))).findFirst().orElseThrow();
        assertEquals(oldMeter,stored.get("meterKey"));assertEquals(oldPds,stored.get("pds"));assertEquals("测试附加信息",stored.get("displayExtra"));
        assertEquals(source.get("meterKey"),jdbc.queryForObject("SELECT extra_info FROM tree_node_config WHERE id='codex-test-copy'",String.class));
        assertEquals("2-1AA10-8",jdbc.queryForObject("SELECT extra_info FROM tree_node_config WHERE id='codex-test-missing'",String.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM tree_node_config WHERE id='hash_3'",Integer.class));
        assertEquals(all.stream().filter(n->!scatter.contains(n.get("id"))).count(),list(saved,"current").stream().filter(n->!descendants(list(saved,"current"),"4").contains(n.get("id"))).count());
        mvc.perform(post("/energy-tree/save").contentType(MediaType.APPLICATION_JSON).content(body(request("yth",version,edited)))).andExpect(status().isConflict());
        String newVersion=(String)saved.get("version");
        List<Map<String,Object>> invalid=new ArrayList<>(edited);Map<String,Object> invalidLeaf=new HashMap<>(leaf);invalidLeaf.put("id","bad");invalidLeaf.put("meterKey","not-a-meter");invalid.add(invalidLeaf);
        mvc.perform(post("/energy-tree/save").contentType(MediaType.APPLICATION_JSON).content(body(request("yth",newVersion,invalid)))).andExpect(status().isBadRequest());
        assertEquals(newVersion,snapshot().get("version"));
        mvc.perform(post("/energy-tree/reset").contentType(MediaType.APPLICATION_JSON).content(body(request("yth",newVersion,null)))).andExpect(status().isOk());
        assertEquals(jdbc.queryForObject("SELECT COUNT(*) FROM tree_node_config_init",Integer.class),jdbc.queryForObject("SELECT COUNT(*) FROM tree_node_config",Integer.class));
        assertEquals(0,jdbc.queryForObject("SELECT COUNT(*) FROM tree_node_display_info",Integer.class));
    }
}
