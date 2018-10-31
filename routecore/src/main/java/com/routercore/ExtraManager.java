package com.routercore;

import android.app.Activity;
import android.util.LruCache;
import com.routercore.template.IExtra;

import java.lang.reflect.InvocationTargetException;

public class ExtraManager {
    public static final String SUFFIX_AUTOWIRED = "_Extra";
    private static ExtraManager instance;
    private LruCache<String, IExtra> classCache;

    public static ExtraManager getInstance() {
        if (instance == null) {
            synchronized (ExtraManager.class) {
                if (instance == null) {
                    instance = new ExtraManager();
                }
            }
        }
        return instance;
    }
    public ExtraManager() {
        classCache = new LruCache<>(64);
    }

    //注入
    public void loadExtras(Activity instance) {
        //查找对应Activity的缓存
        String className = instance.getClass().getName();
        IExtra iExtra = classCache.get(className);

        try {
            if (iExtra == null) {
                iExtra = (IExtra) Class.forName(instance.getClass().getName()
                        + SUFFIX_AUTOWIRED).getConstructor().newInstance();
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        iExtra.loadExtra(instance);
        classCache.put(className, iExtra);

    }

}
