package com.poluria.util.json;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

/**
 * @author poluria
 */
public class JsonUtilTest {
    @Test
    public void toJson() {
        Product foo = new Product(1L, "product1");
        String result = JsonUtil.toJson(foo);
        assertThat(result, is("{\"no\":1, \"name\":\"product1\"}"));
    }
}

class Product {
    private Long no;
    private String name;

    Product(Long no, String name) {
        this.no = no;
        this.name = name;
    }
}
