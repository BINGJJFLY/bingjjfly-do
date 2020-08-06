package com.wjz.wrapper;

import org.junit.Test;

import java.util.Arrays;

public class IWrapperTest {

    @Test
    public void getWrapper() {
        String[] pns = IWrapper.getWrapper(User.class).getPropertyNames();
        System.out.println(Arrays.asList(pns));
    }
}

class User {
    public Integer id;
    public String name;
}
