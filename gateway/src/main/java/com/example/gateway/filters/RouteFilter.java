package com.example.gateway.filters;


import com.example.gateway.loadbalncer.LoadBalancer;


import com.netflix.client.ClientException;
import com.netflix.client.ClientFactory;
import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.*;
import com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList;


import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


import com.netflix.config.ConfigurationManager;
import com.netflix.niws.client.http.RestClient;
import com.netflix.loadbalancer.ZoneAwareLoadBalancer;
import com.netflix.client.http.HttpRequest;
import com.netflix.client.http.HttpResponse;



public class RouteFilter extends ZuulFilter {
    private static final String ZUUL_SERVICE_URL = "eureka-client.ribbon.listOfServers";
    private DynamicStringProperty DSP = DynamicPropertyFactory.getInstance().getStringProperty(ZUUL_SERVICE_URL, null);


    @Override
    public String filterType() {
        return "route";
    }

    @Override
    public int filterOrder() {
        return 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {


        // https://github.com/Netflix/ribbon
        System.out.println("===Inside Route Filter===");
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        RestTemplate restTemplate = new RestTemplate();
        String[] str = new String[0];


        System.out.println("requestURI :" + request.getRequestURI());
        System.out.println("Request Method : " + request.getMethod() + " Request URL : " + request.getRequestURL().toString());

        //유레카 서버 리스트에서 vip address가 같은 서비스들(같은 기능을 하는 서비스)끼리 list에 담음
        ServerList<DiscoveryEnabledServer> list1 = new DiscoveryEnabledNIWSServerList("sampleservice1.mydomain.net");
        ServerList<DiscoveryEnabledServer> list2 = new DiscoveryEnabledNIWSServerList("sampleservice2.mydomain.net");

        List<DiscoveryEnabledServer> service1_List = list1.getInitialListOfServers();
        List<DiscoveryEnabledServer> service2_List = list2.getInitialListOfServers();


        //properties에서 가져온  serverlist 담기위한 리스트  ,,,,, 아래 serverlist 참고하면 더 좋은 방법있음 RestClient 를 이용하는 방법
        List<String> service1_ListString = Arrays.asList(DSP.get().split(","));
        List<Server> service1_List_U = new ArrayList<>();


        //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡloadBalancingㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

        BaseLoadBalancer lb = new BaseLoadBalancer();
        LoadBalancer loadBalancer = new LoadBalancer();
        Server server = null;

        if (service1_List.size() == 0) { // eureka server에 client 정보들이 없을 때 properties의 내용을 가져옴
            for (int i = 0; i < service1_ListString.size(); i++) {

                System.out.println(service1_ListString.get(i).split(":")[2]);
                //serverList에 server(host , port) 넣어줌
                service1_List_U.add(new Server("http:" + service1_ListString.get(i).split(":")[1], Integer.parseInt(service1_ListString.get(i).split(":")[2])));

            }
            //System.out.println(service1_List_U);

            for (int i = 0; i < service1_List_U.size(); i++) {
                lb.addServer((Server) service1_List_U.get(i));
            }
        } else {// eureka server에 client registry가 있을 때

            //loadBalancer.GetServerList(service2_List, lb);
            loadBalancer.GetServerList(service1_List, lb);
        }

//이 방식은 만들어진 로드밸런서에 setRule을 통해 rule을 변경해준다는 느낌
//아래에서 사용할 방식은 로드밸런서를 만들 때 같이 rule을 추가시켜준다는 느낌으로 이해햐자.
//        System.out.println("====WeightedResponseTime 방식====");
//        server = loadBalancer.Choose_WeightedResponseTime(lb);
//        System.out.println(server);

        System.out.println("====Random 방식====");
        server = loadBalancer.Choose_Random(lb);
        System.out.println(server);

//        System.out.println("====RoundRobin 방식====");
//        server = loadBalancer.Choose_RoundRobin(lb);
//        System.out.println(server);


        System.out.println(loadBalancer.GetURL(server));
        System.out.println("====출력 화면 메세지====");
        System.out.println(restTemplate.getForObject(loadBalancer.GetURL(server), String.class));

//
        ///======01/28날짜=================================

        try {
            ConfigurationManager.loadPropertiesFromResources("eureka-client.properties");
        } catch (IOException e) {
            e.printStackTrace();
        }


        //RestClient 를 이용해 properties의 서버 리스트를 가져오는 방법.
        System.out.println(ConfigurationManager.getConfigInstance().getProperty("eureka-client.ribbon.listOfServers"));
            RestClient client = (RestClient) ClientFactory.getNamedClient("eureka-client");
            List<Server> serverList = client.getLoadBalancer().getAllServers();

        try {

            HttpRequest request_Client = HttpRequest.newBuilder().uri(new URI("/")).build();

            for (int i = 0; i < 20; i++)  {
                HttpResponse response = client.executeWithLoadBalancer(request_Client);
                System.out.println("Status code for " + response.getRequestedURI() + "  :" + response.getStatus());
            }
            ZoneAwareLoadBalancer zlb = (ZoneAwareLoadBalancer) client.getLoadBalancer();
            System.out.println(zlb.getLoadBalancerStats()+"\n");
        } catch (URISyntaxException | ClientException e) {
            e.printStackTrace();
        }
///////////RoundRobinRuleㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

        BaseLoadBalancer roundRobinLoadBalancer = LoadBalancerBuilder.newBuilder().withRule(new RoundRobinRule()).buildFixedServerListLoadBalancer(serverList);

        System.out.println("=====RoundRobinRule=====");
        for (int i = 0; i < serverList.size(); i++) {
            System.out.println(roundRobinLoadBalancer.choose(serverList.get(i)));
        }



/////////Best Available Ruleㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

        BaseLoadBalancer bestAvailableLoadBalancer = LoadBalancerBuilder.newBuilder().withRule(new BestAvailableRule()).buildFixedServerListLoadBalancer(serverList);

        System.out.println("=====bestAvailableLoadBalancer=====");
        for (int i = 0; i < serverList.size(); i++) {
            ServerStats stats = bestAvailableLoadBalancer.getLoadBalancerStats().getSingleServerStat(serverList.get(i));
            // Request Connection을 인위적으로 증가시킨다.
            for (int j = 0; j < 10 - i; j++) {
                stats.incrementActiveRequestsCount();
            }
            System.out.print("Stats : " + stats.toString());
        }

        // Active Connection이 가장 낮은 서버를 선택한다.
        System.out.println("\nChosen Server : " + bestAvailableLoadBalancer.choose(null));


        return null;
    }
}
