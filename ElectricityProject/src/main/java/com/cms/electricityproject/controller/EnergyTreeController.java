package com.cms.electricityproject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

@RestController
@RequestMapping("/energy-tree")
/**
 * 提供能耗统计树读取、保存、排序和重置接口。
 */
public class EnergyTreeController {
    private final JdbcTemplate jdbc;
    private final TransactionTemplate tx;
    private final ObjectMapper json;
    @Value("${ENERGY_TREE_PASSWORD:}")
    private String energyTreePassword;
    private volatile boolean tableReady;

    public EnergyTreeController(JdbcTemplate jdbc, TransactionTemplate tx, ObjectMapper json) {
        this.jdbc = jdbc;
        this.tx = tx;
        this.json = json;
    }

    private synchronized void ensureDisplayTable() {
        if (!tableReady) {
            jdbc.execute("CREATE TABLE IF NOT EXISTS tree_node_display_info (node_id varchar(50) NOT NULL PRIMARY KEY, display_extra varchar(100) NOT NULL DEFAULT '') ENGINE=InnoDB DEFAULT CHARSET=utf8mb4");
            tableReady = true;
        }
    }

    @GetMapping
    /**
     * 读取当前能耗树配置并组装为前端使用的树形结构。
     */
    public Map<String, Object> getTree() {
        ensureDisplayTable();
        return snapshot();
    }

    /**
     * 生成树配置快照，用于变更前后校验。
     */
    private Map<String, Object> snapshot() {
        List<Map<String, Object>> current = jdbc.queryForList("SELECT n.id, n.parent_id AS parentId, n.name, n.extra_info AS meterKey, n.pds, n.sort_order AS sortOrder, n.node_type AS nodeType, COALESCE(d.display_extra,n.extra_info,'') AS displayExtra FROM tree_node_config n LEFT JOIN tree_node_display_info d ON d.node_id=n.id ORDER BY n.id");
        List<Map<String, Object>> initial = jdbc.queryForList("SELECT id, parent_id AS parentId, name, extra_info AS meterKey, pds, sort_order AS sortOrder, node_type AS nodeType FROM tree_node_config_init ORDER BY id");
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("current", current);
        out.put("initial", initial);
        out.put("version", digest(current));
        return out;
    }

