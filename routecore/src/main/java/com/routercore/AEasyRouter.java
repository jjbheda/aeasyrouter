package com.routercore;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;

import com.model.RouteMeta;
import com.routercore.NoRouteFoundException.NoRouteFoundException;
import com.routercore.callback.NavigationCallback;
import com.routercore.template.IRouteGroup;
import com.routercore.template.IRouteRoot;
import com.routercore.template.IService;
import com.routercore.utils.ClassUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;


public class AEasyRouter {
    private static final String TAG = "AEasyRouter";
    private static final String ROUTE_ROOT_PACKAGE = "com.aeasyrouter.routes";
    private static final String SDK_NAME = "AEasyRouter";
    private static final String SEPARATOR = "_";
    private static final String SUFFIX_ROOT = "Root";

    private static AEasyRouter mInstance;
    private static Application mContext;
    private Handler mHandler;
    private AEasyRouter() {
        mHandler = new Handler(Looper.getMainLooper());
    }

    public static AEasyRouter getInstance() {
        if (mInstance == null) {
            synchronized (AEasyRouter.class) {
                if (mInstance == null) {
                    mInstance = new AEasyRouter();
                }
            }
        }
        return mInstance;
    }

    public static void init(Application application) {
        mContext = application;
        try {
            loadInfo();
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "初始化失败!", e);
        }
    }

    //分组表制作
    private static void loadInfo() throws PackageManager.NameNotFoundException,InterruptedException,ClassNotFoundException,
            NoSuchMethodException,IllegalAccessException, InvocationTargetException,InstantiationException {
        //获取所有apt生成的路由类的全类名（路由表）
        Set<String> routeMap = ClassUtils.getFileNameByPackageName(mContext, ROUTE_ROOT_PACKAGE);
        for (String className : routeMap) {
            if (className.startsWith(ROUTE_ROOT_PACKAGE + "." + SDK_NAME + SEPARATOR + SUFFIX_ROOT)) {
                //root 中注册的是分组信息， 将分组信息加入仓库中
                ((IRouteRoot)Class.forName(className).getConstructor().newInstance()).loadInto(Warehouse.groupIndex);
            }
        }

        for (Map.Entry<String, Class<? extends IRouteGroup>> stringClassEntry : Warehouse.groupIndex.entrySet()) {
            Log.d(TAG, "Root映射表[" + stringClassEntry.getKey() + ":" + stringClassEntry.getValue() + "]");
        }
    }

    public Postcard build(String path) {
        if (TextUtils.isEmpty(path)) {
            throw new RuntimeException("路由地址无效");
        } else {
            return build(path, extractGroup(path));
        }
    }

    public Postcard build(String path, String group) {
        if (TextUtils.isEmpty(path) || TextUtils.isEmpty(group)) {
            throw new RuntimeException("路由地址无效");
        } else {
            return new Postcard(path, group);
        }
    }

    //获取组别
    private String extractGroup(String path) {
        if (TextUtils.isEmpty(path) || !path.startsWith("/")) {
            throw new RuntimeException(path + ": 不能提取group");
        }
        String defaultGroup = path.substring(1,path.indexOf("/",1));
        if (TextUtils.isEmpty(defaultGroup)) {
            throw new RuntimeException(path + ":不能提取group");
        } else {
            return defaultGroup;
        }
    }

    protected Object navigation(Context context, final Postcard postcard, final int requestCode,
                                final NavigationCallback callback) {
        try {
            prepareCard(postcard);
        } catch (NoRouteFoundException e) {
            e.printStackTrace();
            if (callback != null) {
                callback.onLost(postcard);
            }

        } catch (Exception e) {
            return null;
        }

        if (callback != null) {
            callback.onFound(postcard);
        }

        switch (postcard.getType()) {
            case ACTIVITY:
                final Context currentContext = (context == null) ? mContext : context;
                final Intent intent = new Intent(currentContext, postcard.getDestination());
                intent.putExtras(postcard.getExtras());
                int flags = postcard.getFlags();
                if (flags != -1) {
                    intent.setFlags(flags);
                } else if (!(currentContext instanceof Activity)) {
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                }

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        //可能需要返回码
                        if (requestCode > 0) {
                            ActivityCompat.startActivityForResult((Activity) currentContext,intent, requestCode, postcard.getOptionsBundle());
                        } else {
                            ActivityCompat.startActivity(currentContext, intent, postcard.getOptionsBundle());
                        }

                        if ((postcard.getEnterAnim() != 0 || postcard.getExitAnim() != 0) && currentContext instanceof Activity) {
                            //老版本
                            ((Activity) currentContext).overridePendingTransition(postcard.getEnterAnim(),postcard.getExitAnim());
                        }

                        //跳转完成
                        if (callback != null) {
                            callback.onArrival(postcard);
                        }
                    }
                });

                break;
            case ISERVICE:
                return postcard.getService();

            default:
                    break;
        }
        return null;
    }

    //准备卡片
    private void prepareCard(Postcard postcard) {
        RouteMeta routeMeta = Warehouse.routes.get(postcard.getPath());
        if (routeMeta == null) {
            Class<? extends IRouteGroup> groupMeta = Warehouse.groupIndex.get(postcard.getGroup());
            if (null == groupMeta) {
                throw new NoRouteFoundException("没找到对应路由：分组=" + postcard.getGroup() +
                "路径 = " + postcard.getPath());
            }
            IRouteGroup iRouteGroupInstance;
            try {
                iRouteGroupInstance = groupMeta.getConstructor().newInstance();
            } catch (Exception e) {
               throw new RuntimeException("路由分组映射失败" + e);
            }
            iRouteGroupInstance.loadInto(Warehouse.routes);
            Warehouse.groupIndex.remove(postcard.getGroup());
            prepareCard(postcard);
        } else {
            //类 要跳转的Activity 或者IService 实现类
            postcard.setDestination(routeMeta.getDestination());
            postcard.setType(routeMeta.getType());
            switch (routeMeta.getType()) {
                case ISERVICE:
                    Class<?> destination = routeMeta.getDestination();
                    IService service = Warehouse.services.get(destination);
                    try {
                        if (service == null) {
                            //此处采用的是反射方式，可替换成插桩方式
                            service = (IService) destination.getConstructor().newInstance();
                            Warehouse.services.put(destination, service);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    postcard.setService(service);
                    break;
                 default:
                     break;
            }
        }

    }

    //注入
    public void inject(Activity instance) {
        ExtraManager.getInstance().loadExtras(instance);
    }

}
