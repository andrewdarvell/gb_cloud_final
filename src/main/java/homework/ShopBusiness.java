package homework;

import java.util.*;
import java.util.stream.Collectors;

public class ShopBusiness {

    public static void main(String[] args) {
        Map<Integer, Set<Integer>> vendorsShops = new HashMap<>();
        vendorsShops.put(1, new HashSet<>(Arrays.asList(1,2,3,4)));
        vendorsShops.put(2, new HashSet<>(Arrays.asList(5,6)));

        Map<Integer, Integer> shopBusiness = new HashMap<>();
        shopBusiness.put(1, 101);
        shopBusiness.put(2, 102);
        shopBusiness.put(3, 103);
        shopBusiness.put(4, 104);
        shopBusiness.put(5, 105);
        shopBusiness.put(6, 106);

        Map<Integer, Set<Integer>> businessMap = getBusinessMap(vendorsShops, shopBusiness);
        businessMap.entrySet().stream()
                .forEach(e -> {
                    System.out.print(e.getKey());
                    System.out.print(" : ");
                    System.out.println(e.getValue().stream().map(String::valueOf).collect(Collectors.joining(",")));
                });
    }

    static Map<Integer, Set<Integer>> getBusinessMap(
            Map<Integer, Set<Integer>> vendorsShops,
            Map<Integer, Integer> shopBusiness
    ) {
        return vendorsShops.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().stream()
                                .map(shopBusiness::get)
                                .filter(Objects::nonNull)
                                .collect(Collectors.toSet())
                ));
    }
}
