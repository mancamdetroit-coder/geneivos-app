package org.geneivos.app;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        ImageView logo     = findViewById(R.id.splashLogo);
        TextView tagline   = findViewById(R.id.splashTagline);
        ProgressBar spinner = findViewById(R.id.splashProgress);

        // Animate logo in
        ObjectAnimator logoFade = ObjectAnimator.ofFloat(logo, View.ALPHA, 0f, 1f);
        logoFade.setDuration(800);

        ObjectAnimator logoScale = ObjectAnimator.ofFloat(logo, View.SCALE_X, 0.8f, 1f);
        logoScale.setDuration(800);

        ObjectAnimator logoScaleY = ObjectAnimator.ofFloat(logo, View.SCALE_Y, 0.8f, 1f);
        logoScaleY.setDuration(800);

        AnimatorSet logoAnim = new AnimatorSet();
        logoAnim.playTogether(logoFade, logoScale, logoScaleY);
        logoAnim.start();

        // Animate tagline after logo
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator tagFade = ObjectAnimator.ofFloat(tagline, View.ALPHA, 0f, 1f);
            tagFade.setDuration(600);
            tagFade.start();
        }, 600);

        // Show spinner
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            ObjectAnimator spinFade = ObjectAnimator.ofFloat(spinner, View.ALPHA, 0f, 1f);
            spinFade.setDuration(400);
            spinFade.start();
        }, 1000);

        // Go to main after delay
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }, 2200);
    }
}