    private String digest(Object value) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(json.writeValueAsBytes(value));
            StringBuilder out = new StringBuilder();
            for (byte b : bytes) out.append(String.format("%02x", b & 255));
            return out.toString();
        } catch (Exception ex) { throw new IllegalStateException(ex); }
    }

    /**
     * 校验树结构修改请求携带的身份验证口令。
     */
    private void authorize(Map<String, Object> body) {
        byte[] actual = String.valueOf(body.getOrDefault("password", "")).getBytes(StandardCharsets.UTF_8);
        byte[] expected = energyTreePassword == null
                ? new byte[0]
                : energyTreePassword.getBytes(StandardCharsets.UTF_8);
        if (expected.length == 0 || !MessageDigest.isEqual(expected, actual))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "密码不正确");
    }

    /**
     * 锁定树配置并校验修改前快照，避免并发覆盖他人更新。
     */
    private void lockAndCheck(Map<String, Object> body) {
        jdbc.queryForList("SELECT id FROM tree_node_config WHERE id='4' FOR UPDATE");
        if (!Objects.equals(body.get("version"), snapshot().get("version")))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "树结构已发生变化，请刷新后重新操作");
    }

    @PostMapping("/reset")
    /**
     * 将初始化树结构复制为当前生效配置。
     */
    public Map<String, Object> reset(@RequestBody Map<String, Object> body) {
        authorize(body);
        ensureDisplayTable();
        return tx.execute(status -> {
            lockAndCheck(body);
            jdbc.update("DELETE FROM tree_node_display_info");
            jdbc.update("DELETE FROM tree_node_config");
            jdbc.update("INSERT INTO tree_node_config (id,parent_id,name,extra_info,pds,sort_order,node_type,created_at,updated_at) SELECT id,parent_id,name,extra_info,pds,sort_order,node_type,created_at,updated_at FROM tree_node_config_init");
            return snapshot();
        });
    }

    @PostMapping("/save")
    /**
     * 验证并保存能耗树节点的新增、编辑、删除及顺序调整结果。
     */
    public Map<String, Object> save(@RequestBody Map<String, Object> body) {
        authorize(body);
        ensureDisplayTable();
        Object raw = body.get("nodes");
        if (!(raw instanceof List)) throw bad("缺少树节点");
        @SuppressWarnings("unchecked") List<Map<String, Object>> submitted = (List<Map<String, Object>>) raw;
        return tx.execute(status -> {
            lockAndCheck(body);
            Map<String, Object> oldSnapshot = snapshot();
            @SuppressWarnings("unchecked") List<Map<String, Object>> existing = (List<Map<String, Object>>) oldSnapshot.get("current");
            @SuppressWarnings("unchecked") List<Map<String, Object>> initial = (List<Map<String, Object>>) oldSnapshot.get("initial");
            Set<String> oldIds = descendants(existing, "4");
            Set<String> outside = new HashSet<>();
            for (Map<String, Object> n : existing) if (!oldIds.contains(str(n, "id"))) outside.add(str(n, "id"));
            Set<String> excluded = descendants(initial, "hash_3");
            Set<String> allowed = new HashSet<>();
            for (Map<String, Object> n : initial) {
                if (!str(n,"meterKey").isEmpty() && !excluded.contains(str(n,"id")))
                    allowed.add(str(n,"meterKey") + "\u0000" + str(n,"pds"));
            }
            // These two meters are present in the project's circuit tree and electricity
            // statistics, but are missing from the initial database tree.
            allowed.add("2-1AA10-8\u00002#配电室");
            allowed.add("2-4AA7-1\u00002#配电室");
            if (submitted.size() > 3000) throw bad("节点数量过多");
            Map<String, Map<String, Object>> byId = new HashMap<>();
            for (Map<String, Object> n : submitted) {
                String id = str(n,"id"), name = str(n,"name"), type = str(n,"nodeType");
                if (id.isEmpty() || id.length()>50 || outside.contains(id) || byId.put(id,n)!=null || name.trim().isEmpty() || name.length()>500 || !("leaf".equals(type)||"folder".equals(type))) throw bad("节点信息无效");
                if (str(n,"meterKey").length()>100 || str(n,"displayExtra").length()>100 || str(n,"pds").length()>50) throw bad("节点信息过长");
                if ("leaf".equals(type) && !allowed.contains(str(n,"meterKey") + "\u0000" + str(n,"pds"))) throw bad("电表不在允许的范围内");
                if ("folder".equals(type) && !str(n,"meterKey").isEmpty()) throw bad("目录不能绑定电表");
            }
            for (Map<String, Object> n : submitted) {
                Set<String> path = new HashSet<>();
                Map<String, Object> cursor = n;
                while (!"4".equals(str(cursor,"parentId"))) {
                    if (!path.add(str(cursor,"id"))) throw bad("目录存在循环");
                    cursor = byId.get(str(cursor,"parentId"));
                    if (cursor==null || !"folder".equals(str(cursor,"nodeType"))) throw bad("目录层级无效");
                }
                if ("4".equals(str(n,"parentId")) && !"folder".equals(str(n,"nodeType"))) throw bad("散表下只能添加分组");
            }
            for (String id : oldIds) { jdbc.update("DELETE FROM tree_node_display_info WHERE node_id=?", id); jdbc.update("DELETE FROM tree_node_config WHERE id=?", id); }
            for (Map<String, Object> n : submitted) {
                int order;
                try { order = Integer.parseInt(String.valueOf(n.get("sortOrder"))); } catch (Exception e) { throw bad("排序无效"); }
                jdbc.update("INSERT INTO tree_node_config(id,parent_id,name,extra_info,pds,sort_order,node_type) VALUES (?,?,?,?,?,?,?)",str(n,"id"),str(n,"parentId"),str(n,"name"),str(n,"meterKey"),str(n,"pds"),order,str(n,"nodeType"));
                if ("leaf".equals(str(n,"nodeType"))) jdbc.update("INSERT INTO tree_node_display_info(node_id,display_extra) VALUES (?,?)", str(n,"id"),str(n,"displayExtra"));
            }
            return snapshot();
        });
    }

    private static String str(Map<String, Object> n, String key) { Object v=n.get(key); return v==null?"":String.valueOf(v); }
    private static ResponseStatusException bad(String msg) { return new ResponseStatusException(HttpStatus.BAD_REQUEST,msg); }
    /**
     * 查找指定节点下的所有后代节点。
     */
    private static Set<String> descendants(List<Map<String, Object>> nodes, String root) {
        Set<String> ids = new HashSet<>();
        boolean changed;
        do { changed=false; for (Map<String, Object> n : nodes) if (!ids.contains(str(n,"id")) && (root.equals(str(n,"parentId")) || ids.contains(str(n,"parentId")))) { ids.add(str(n,"id")); changed=true; } } while (changed);
        return ids;
    }
}
