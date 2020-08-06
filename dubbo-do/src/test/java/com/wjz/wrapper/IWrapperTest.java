package com.wjz.wrapper;

import org.junit.Test;

import java.util.Arrays;

public class IWrapperTest {

    @Test
    public void getWrapper() {
        IWrapper wrapper = IWrapper.getWrapper(User.class);
        String[] pns = wrapper.getPropertyNames();
        System.out.println(wrapper.getClass().getName());
        System.out.println(Arrays.asList(pns));
    }

    @Test
    public void getWrapper2() {
        IWrapper wrapper = IWrapper.getWrapper(User2.class);
        String[] pns = wrapper.getPropertyNames();
        System.out.println(wrapper.getClass().getName());
        System.out.println(Arrays.asList(pns));
    }

    private class User {
        public Integer id;
        public String name;
    }

    public class User2 {
        public Integer id;
        public String name;
    }
}
