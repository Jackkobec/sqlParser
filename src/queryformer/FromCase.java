package queryformer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jack on 23.04.2017.
 */

public class FromCase implements ISqlBlockParser {

    private List<Object> joins = new ArrayList<>();

    public FromCase(Object...joins) {
        Stream.of(joins).forEach(i -> this.joins.add(i));
    }

    public List<Object> getJoins() {
        return joins;
    }

    @Override
    public String parseSqlBlock(Object block) {

//        System.out.printf("FromCase: %s\n", joins.toString());
        // Correct view for printing. Without superfluous brackets.
        joins.forEach(i -> System.out.printf("FromCase: %s\n",  i.toString()));

        return joins.toString();
    }

    @Override
    public String toString() {
        return "FromCase: " + joins;
    }
}
