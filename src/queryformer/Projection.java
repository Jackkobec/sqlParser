package queryformer;

/**
 * Created by Jack on 23.04.2017.
 */
public class Projection implements ISqlBlockParser {

    private Object projection;

    public Projection(Object projection) {
        this.projection = projection;
    }

    public Object getProjection() {
        return projection;
    }

    public void setProjection(Object projection) {
        this.projection = projection;
    }

    @Override
    public String parseSqlBlock(Object block) {

        System.out.println("Projection: " + block);
        return block.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Projection that = (Projection) o;

        return projection != null ? projection.equals(that.projection) : that.projection == null;
    }

    @Override
    public int hashCode() {
        return projection != null ? projection.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Projection{" +
                "projection=" + projection +
                '}';
    }
}
