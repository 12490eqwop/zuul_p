package com.example.gateway.filters;



import com.example.gateway.loadbalncer.LoadBalancer;


import com.netflix.config.DynamicPropertyFactory;
import com.netflix.config.DynamicStringProperty;
import com.netflix.loadbalancer.BaseLoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ServerList;
import com.netflix.niws.loadbalancer.DiscoveryEnabledNIWSServerList;


import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.constants.ZuulConstants;
import com.netflix.zuul.context.RequestContext;
import org.springframework.web.client.RestTemplate;

import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;



public class RouteFilter extends ZuulFilter {
    private static final String ZUUL_SERVICE_URL="zuul.routes.service1.url";
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
        ServerList<DiscoveryEnabledServer> list1 = new DiscoveryEnabledNIWSServerList("eureka.mydomain.net");
        ServerList<DiscoveryEnabledServer> list2 = new DiscoveryEnabledNIWSServerList("sampleservice2.mydomain.net");

        System.out.println("@@@@@"+list1);

        System.out.println("@@@@@"+list1.getInitialListOfServers().size());

        List<DiscoveryEnabledServer> service1_List = list1.getInitialListOfServers();
        List<DiscoveryEnabledServer> service2_List = list2.getInitialListOfServers();

        System.out.println("ddddddd"+service1_List);
        System.out.println("!@!@!!"+service1_List.size());

        //properties에서 가져온  serverlist 담기위한 리스트
        List<String> service1_ListString = Arrays.asList(DSP.get().split(","));
        System.out.println("여기까지는?");
        List<Server> service1_List_U = new ArrayList<>();



            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡloadBalancingㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

            BaseLoadBalancer lb = new BaseLoadBalancer();
            LoadBalancer loadBalancer = new LoadBalancer();
            Server server;

            if(service1_List.size()==0){
                System.out.println("여기는 유레카서버리스트없을때");
                System.out.println(service1_ListString.size());
                for(int i=0; i<service1_ListString.size();i++){

                    System.out.println(service1_ListString.get(i));
                    System.out.println(service1_ListString.get(i).split(":")[2]);
                    //serverList에 server(host , port) 넣어줌
                    service1_List_U.add(new Server("http:"+service1_ListString.get(i).split(":")[1],Integer.parseInt(service1_ListString.get(i).split(":")[2])));
                }

                for (int i = 0; i < service1_List_U.size(); i++) {
                    lb.addServer((Server) service1_List_U.get(i));
                }
            }else {
                System.out.println("유레카서버리스트있을때 여긴데 왜안옴;");
                loadBalancer.GetServerList(service1_List, lb);
            }

        loadBalancer.GetServerList(service1_List, lb);

            //loadBalancer.GetServerList(service2_List, lb);


//        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡWeightedResponseTime 방식ㅡㅡㅡㅡㅡ");
//        server = loadBalancer.Choose_WeightedResponseTime(lb);
//        System.out.println(server);

            System.out.println("====Random 방식====");
            server = loadBalancer.Choose_Random(lb);
            System.out.println(server);


//            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡRoundRobin 방식ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
//            server = a.Choose_RoundRobin(lb);
//            System.out.println(server);


        System.out.println(loadBalancer.GetURL(server));
            System.out.println("====출력 화면 메세지====");
            System.out.println(restTemplate.getForObject(loadBalancer.GetURL(server),String.class));


        return null;
    }
}
