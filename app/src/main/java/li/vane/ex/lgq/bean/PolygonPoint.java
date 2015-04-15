package li.vane.ex.lgq.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Polygon")
public class PolygonPoint extends Model
{
    @Column(name = "Lat")
    public double lat;

    @Column(name = "Lng")
    public double lng;

    @Column(name = "LGQ")
    public LGQ lgq;

    public PolygonPoint() {
        super();
    }

    public PolygonPoint(double lat, double lng, LGQ lgq) {
        super();
        this.lat = lat;
        this.lng = lng;
        this.lgq = lgq;
    }


}