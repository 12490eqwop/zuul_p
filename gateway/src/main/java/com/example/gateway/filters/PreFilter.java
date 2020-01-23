package com.example.gateway.filters;

import com.netflix.zuul.ZuulFilter;
import javax.servlet.http.HttpServletRequest;
import com.netflix.zuul.context.RequestContext;


public class PreFilter extends ZuulFilter {

    @Override
    public int filterOrder()
    {
        return 0;
    }

    @Override
    public String filterType()
    {
        return "pre" ;
    }

    @Override
    public boolean shouldFilter()
    {
        return true;
    }

    @Override
    public Object run(){

        System.out.println("===preFilter===");

        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();

        return null;
    }
}