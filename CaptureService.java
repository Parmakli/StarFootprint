package tk.parmclee.starfootprint;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.preference.PreferenceManager;

import java.io.File;

public class CaptureService extends IntentService {

    public CaptureService() {
        super("StarFootprintService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        String tempFilePath = intent.getStringExtra("tempFilePath");
        String filePath = intent.getStringExtra("filePath");
        File file = new File(filePath);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap previous = BitmapFactory.decodeFile(filePath, options);
        Bitmap current = BitmapFactory.decodeFile(tempFilePath, options);

        Bitmap result = retainWhitePixels(previous, current);
        CaptureFragment.saveBitmap(result, file);

        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        if (!preferences.getBoolean("interim", false)) (new File(tempFilePath)).delete();
        AlarmReceiver.completeWakefulIntent(intent);
        AlarmReceiver.completeWakefulIntent(AlarmReceiver.sActivityIntent);
    }

    private Bitmap retainWhitePixels(Bitmap previous, Bitmap current) {
        int width = previous.getWidth();
        int height = previous.getHeight();
        Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int pixel = previous.getPixel(i, j);
                int newPixel = current.getPixel(i, j);
                int red = Color.red(pixel),
                        green = Color.green(pixel),
                        blue = Color.blue(pixel);
                int newRed = Color.red(newPixel),
                        newGreen = Color.green(newPixel),
                        newBlue = Color.blue(newPixel);
                if (red <= newRed && green <= newGreen && blue <= newBlue) { // previous is darker
                    result.setPixel(i, j, newPixel);
                } else result.setPixel(i, j, pixel);
            }
        }
        return result;
    }
}
