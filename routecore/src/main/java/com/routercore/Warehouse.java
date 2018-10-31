package com.routercore;

import com.model.RouteMeta;
import com.routercore.template.IRouteGroup;
import com.routercore.template.IService;

import java.util.HashMap;
import java.util.Map;

public class Warehouse {
    //root 映射表 保存分组信息
    static Map<String, Class<? extends IRouteGroup>> groupIndex = new HashMap<>();

    //group 映射表 保存组中的所有数据
    static Map<String, RouteMeta> routes = new HashMap<>();

    static Map<Class, IService> services = new HashMap<>();
}
