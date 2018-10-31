package com.routercore.template;

import com.model.RouteMeta;

import java.util.Map;

public interface IRouteGroup {
    void loadInto(Map<String, RouteMeta> map);
}
