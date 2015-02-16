package minechem.handler;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.Set;
import minechem.Compendium;
import minechem.Config;
import minechem.helper.FileHelper;
import minechem.helper.LogHelper;
import minechem.registry.MoleculeRegistry;
import org.apache.logging.log4j.Level;

public class MoleculeHandler
{
    public static void init()
    {
        String[] fileDestSource = new String[2];
        fileDestSource[0] = Compendium.Config.dataJsonPrefix + Compendium.Config.moleculesDataJson;
        fileDestSource[1] = Compendium.Config.configPrefix + Compendium.Config.dataJsonPrefix + Compendium.Config.moleculesDataJson;
        InputStream inputStream = FileHelper.getJsonFile(MoleculeHandler.class, fileDestSource, Config.useDefaultMolecules);
        readFromStream(inputStream);
        if (inputStream != null)
        {
            try
            {
                inputStream.close();
            } catch (IOException e)
            {
                LogHelper.exception("Cannot close stream!", e, Level.WARN);
            }
        }
    }

    private static void readFromStream(InputStream stream)
    {
        JsonReader jReader = new JsonReader(new InputStreamReader(stream));
        JsonParser parser = new JsonParser();

        Set<Map.Entry<String, JsonElement>> moleculeSet = parser.parse(jReader).getAsJsonObject().entrySet();
        int count = 0;
        for (Map.Entry<String, JsonElement> moleculeEntry : moleculeSet)
        {
            if (!moleculeEntry.getValue().isJsonObject())
            {
                continue;
            }
            JsonObject elementObject = moleculeEntry.getValue().getAsJsonObject();
            MoleculeRegistry.getInstance().registerMolecule(
                moleculeEntry.getKey(),
                elementObject.get("form").getAsString(),
                elementObject.get("formula").getAsString()
            );
            count++;
        }
        LogHelper.info("Total of " + count + " molecules registered");
    }
}