﻿using System;
using UnityEngine;

namespace AdiveryUnity
{
    public class RewardedAd
    {
        private readonly AndroidJavaObject adObject;

        public event EventHandler OnAdLoaded;
        public event EventHandler OnAdRewarded;
        public event EventHandler<int> OnAdLoadFailed;
        public event EventHandler<int> OnAdShowFailed;
        public event EventHandler OnAdShown;
        public event EventHandler OnAdClicked;
        public event EventHandler OnAdClosed;

        public RewardedAd(string placementId)
        {
            if (!Adivery.IsAdiverySupported())
            {
                return;
            }

            adObject = new AndroidJavaObject("com.adivery.unity.Rewarded",
                Adivery.GetAndroidActivity(),
                placementId,
                new RewardedCallbackImpl(this));
        }

        public void LoadAd()
        {
            adObject?.Call("loadAd");
        }

        public bool IsLoaded()
        {
            return adObject?.Call<bool>("isLoaded") ?? false;
        }

        public void Show()
        {
            adObject?.Call("show");
        }

        internal class RewardedCallbackImpl : RewardedCallback
        {
            readonly RewardedAd ad;

            public RewardedCallbackImpl(RewardedAd ad)
            {
                this.ad = ad;
            }

            public override void onAdLoaded()
            {
                ad.OnAdLoaded?.Invoke(this, null);
            }

            public override void onAdRewarded()
            {
                ad.OnAdRewarded?.Invoke(this, null);
            }

            public override void onAdLoadFailed(int errorCode)
            {
                ad.OnAdLoadFailed?.Invoke(this, errorCode);
            }

            public override void onAdShown()
            {
                ad.OnAdShown?.Invoke(this, null);
            }

            public override void onAdShowFailed(int errorCode)
            {
                ad.OnAdShowFailed?.Invoke(this, errorCode);
            }

            public override void onAdClicked()
            {
                ad.OnAdClicked?.Invoke(this, null);
            }

            public override void onAdClosed()
            {
                ad.OnAdClosed?.Invoke(this, null);
            }
        }
    }
}
