package com.example.gateway;

import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.discovery.EurekaClient;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.inject.Singleton;


@Singleton//인스턴스 한번만 생성하기 위해 사용>> 계속 새로 만들면 자원 낭비
public class ServiceBase {

    private final ApplicationInfoManager applicationInfoManager;
    private final EurekaClient eurekaClient;
    private final DynamicPropertyFactory configInstance;

    @Inject//spring의 bean 객체를 주입하는 것 ServiceBase SB = new ServiceBase >>> @Inject  public ServiceBase SB;
    public ServiceBase(ApplicationInfoManager applicationInfoManager,
                       EurekaClient eurekaClient,
                       DynamicPropertyFactory configInstance) {
        this.applicationInfoManager = applicationInfoManager;
        this.eurekaClient = eurekaClient;
        this.configInstance = configInstance;
    }



    @PostConstruct//스프링에 의해 instance생성 후 @PostConstruct 이 적용된 메서드 호출
    public void start() {
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        System.out.println("Registering service to eureka with STARTING status");
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.STARTING);
        //applicationInfoManager의 instanceStatus를 STARTING로 해줌
        System.out.println("Simulating service initialization by sleeping for 2 seconds...");
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        //서비스 초기화 2초간 sleep 시킨다고는하지만......
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            // Nothing
        }

        // Now we change our status to UP
        System.out.println("Done sleeping, now changing status to UP");
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        //초기화 후 instanceStatus를 UP으로 바꿈
        applicationInfoManager.setInstanceStatus(InstanceInfo.InstanceStatus.UP);

        //waitForRegistrationWithEureka 함수에 ServiceBase의 eurekaClient값을 넣어줌
        //이 함수에서는 vipAddress의 값에 따라 유레카 서버에
        waitForRegistrationWithEureka(eurekaClient);
        System.out.println("Service started and ready to process requests..");
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");



        System.out.println("Simulating service doing work by sleeping for " + 5 + " seconds...");
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            // Nothing
        }
    }

/*    @PreDestroy //객체 제거 전에 해야할 작업 수행하기 위해 사용
    public void stop() {
        if (eurekaClient != null) {
            System.out.println("Shutting down server. Demo over.");
            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            eurekaClient.shutdown();
        }
    }*/


    private void waitForRegistrationWithEureka(EurekaClient eurekaClient) {
        // my vip address to listen on

        String vipAddress = configInstance.getStringProperty("eureka.vipAddress", "sampleservice1.mydomain.net").get();
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡvipAddressㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        System.out.println("vipAddress : " + vipAddress);
        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
        InstanceInfo nextServerInfo = null;
        while (nextServerInfo == null) {
            try {
                nextServerInfo = eurekaClient.getNextServerFromEureka(vipAddress, false);
            } catch (Throwable e) {
                System.out.println("Waiting ... verifying service registration with eureka ...");
                System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


}
