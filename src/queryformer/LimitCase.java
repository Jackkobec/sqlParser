package queryformer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jack on 23.04.2017.
 */
@Data
public class LimitCase implements ISqlBlockParser {

    private List<Object> limits = new ArrayList<>();

    public LimitCase(Object... limits) {
        Stream.of(limits).forEach(i -> this.limits.add(i));
    }

    @Override
    public String parseSqlBlock(Object block) {

//        System.out.printf("FromCase: %s\n", joins.toString());
        // Correct view for printing. Without superfluous brackets.
        limits.forEach(i -> System.out.printf("LimitCase: %s\n", i.toString()));

        return limits.toString();
    }
}
