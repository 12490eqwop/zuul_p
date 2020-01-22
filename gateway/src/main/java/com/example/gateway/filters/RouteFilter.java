package com.example.gateway.filters;

///로드밸런싱 연습해보기위해...


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
        System.out.println("ㅡㅡㅡInside Route Filterㅡㅡㅡㅡㅡㅡㅡ");
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        RestTemplate restTemplate = new RestTemplate();
        String[] str = new String[0];
        System.out.println("requestURL :" + request.getRequestURL());
        System.out.println("requestURI :" + request.getRequestURI());
       // System.out.println("defaultHost :"+defaultHost);
        System.out.println("Request Method : " + request.getMethod() + " Request URL : " + request.getRequestURL().toString());

        //서버리스트 얻는 부분
        ServerList<DiscoveryEnabledServer> list1 = new DiscoveryEnabledNIWSServerList("sampleservice1.mydomain.net");
        ServerList<DiscoveryEnabledServer> list2 = new DiscoveryEnabledNIWSServerList("sampleservice2.mydomain.net");

        List<DiscoveryEnabledServer> service1_List = list1.getInitialListOfServers();
        List<DiscoveryEnabledServer> service2_List = list2.getInitialListOfServers();

        // 20200120 유레카 서버 없을 때 연결시키기 부분
        List<String> service1_ListString = Arrays.asList(DSP.get().split(","));
        List<Server> service1_List_U = new ArrayList<>();

        System.out.println(service1_ListString.size());
        for(int i=0; i<service1_ListString.size();i++){

            System.out.println(service1_ListString.get(i));
            System.out.println(service1_ListString.get(i).split(":")[2]);
            service1_List_U.add(new Server("http:"+service1_ListString.get(i).split(":")[1],Integer.parseInt(service1_ListString.get(i).split(":")[2])));
        }




            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡvip주소 sampleservice1.mydomain.net 인 서비스 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

            for (int i = 0; i < service1_List.size(); i++) {
                System.out.println("service1.get(i).getPort() : " + service1_List.get(i).getPort());
            }

            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡvip주소 sampleservice2.mydomain.net 인 서비스 ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            for (int i = 0; i < service2_List.size(); i++) {
                System.out.println("service2.get(i).getPort() : " + service2_List.get(i).getPort());
            }


            //ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡloadBalancingㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ

            BaseLoadBalancer lb = new BaseLoadBalancer();
            LoadBalancer a = new LoadBalancer();
            Server server;

            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");

            if(service1_List.size()==0){
                for (int i = 0; i < service1_List_U.size(); i++) {
                    lb.addServer((Server) service1_List_U.get(i));
                }
            }else {
                a.GetServerList(service1_List, lb);
            }



            //a.GetServerList(service2_List, lb);

            System.out.println(lb);


//        System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡWeightedResponseTime 방식ㅡㅡㅡㅡㅡ");
//        server = a.Choose_WeightedResponseTime(lb);
//        System.out.println(server);

            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡRandom 방식ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            server = a.Choose_Random(lb);
            System.out.println(server);

            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡRoundRobin 방식ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            server = a.Choose_RoundRobin(lb);
            System.out.println(server);


        System.out.println(a.GetURL(server));
            System.out.println("ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ");
            System.out.println("ㅡㅡㅡㅡㅡㅡㅡ출력 화면 메세지ㅡㅡㅡㅡㅡㅡㅡ");
            System.out.println(restTemplate.getForObject(a.GetURL(server),String.class));


        return null;
    }
}
