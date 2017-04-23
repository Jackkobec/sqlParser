package queryformer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jack on 23.04.2017.
 */
@Data
public class WhereCase implements ISqlBlockParser {

    private List<Object> conditions = new ArrayList<>();

    public WhereCase(Object... conditions) {
        Stream.of(conditions).forEach(i -> this.conditions.add(i));
    }

    @Override
    public String parseSqlBlock(Object block) {

        conditions.forEach(i -> System.out.printf("WhereCase: %s\n",  i.toString()));

        return conditions.toString();
    }
}
