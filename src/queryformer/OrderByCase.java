package queryformer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jack on 23.04.2017.
 */
@Data
public class OrderByCase implements ISqlBlockParser {

    private List<Object> fields = new ArrayList<>();

    public OrderByCase(Object... fields) {
        Stream.of(fields).forEach(i -> this.fields.add(i));
    }

    @Override
    public String parseSqlBlock(Object block) {

//        System.out.printf("FromCase: %s\n", joins.toString());
        // Correct view for printing. Without superfluous brackets.
        fields.forEach(i -> System.out.printf("OrderByCase: %s\n", i.toString()));

        return fields.toString();
    }
}
