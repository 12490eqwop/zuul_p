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

    private static ApplicationInfoManager applicationInfoManager; //com.netflix.appinfo
    private static EurekaClient eurekaClient;//com.netflix.discovery.EurekaClient

    public static synchronized ApplicationInfoManager initializeApplicationInfoManager(EurekaInstanceConfig instanceConfig) {
        if (applicationInfoManager == null) {
            //applicationInfoManager가 없으면 EurekaConfigBasedInstanceInfoProvider 객체를 생성해 instanceInfo를 만들고
            //ApplicationInfoManager객체 생성후 instanceConfig, instanceInfo 값을 넣어 applicationInfoManager에 저장 후   applicationInfoManager리턴
            InstanceInfo instanceInfo = new EurekaConfigBasedInstanceInfoProvider(instanceConfig).get();
            applicationInfoManager = new ApplicationInfoManager(instanceConfig, instanceInfo);
        }
        return applicationInfoManager;
    }


    //eureka Client 식별 해주는 함수 .
    private static synchronized EurekaClient initializeEurekaClient(ApplicationInfoManager applicationInfoManager, EurekaClientConfig clientConfig) {
        if (eurekaClient == null) {
            //eurekaClient 가 없으면 DiscoveryClient객체를 새로 생성해줘서 넣어주고 리턴
            //위의 initializeApplicationInfoManager에서 생성한   applicationInfoManager 값과 com.netflix.discovery
            // 의 clientConfig값을 넣어   DiscoveryClient 객체 생성
            eurekaClient = new DiscoveryClient(applicationInfoManager, clientConfig);
        }

        return eurekaClient;
    }

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
        p.put("archaius.configurationSource.additionalUrls","file:///C:\\Users\\DIR-P-0076\\Desktop\\zuul\\gateway\\target\\classes\\eureka-client.properties");
//        p.put("archaius.configurationSource.additionalUrls","file:///../../../classes\\eureka-client.properties");

        p.put("archaius.fixedDelayPollingScheduler.delayMills","3000");



        HashMap map  = new HashMap();
        map.put("server.port",8088);

        SpringApplication sa = new SpringApplication(GatewayApplication.class);
        sa.setDefaultProperties(map);
        sa.run(args);

        DynamicPropertyFactory configInstance = com.netflix.config.DynamicPropertyFactory.getInstance();
        ApplicationInfoManager applicationInfoManager = initializeApplicationInfoManager(new MyDataCenterInstanceConfig());
//        EurekaClient eurekaClient = initializeEurekaClient(applicationInfoManager, new DefaultEurekaClientConfig()); //잠시 유레카 서버 없을떄 로그확인 편하게 하기위해 주석처리

//        ServiceBase serviceBase = new ServiceBase(applicationInfoManager, eurekaClient, configInstance);
//        try {
//            serviceBase.start();
//        } finally {
//            // the stop calls shutdown on eurekaClient
//            //serviceBase.stop();
//        }
    }
}
