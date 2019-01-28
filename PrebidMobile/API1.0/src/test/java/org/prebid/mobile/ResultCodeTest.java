package org.prebid.mobile;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.mopub.mobileads.MoPubView;

import junit.framework.Assert;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.prebid.mobile.testutils.MockPrebidServerResponses;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;
import org.robolectric.shadows.ShadowNetworkInfo;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class ResultCodeTest extends BaseSetup {
    @Test
    public void testSuccessForMoPub() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            fetcher.enableTestMode();
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            assertEquals("hb_pb:0.50,hb_env:mobile-app,hb_pb_appnexus:0.50,hb_size:300x250,hb_bidder_appnexus:appnexus,hb_bidder:appnexus,hb_cache_id:df4aba04-5e69-44b8-8608-058ab21600b8,hb_env_appnexus:mobile-app,hb_creative_loadtype:html,hb_size_appnexus:300x250,hb_cache_id_appnexus:df4aba04-5e69-44b8-8608-058ab21600b8,", testView.getKeywords());
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testSuccessForDFP() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            PublisherAdRequest testRequest = new PublisherAdRequest.Builder().build();
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testRequest, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            fetcher.enableTestMode();
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.SUCCESS);
            Bundle bundle = testRequest.getCustomTargeting();
            Assert.assertEquals(11, bundle.size());
            Assert.assertTrue(bundle.containsKey("hb_pb"));
            Assert.assertEquals("0.50", bundle.get("hb_pb"));
            Assert.assertTrue(bundle.containsKey("hb_bidder"));
            Assert.assertEquals("appnexus", bundle.get("hb_bidder"));
            Assert.assertTrue(bundle.containsKey("hb_bidder_appnexus"));
            Assert.assertEquals("appnexus", bundle.get("hb_bidder_appnexus"));
            Assert.assertTrue(bundle.containsKey("hb_cache_id"));
            Assert.assertEquals("df4aba04-5e69-44b8-8608-058ab21600b8", bundle.get("hb_cache_id"));
            Assert.assertTrue(bundle.containsKey("hb_cache_id_appnexus"));
            Assert.assertEquals("df4aba04-5e69-44b8-8608-058ab21600b8", bundle.get("hb_cache_id_appnexus"));
            Assert.assertTrue(bundle.containsKey("hb_creative_loadtype"));
            Assert.assertEquals("html", bundle.get("hb_creative_loadtype"));
            Assert.assertTrue(bundle.containsKey("hb_env"));
            Assert.assertEquals("mobile-app", bundle.get("hb_env"));
            Assert.assertTrue(bundle.containsKey("hb_env_appnexus"));
            Assert.assertEquals("mobile-app", bundle.get("hb_env_appnexus"));
            Assert.assertTrue(bundle.containsKey("hb_pb_appnexus"));
            Assert.assertEquals("0.50", bundle.get("hb_pb_appnexus"));
            Assert.assertTrue(bundle.containsKey("hb_size"));
            Assert.assertEquals("300x250", bundle.get("hb_size"));
            Assert.assertTrue(bundle.containsKey("hb_size_appnexus"));
            Assert.assertEquals("300x250", bundle.get("hb_size_appnexus"));

        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testNetworkError() {
        PrebidMobile.setHost(Host.APPNEXUS);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        PrebidMobile.setAccountId("123456");
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        ShadowNetworkInfo shadowOfActiveNetworkInfo = shadowOf(connectivityManager.getActiveNetworkInfo());
        shadowOfActiveNetworkInfo.setConnectionStatus(false);
        BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.NETWORK_ERROR);
    }

    @Test
    public void testTimeOut() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.oneBidFromAppNexus()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            PrebidMobile.timeoutMillis = 30;
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.TIME_OUT);
            assertEquals(null, testView.getKeywords());
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testNoBids() throws Exception {
        if (successfulMockServerStarted) {
            HttpUrl httpUrl = server.url("/");
            Host.CUSTOM.setHostUrl(httpUrl.toString());
            PrebidMobile.setHost(Host.CUSTOM);
            PrebidMobile.setApplicationContext(activity.getApplicationContext());
            PrebidMobile.setAccountId("123456");
            server.enqueue(new MockResponse().setResponseCode(200).setBody(MockPrebidServerResponses.noBid()));
            BannerAdUnit adUnit = new BannerAdUnit("123456", 300, 250);
            MoPubView testView = new MoPubView(activity);
            OnCompleteListener mockListener = mock(OnCompleteListener.class);
            adUnit.fetchDemand(testView, mockListener);
            DemandFetcher fetcher = (DemandFetcher) FieldUtils.readField(adUnit, "fetcher", true);
            fetcher.enableTestMode();
            ShadowLooper fetcherLooper = shadowOf(fetcher.getHandler().getLooper());
            fetcherLooper.runOneTask();
            ShadowLooper demandLooper = shadowOf(fetcher.getDemandHandler().getLooper());
            demandLooper.runOneTask();
            Robolectric.flushBackgroundThreadScheduler();
            Robolectric.flushForegroundThreadScheduler();
            verify(mockListener).onComplete(ResultCode.NO_BIDS);
            assertEquals(null, testView.getKeywords());
        } else {
            assertTrue("Mock server not started", false);
        }
    }

    @Test
    public void testInvalidAccountId() throws Exception {
        PrebidMobile.setAccountId("");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_ACCOUNT_ID);
    }

    @Test
    public void testInvalidConfigId() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_CONFIG_ID);
    }

    @Test
    public void testInvalidHostUrl() throws Exception {
        PrebidMobile.setAccountId("123456");
        Host.CUSTOM.setHostUrl("");
        PrebidMobile.setHost(Host.CUSTOM);
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_HOST_URL);
    }

    @Test
    public void testDoNotSupportMultipleSizesForMoPubBanner() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        MoPubView testView = new MoPubView(activity);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        adUnit.fetchDemand(testView, mockListener);
        verify(mockListener).onComplete(ResultCode.INVALID_SIZE);
    }

    @Test
    public void testSupportMultipleSizesForDFPBanner() throws Exception {
        PrebidMobile.setAccountId("123456");
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        OnCompleteListener mockListener = mock(OnCompleteListener.class);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        adUnit.fetchDemand(builder.build(), mockListener);
        verify(mockListener, never()).onComplete(ResultCode.INVALID_SIZE);
    }
}
