package com.ansimue.kajimbatsiko;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.btnContactDev).setOnClickListener(v -> {
            String versionName = "";
            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                versionName = "1.2.2";
            }

            String phoneNumber = "261342943802";
            String message = "Bonjour l'équipe Kajimbatsiko,\n\n" +
                    "[Rédigez votre message ici]\n\n" +
                    "---------------------------\n" +
                    "Informations de diagnostic (ne pas modifier) :\n" +
                    "- Application : Kajimbatsiko\n" +
                    "- Version : " + versionName + "\n" +
                    "- Version Android : " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n" +
                    "- Appareil : " + Build.MANUFACTURER + " " + Build.MODEL + "\n";
            
            try {
                String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + Uri.encode(message);
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception ex) {
                Toast.makeText(this, "Impossible de lancer WhatsApp.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
