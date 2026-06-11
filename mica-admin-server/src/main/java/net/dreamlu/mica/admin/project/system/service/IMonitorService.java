package net.dreamlu.mica.admin.project.system.service;

import java.util.Map;

/**
 * @author Zheng Jie
 * @date 2020-05-02
 */
public interface IMonitorService {

    /**
    * 查询数据分页
    * @return Map<String,Object>
    */
    Map<String,Object> getServers();
}
