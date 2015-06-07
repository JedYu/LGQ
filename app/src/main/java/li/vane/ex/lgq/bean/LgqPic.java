package li.vane.ex.lgq.bean;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

@Table(name = "Pic")
public class LgqPic extends Model
{
    @Column(name = "Path")
    public String path;

    @Column(name = "LGQ")
    public LGQ lgq;

    public LgqPic() {
        super();
    }

    public LgqPic(String path, LGQ lgq) {
        super();
        this.path = path;
        this.lgq = lgq;
    }


}