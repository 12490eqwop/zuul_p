package com.example.gateway.filters;

import com.netflix.zuul.ZuulFilter;



public class PostFilter extends ZuulFilter {

    @Override
    public int filterOrder()
    {
        System.out.println("나의 filterOrder : 0");

        return 0;
    }

    @Override
    public String filterType()
    {
        return "post" ;
    }

    @Override
    public boolean shouldFilter()
    {
        return true;
    }

    @Override
    public Object run(){

        System.out.println("===Inside Post filter===");

        return null;
    }
}
