import java.util.ArrayList;

public class ReducerDemo {
    public static void main(String[] args) {
        ArrayList<Integer> numbers = new ArrayList<>();
        numbers.add(10);
        numbers.add(20);
        numbers.add(30);
        numbers.add(40);



//        numbers.stream().forEach(System.out::println);
//        numbers.stream().reduce(0,(previous,current) -> {
////            System.out.println(previous);
//            System.out.println(current);
//            System.out.println("---------------");
//
//
//            return current;
//        });

        System.out.println(numbers.stream().reduce(0,(p,c) -> p+c ));// 4 iter
        System.out.println(numbers.stream().reduce((p,c) -> p+c ));// 3 iter


        ArrayList<String> lines = new ArrayList<>();
        lines.add("{");
        lines.add("type:transfer,");
        lines.add("account:123-123");
        lines.add("}");

        System.out.println(lines.stream().reduce("",(p,c) -> p+c ));// 3 iter


    }
}
