package queryformer;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Created by Jack on 23.04.2017.
 */
@Data
public class Projection implements ISqlBlockParser {

    private List<Object> projections = new ArrayList<>();

    public Projection(Object...projections) {
        Stream.of(projections).forEach(i -> this.projections.add(i));
    }

    @Override
    public String parseSqlBlock(Object projection) {

        System.out.println("Projections: " + projection);
        return projection.toString();
    }


}
