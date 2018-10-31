package com.module1;

import com.aeasyannotation.Route;

import com.baseprovider.BaseProvider;
@Route(path = "/module1/provider")
public class Module1ProviderImpl implements BaseProvider {
    @Override
    public int add(int a, int b) {
        return a + b;
    }
}
