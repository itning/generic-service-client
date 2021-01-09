package top.itning.generic.service.jar.handle;

import lombok.Data;

import java.io.Serializable;

/**
 * @author itning
 * @since 2020/12/31 16:06
 */
@Data
public class Test<T> implements Serializable {
    private int age;
    private T name;
    private EnumTest enumTest;

    public void test(EnumTest enumTest){

    }

   /* public <A extends List<String> & Serializable & AutoCloseable> void test(Test<A> a, Test b, Test<?> c) {

    }

    public void test(Object d) {

    }

    public void test(Test<Test<String>> e) {

    }

    public void test(Test<String> f, String g) {

    }

    public <A extends Integer & Serializable & AutoCloseable> void test(Test<A> h, Integer i) {

    }

    public <A extends Integer & Serializable & AutoCloseable> void test(List<A> j) {

    }

    public <A> void test(List<A> k, String l) {

    }

    public <A> void test(A m, String n) {

    }

    public <A extends String> void test(A o, String p) {

    }*/
}
