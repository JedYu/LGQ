package li.vane.ex.lgq.util;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Administrator on 2015/5/31 0031.
 */
public class AssetUtil
{

    public static void copyAssets(Context context, String assetDir, String dstDir)
    {
        AssetManager assetManager = context.getAssets();
        String[] files = null;
        try
        {
            files = assetManager.list(assetDir);
        }
        catch (IOException e)
        {
            Log.e("tag", "Failed to get asset file list.", e);
        }

        File mapDirFile = new File(dstDir);
        if (!mapDirFile.exists())
        {
            boolean b = mapDirFile.mkdirs();
            if (!b)
            {
                Log.w("111", "mkdir failed");
            }
        }

        for (String filename : files)
        {
            InputStream in = null;
            OutputStream out = null;
            try
            {
                in = assetManager.open(assetDir + File.separator + filename);
                File outFile = new File(dstDir, filename);


                Log.d("tag", "Copy asset file: " + filename + " to " + outFile.getAbsolutePath());

                if (outFile.exists())
                {
                    continue;
                }
                else
                {
                    outFile.createNewFile();
                }
                out = new FileOutputStream(outFile);
                copyFile(in, out);
            }
            catch (IOException e)
            {
                Log.e("tag", "Failed to copy asset file: " + filename, e);
            }
            finally
            {
                if (in != null)
                {
                    try
                    {
                        in.close();
                    }
                    catch (IOException e)
                    {
                        // NOOP
                    }
                }
                if (out != null)
                {
                    try
                    {
                        out.close();
                    }
                    catch (IOException e)
                    {
                        // NOOP
                    }
                }
            }
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException
    {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1)
        {
            out.write(buffer, 0, read);
        }
    }
}
