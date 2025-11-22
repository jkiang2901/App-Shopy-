package com.example.banhangapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.banhangapp.utils.AnimationHelper;
import com.example.banhangapp.utils.SharedPreferencesHelper;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.card.MaterialCardView;

public class SellerDashboardActivity extends AppCompatActivity {
    private MaterialCardView cardCustomers, cardInventory, cardPromotions, 
                            cardStatistics, cardTrackSales, cardLogout;
    private SharedPreferencesHelper prefsHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_dashboard);

        prefsHelper = new SharedPreferencesHelper(this);

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setHomeButtonEnabled(false);
        }

        // Initialize card views
        cardCustomers = findViewById(R.id.cardCustomers);
        cardInventory = findViewById(R.id.cardInventory);
        cardPromotions = findViewById(R.id.cardPromotions);
        cardStatistics = findViewById(R.id.cardStatistics);
        cardTrackSales = findViewById(R.id.cardTrackSales);
        cardLogout = findViewById(R.id.cardLogout);

        // Set click listeners with animations
        if (cardCustomers != null) {
            cardCustomers.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                startActivity(new Intent(this, SellerManageCustomersActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            });
        }
        if (cardInventory != null) {
            cardInventory.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                startActivity(new Intent(this, SellerManageInventoryActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            });
        }
        if (cardPromotions != null) {
            cardPromotions.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                startActivity(new Intent(this, SellerManagePromotionsActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            });
        }
        if (cardStatistics != null) {
            cardStatistics.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                startActivity(new Intent(this, SellerStatisticsActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            });
        }
        if (cardTrackSales != null) {
            cardTrackSales.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                startActivity(new Intent(this, SellerTrackSalesActivity.class));
                overridePendingTransition(R.anim.slide_in_up, R.anim.fade_out);
            });
        }
        if (cardLogout != null) {
            cardLogout.setOnClickListener(v -> {
                AnimationHelper.pulse(v);
                prefsHelper.clear();
                startActivity(new Intent(this, LoginActivity.class));
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
            });
        }

        // Animate cards entrance
        animateCardsEntrance();
    }

    private void animateCardsEntrance() {
        if (cardCustomers != null) {
            cardCustomers.postDelayed(() -> AnimationHelper.scaleIn(cardCustomers, 400), 100);
        }
        if (cardInventory != null) {
            cardInventory.postDelayed(() -> AnimationHelper.scaleIn(cardInventory, 400), 200);
        }
        if (cardPromotions != null) {
            cardPromotions.postDelayed(() -> AnimationHelper.scaleIn(cardPromotions, 400), 300);
        }
        if (cardStatistics != null) {
            cardStatistics.postDelayed(() -> AnimationHelper.scaleIn(cardStatistics, 400), 400);
        }
        if (cardTrackSales != null) {
            cardTrackSales.postDelayed(() -> AnimationHelper.scaleIn(cardTrackSales, 400), 500);
        }
        if (cardLogout != null) {
            cardLogout.postDelayed(() -> AnimationHelper.scaleIn(cardLogout, 400), 600);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

