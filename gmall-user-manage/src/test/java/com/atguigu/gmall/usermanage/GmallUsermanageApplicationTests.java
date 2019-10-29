package com.atguigu.gmall.usermanage;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GmallUsermanageApplicationTests {

    @Test
    public void contextLoads() throws IOException {
        List list = new ArrayList<>();
        List list2 = new LinkedList();

        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        String s = input.readLine();
        System.out.println(s);
    }

}
