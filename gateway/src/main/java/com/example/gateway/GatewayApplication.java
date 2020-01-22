package com.example.gateway;


import com.example.gateway.filters.*;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.EurekaInstanceConfig;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.appinfo.MyDataCenterInstanceConfig;
import com.netflix.appinfo.providers.EurekaConfigBasedInstanceInfoProvider;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.DefaultEurekaClientConfig;
import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.EurekaClient;
import com.netflix.discovery.EurekaClientConfig;
import com.netflix.zuul.filters.FilterRegistry;
import com.netflix.zuul.monitoring.MonitoringHelper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.HashMap;
import java.util.Properties;


@ServletComponentScan
@WebListener
@SpringBootApplication
public class GatewayApplication implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // mocks monitoring infrastructure as we don't need it for this simple app
        MonitoringHelper.initMocks();
        // initializes groovy filesystem poller
        final FilterRegistry r = FilterRegistry.instance();
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡfilter putㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        //r.put("pre1", new PreFilter1());
        r.put("Error", new ErrorFilter());
        r.put("post", new PostFilter());
        //r.put("pre2", new PreFilter2());
        r.put("pre", new PreFilter());
        r.put("Route", new RouteFilter());
        // initializes a few java filter examples
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡfilter put endㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }

    public static void main(String[] args) {

        //ystem.setProperties();

        Properties p = System.getProperties();
        p.put("archaius.configurationSource.additionalUrls","file:///C:/Users/DIR-P-0076/Desktop/zuul/gateway/target/classes/eureka-client.properties");
        p.put("archaius.fixedDelayPollingScheduler.delayMills","3000");

        HashMap map  = new HashMap();
        map.put("server.port",8088);

        SpringApplication sa = new SpringApplication(GatewayApplication.class);
        sa.setDefaultProperties(map);
        sa.run(args);
    }
}
