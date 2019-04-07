package chy.spring.mvc;

import chy.spring.annotation.ChyController;
import chy.spring.annotation.ChyRequestMapping;
import chy.spring.annotation.ChyRequestParam;
import chy.spring.context.ChyAppilicationContext;
import chy.spring.mvc.bean.HandleMapping;
import chy.spring.mvc.bean.HandlerAdapter;
import chy.spring.mvc.bean.ModelAndView;
import com.alibaba.fastjson.JSON;
import com.sun.org.apache.xpath.internal.operations.Mod;
import org.apache.commons.lang.StringUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DispatcherServlet extends HttpServlet {

    private ChyAppilicationContext context;

    private List<HandleMapping> handleMappings = new ArrayList<>();
    private Map<HandleMapping,HandlerAdapter> handlerAdapterMaps = new HashMap<>();



    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
        String config = servletConfig.getInitParameter("config");
        //初始化容器
        context = new ChyAppilicationContext(config);
        initStrategies(context);

    }

    private void initStrategies(ChyAppilicationContext context) {
        //spring 中的9大策略组件,这里只处理其中的三种

        //通过url映射对应的方法,这里需要先把所有的对应的URL缓存一下`
        initHandlerMappings(context);

        //请求参数转换器,这里要把参数的类型 按照顺序缓存一下
        initHandlerAdapters(context);

        //通过ViewResolvers实现动态模板的解析
        initViewResolvers(context);

    }


    private void initViewResolvers(ChyAppilicationContext context) {
    }

    private void initHandlerAdapters(ChyAppilicationContext context) {
        for (HandleMapping handleMapping : handleMappings) {
            Method method = handleMapping.getMethod();

            Map<String,Integer> paramMapping = new HashMap<>();

            Parameter[] parameters = method.getParameters();
            for (int i = 0; i < parameters.length; i++) {
                Parameter parameter = parameters[i];
                //处理打了注解的参数
                ChyRequestParam requestParam = parameter.getAnnotation(ChyRequestParam.class);
                if(requestParam!=null){
                    String requestParamValue = requestParam.value();
                    if(!StringUtils.isEmpty(requestParamValue)){
                        paramMapping.put(requestParamValue,i);
                    }

                }
                //没打注解的只处理request和respon;
                Class<?> parameterType = parameter.getType();
                if(parameterType == HttpServletRequest.class || parameterType == HttpServletResponse.class){
                    paramMapping.put(parameterType.getName(),i);
                }
            }

            HandlerAdapter handlerAdapter = new HandlerAdapter(paramMapping);
            this.handlerAdapterMaps.put(handleMapping,handlerAdapter);

        }


    }

    private void initHandlerMappings(ChyAppilicationContext context) {
        for (String beanDefinitionName : context.getBeanDefinitionNames()) {
            Object bean = context.getBean(beanDefinitionName);
            Class<?> controllerClassSub = bean.getClass();

            Class<?> controllerClass = controllerClassSub.getSuperclass();
            //没打上controller表示,不处理
            if(controllerClass.getAnnotation(ChyController.class)==null){continue;}

            String baseUrl = "";
            //如果在类上写了父级路径,则把父级路径拼上
            ChyRequestMapping controllerChyRequestMapping = controllerClass.getAnnotation(ChyRequestMapping.class);
            if(controllerChyRequestMapping!=null){
                baseUrl = baseUrl + "/"+controllerChyRequestMapping.value();
            }

            for (Method method : controllerClass.getDeclaredMethods()) {
                ChyRequestMapping chyRequestMapping = method.getAnnotation(ChyRequestMapping.class);
                if(chyRequestMapping == null){continue;}
                String value = chyRequestMapping.value();
                String url = baseUrl+"/"+value;
                url = url.replaceAll("/+","/");
                HandleMapping handleMapping = new HandleMapping(url,method,bean);
                handleMappings.add(handleMapping);
                System.out.println("Mapping: " + url + " , " + method);
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Content-type", "text/html;charset=UTF-8");
        response.setCharacterEncoding("utf-8");

        try {
            doDispatch(request,response);
        } catch (Exception e) {
            response.getWriter().write("500 请求错误");
            e.printStackTrace();
        }
    }

    /**
     * 真正处理请求的地方
     * @param request
     * @param response
     */
    private void doDispatch(HttpServletRequest request, HttpServletResponse response) throws Exception {
        HandleMapping handler = getHandler(request);
        if(handler == null){
            try {
                response.getWriter().write("404 请求地址未找到");
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        HandlerAdapter handlerAdapter = getHandlerAdapter(handler);

        ModelAndView modelAndView = modelAndView = handlerAdapter.handle(request,response,handler);

        processDispatchResult(request,response, modelAndView);


    }

    private void processDispatchResult(HttpServletRequest request, HttpServletResponse response,ModelAndView modelAndView) throws ServletException, IOException {
        if(modelAndView == null){
            return ;
        }
        if(ModelAndView.type_forward.equals(modelAndView.getType())){
            request.getRequestDispatcher(modelAndView.getViewName()).forward(request,response);
            return;
        }

        if(ModelAndView.type_json.equals(modelAndView.getType())){
            String data = JSON.toJSON(modelAndView.getData()).toString();
            response.setHeader("Content-type", "application/json;charset=UTF-8");
            response.getWriter().print(data);
            return;
        }

    }

    private HandlerAdapter getHandlerAdapter(HandleMapping handler) {
        HandlerAdapter handlerAdapter = this.handlerAdapterMaps.get(handler);
        return handlerAdapter;
    }


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req,resp);
    }


    public HandleMapping getHandler(HttpServletRequest request){
        String url = request.getRequestURI();
        String contextPath = request.getContextPath();
        url = url.replace(contextPath,"").replaceAll("/+","/");

        System.out.println("进入请求: "+url);
        for (HandleMapping handleMapping : handleMappings) {
            if(handleMapping.getPatter().equals(url)){
                return handleMapping;
            }
        }
        return null;
    }
}
