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

    @Column(name = "Status")
    public String status;

    @Column(name = "PlanYear")
    public String planYear;

    @Column(name = "BeginYear")
    public String beginYear;

    @Column(name = "EndnYear")
    public String endYear;

    @Column(name = "IdentifiedYear")
    public String identifiedYear;

    public List<PolygonPoint> polygon() {
        return getMany(PolygonPoint.class, "LGQ");
    }

    public List<LgqPic> pics() {
        return getMany(LgqPic.class, "LGQ");
    }
}

