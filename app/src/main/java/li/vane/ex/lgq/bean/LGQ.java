package li.vane.ex.lgq.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;

import java.util.List;

public class LGQ extends Model
{
    @Column(name = "City")
    public String city;

    @Column(name = "County")
    public String county;

    @Column(name = "Name")
    public String name;

    @Column(name = "Level")
    public String level;

    @Column(name = "Area")
    public double area;

    @Column(name = "Crop")
    public String crop;

    @Column(name = "PlanYear")
    public String planYear;

    @Column(name = "IdentifiedYear")
    public String identifiedYear;

    public List<PolygonPoint> polygon() {
        return getMany(PolygonPoint.class, "LGQ");
    }
}

