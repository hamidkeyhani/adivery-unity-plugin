package com.adivery.unity;

import android.app.Activity;
import android.os.Build;
import android.view.DisplayCutout;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.view.WindowInsets;
import android.widget.FrameLayout;
import com.adivery.sdk.Adivery;
import com.adivery.sdk.AdiveryBannerCallback;
import com.adivery.sdk.BannerType;

public class Banner {

  private static final int POSITION_TOP = 0;
  private static final int POSITION_BOTTOM = 1;

  private static final int TYPE_BANNER = 0;
  private static final int TYPE_LARGE_BANNER = 1;
  private static final int TYPE_MEDIUM_RECTANGLE = 2;

  private String placementId;
  private Activity activity;
  private BannerType bannerType;
  private int bannerGravity;
  private View adView;
  private BannerCallback callback;
  private boolean isHidden;
  private boolean isLoaded;

  public Banner(
      Activity activity,
      String placementId,
      int bannerType,
      int bannerPosition,
      BannerCallback callback) {
    this.activity = activity;
    this.placementId = placementId;
    this.bannerType = getBannerType(bannerType);
    this.bannerGravity = getBannerGravity(bannerPosition);
    this.callback = callback;
    isLoaded = false;
    isHidden = false;
  }

  private static int getBannerGravity(int bannerPosition) {
    switch (bannerPosition) {
      case POSITION_TOP:
        return Gravity.TOP;
      case POSITION_BOTTOM:
        return Gravity.BOTTOM;
      default:
        return Gravity.CENTER;
    }
  }

  public void loadAd() {
    Adivery.requestBannerAd(
        activity,
        placementId,
        bannerType,
        new AdiveryBannerCallback() {
          @Override
          public void onAdLoaded(View adView) {
            Banner.this.adView = adView;
            if (isHidden) {
              adView.setVisibility(View.GONE);
            }
            activity.addContentView(adView, getLayoutParams());
            isLoaded = true;
            new Thread(
                    new Runnable() {
                      @Override
                      public void run() {
                        callback.onAdLoaded();
                      }
                    })
                .start();
          }

          @Override
          public void onAdLoadFailed(final int errorCode) {
            new Thread(
                    new Runnable() {
                      @Override
                      public void run() {
                        callback.onAdLoadFailed(errorCode);
                      }
                    })
                .start();
          }

          @Override
          public void onAdShowFailed(final int errorCode) {
            new Thread(
                    new Runnable() {
                      @Override
                      public void run() {
                        callback.onAdShowFailed(errorCode);
                      }
                    })
                .start();
          }

          @Override
          public void onAdClicked() {
            new Thread(
                    new Runnable() {
                      @Override
                      public void run() {
                        callback.onAdClicked();
                      }
                    })
                .start();
          }
        });
  }

  private FrameLayout.LayoutParams getLayoutParams() {
    final FrameLayout.LayoutParams adParams =
        new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
    adParams.gravity = bannerGravity | Gravity.CENTER_HORIZONTAL;

    Insets insets = getSafeInsets();
    adParams.setMargins(insets.left, insets.top, insets.right, insets.bottom);

    return adParams;
  }

  private BannerType getBannerType(int bannerType) {
    switch (bannerType) {
      case TYPE_BANNER:
        return BannerType.BANNER;
      case TYPE_LARGE_BANNER:
        return BannerType.LARGE_BANNER;
      case TYPE_MEDIUM_RECTANGLE:
        return BannerType.MEDIUM_RECTANGLE;
      default:
        return null;
    }
  }

  private Insets getSafeInsets() {
    Insets insets = new Insets();

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
      return insets;
    }
    Window window = activity.getWindow();
    if (window == null) {
      return insets;
    }
    WindowInsets windowInsets = window.getDecorView().getRootWindowInsets();
    if (windowInsets == null) {
      return insets;
    }
    DisplayCutout displayCutout = windowInsets.getDisplayCutout();
    if (displayCutout == null) {
      return insets;
    }
    insets.top = displayCutout.getSafeInsetTop();
    insets.left = displayCutout.getSafeInsetLeft();
    insets.bottom = displayCutout.getSafeInsetBottom();
    insets.right = displayCutout.getSafeInsetRight();
    return insets;
  }

  public boolean isLoaded() {
    return isLoaded;
  }

  public void show() {
    activity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            isHidden = false;
            if (adView != null) {
              adView.setVisibility(View.VISIBLE);
            }
          }
        });
  }

  public void hide() {
    activity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            isHidden = true;
            if (adView != null) {
              adView.setVisibility(View.GONE);
            }
          }
        });
  }

  public void destroy() {
    activity.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            if (adView != null) {
              ViewParent parentView = adView.getParent();
              if (parentView instanceof ViewGroup) {
                ((ViewGroup) parentView).removeView(adView);
              }
            }
          }
        });
  }

  private static class Insets {
    int top = 0;
    int bottom = 0;
    int left = 0;
    int right = 0;
  }
}