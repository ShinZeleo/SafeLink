package com.example.safelink;

import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.TileService;
import android.annotation.SuppressLint;
import androidx.annotation.RequiresApi;

@RequiresApi(api = Build.VERSION_CODES.N)
public class QuickScanTileService extends TileService {

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Override
    public void onClick() {
        super.onClick();
        
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        
        if (Build.VERSION.SDK_INT >= 34) { // UPSIDE_DOWN_CAKE
            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getActivity(
                this,
                0,
                intent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
            );
            startActivityAndCollapse(pendingIntent);
        } else {
            // Fallback for older APIs
            startActivityAndCollapse(intent);
        }
    }
}
