package desutine.kismet.target;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.world.storage.loot.LootEntry;
import net.minecraft.world.storage.loot.LootEntryTable;

import java.lang.reflect.Type;

public class LootEntrySerializerFix extends LootEntry.Serializer {
    @Override
    public JsonElement serialize(LootEntry entry, Type type, JsonSerializationContext context) {
        final JsonObject original = (JsonObject) super.serialize(entry, type, context);
        if (entry instanceof LootEntryTable) {
            original.remove("type");
            original.addProperty("type", "loot_entry");
        }
        return original;
    }
}
