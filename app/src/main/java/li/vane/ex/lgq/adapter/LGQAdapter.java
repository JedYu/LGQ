package li.vane.ex.lgq.adapter;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

import li.vane.ex.lgq.bean.LGQ;

/**
 * User: YuJian
 * Date: 2015-04-22
 * Time: 15:23
 */
public class LGQAdapter implements JsonSerializer<LGQ>
{
    @Override
    public JsonElement serialize(LGQ lgq, Type type, JsonSerializationContext jsc)
    {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("name", lgq.name);
        jsonObject.addProperty("city", lgq.city);
        jsonObject.addProperty("county", lgq.county);
        jsonObject.addProperty("level", lgq.level);
        jsonObject.addProperty("area", lgq.area);
        jsonObject.addProperty("crop", lgq.crop);
        jsonObject.addProperty("planYear", lgq.planYear);
        jsonObject.addProperty("identifiedYear", lgq.identifiedYear);
        return jsonObject;
    }
}
