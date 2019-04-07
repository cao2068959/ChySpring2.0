package chy.test;

import chy.spring.annotation.ChyAutowired;
import chy.spring.annotation.ChyService;

@ChyService
public class Haha {

    @ChyAutowired
    private Uii uii;

    public void xxx(){
        uii.uuud();
    }

}
