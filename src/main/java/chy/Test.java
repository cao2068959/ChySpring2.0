package chy;

import chy.spring.context.ChyAppilicationContext;
import chy.test.Haha;

import java.net.URISyntaxException;

public class Test {


    public static void main(String[] args) throws URISyntaxException {
        ChyAppilicationContext chyAppilicationContext = new ChyAppilicationContext("application.properties");
        Haha bean = chyAppilicationContext.getBean(Haha.class);
        bean.xxx();

    }
}
