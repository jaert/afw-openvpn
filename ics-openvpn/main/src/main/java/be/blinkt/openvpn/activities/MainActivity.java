/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package be.blinkt.openvpn.activities;

import android.annotation.TargetApi;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.support.v4n.view.ViewPager;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import be.blinkt.openvpn.R;
import be.blinkt.openvpn.VpnProfile;
import be.blinkt.openvpn.core.AppConfiguration;
import be.blinkt.openvpn.core.ConfigParser;
import be.blinkt.openvpn.core.ProfileManager;
import be.blinkt.openvpn.fragments.AboutFragment;
import be.blinkt.openvpn.fragments.FaqFragment;
import be.blinkt.openvpn.fragments.GeneralSettings;
import be.blinkt.openvpn.fragments.LogFragment;
import be.blinkt.openvpn.fragments.SendDumpFragment;
import be.blinkt.openvpn.fragments.VPNProfileList;
import be.blinkt.openvpn.views.ScreenSlidePagerAdapter;
import be.blinkt.openvpn.views.SlidingTabLayout;
import be.blinkt.openvpn.views.TabBarView;


public class MainActivity extends BaseActivity {

    private ViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;
    private SlidingTabLayout mSlidingTabLayout;

    protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);


        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getFragmentManager(), this);

        /* Toolbar and slider should have the same elevation */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            disableToolbarElevation();
        }



        mPagerAdapter.addTab(R.string.vpn_list_title, VPNProfileList.class);

        mPagerAdapter.addTab(R.string.generalsettings, GeneralSettings.class);
        mPagerAdapter.addTab(R.string.faq, FaqFragment.class);

        if(SendDumpFragment.getLastestDump(this)!=null) {
            mPagerAdapter.addTab(R.string.crashdump, SendDumpFragment.class);
        }

        if (isDirectToTV())
            mPagerAdapter.addTab(R.string.openvpn_log, LogFragment.class);

        mPagerAdapter.addTab(R.string.about, AboutFragment.class);
        mPager.setAdapter(mPagerAdapter);

        TabBarView tabs = (TabBarView) findViewById(R.id.sliding_tabs);
        tabs.setViewPager(mPager);

       // requestDozeDisable();
	}

    private AppConfiguration getManagedConfiguration()
    {
        AppConfiguration appConf = new AppConfiguration();

        RestrictionsManager myRestrictionsMgr =
                (RestrictionsManager) this
                        .getSystemService(Context.RESTRICTIONS_SERVICE);

        Bundle appRestrictions = myRestrictionsMgr.getApplicationRestrictions();

        String commonVPNConfiguration = null, userVPNConfiguration = null, allowedApps = null;

        if (appRestrictions.containsKey("CommonVPNConfiguration")) {
            commonVPNConfiguration = appRestrictions.getString("CommonVPNConfiguration");
            System.out.println("CommonVPNConfiguration: " + commonVPNConfiguration);
        }

        if (appRestrictions.containsKey("UserVPNConfiguration")) {
            userVPNConfiguration = appRestrictions.getString("UserVPNConfiguration");
            System.out.println("UserVPNConfiguration: " + userVPNConfiguration);
        }

        if (appRestrictions.containsKey("AllowedApps")) {
            allowedApps = appRestrictions.getString("AllowedApps");
            System.out.println("AllowedApps: " + allowedApps);
        }

        if(commonVPNConfiguration != null && userVPNConfiguration != null && allowedApps != null)
        {
            appConf.setAllowedApps(allowedApps);
            appConf.setCommonConfiguration(commonVPNConfiguration);
            appConf.setUserConfiguration(userVPNConfiguration);
            return appConf;
        } else return null;
    }

    //String profileString64 = "cGVyc2lzdC10dW4KcGVyc2lzdC1rZXkKY2lwaGVyIEFFUy0yNTYtQ0JDCmF1dGggU0hBMQp0bHMtY2xpZW50CmNsaWVudApyZW1vdGUgMTkyLjE2OC4xLjEwMCAxMTk0IHVkcApscG9ydCAwCnZlcmlmeS14NTA5LW5hbWUgIm9wZW52cG4tY2EiIG5hbWUKYXV0aC11c2VyLXBhc3MKbnMtY2VydC10eXBlIHNlcnZlcgoKPGNhPgotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJR2pEQ0NCSFNnQXdJQkFnSUJBREFOQmdrcWhraUc5dzBCQVFzRkFEQ0JpekVMTUFrR0ExVUVCaE1DUWtVeApHREFXQmdOVkJBZ1REMDl2YzNRdFZteGhZVzVrWlhKbGJqRU5NQXNHQTFVRUJ4TUVSMlZ1ZERFU01CQUdBMVVFCkNoTUpTMVVnVEdWMWRtVnVNU293S0FZSktvWklodmNOQVFrQkZodHFZVzR1ZG05emMyRmxjblJBWTNNdWEzVnMKWlhWMlpXNHVZbVV4RXpBUkJnTlZCQU1UQ205d1pXNTJjRzR0WTJFd0hoY05NVFl3T0RFeU1UUXpPVFV3V2hjTgpNall3T0RFd01UUXpPVFV3V2pDQml6RUxNQWtHQTFVRUJoTUNRa1V4R0RBV0JnTlZCQWdURDA5dmMzUXRWbXhoCllXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUVDaE1KUzFVZ1RHVjFkbVZ1TVNvd0tBWUoKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWc1pYVjJaVzR1WW1VeEV6QVJCZ05WQkFNVApDbTl3Wlc1MmNHNHRZMkV3Z2dJaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQ0R3QXdnZ0lLQW9JQ0FRRGladFg4Cm1ZUGdCK0dWcmtUNDNmMkd4MlVHQWRuNmM1UHhpMjlqWEd4Mk40cUNBdkN1UHFKaXJkL0w1dklxVzNqZjdaK1IKMUhLT3VZdVdXUFlBUjZSUVJ1em1kcHNRb3hFcFVQZzlLUHNsZlloTDJ1dnYzbmUwS1NtNUVhb3JKVXF3c2lJKwo1cUF2Sy85QmRLSmhYMlV3SzlRczYzcmVicVdtc0kzbG51UEQ3UW5GMVl3Yjd4eHE1S0dtdU1QTi9KU2FkcEZlCjNjRjFkUk0wSTVTVkovazV4M3VpTlZNZ2cwV2pCV0NVQ1pEdUFLdXMrckk2K1JzZGVmTzA3eW1sbVF3b0pKS0cKaUZxMTVLV2JxczZEakVHaTkwVG9IVCtWeUlnU1JQUHh0bjN1S2NtdUh3ai8ramJmL0p2d3FkbnhVdXhNNEVnSgpIbENKaS9Ddm1WN2dENTlPaEwzK09EU0VvcmxjYmxGQlRRMTJFQzNEVUJVTVI2enVmNnF5eEtpY2VvazkrY3pyCk1KcjQvTFlGaW9FWVhYUGxiZWxDcy85YWRTREl6ZlBGdmMzUWx5ZVRJTXM3VGhvUGJjOCtLOEtmMGRXbDRWNVkKMkd6K3o3eXRJbU5TcTBJK05neVBRY1JxdXRIRWZaUXRmQUgwQWFNVUhFQ1JmZ0o2dGEyMUVWT1J0R2JxakFkbgpyZVdyMm8xa3dWL3cwaHlST0dkdTlySGdaMW5iSE91VXpXN0xMbCsxTGtDNTA2OWhMZklpZnd2WUtla3UralNjCkRpRVlhalpLbWsxNlk0YTlyUTdMN0J2NVhHRUdZbTN5eEtRcjNCblJraDVHWGIyRWl5VlJ0dXpENmpKTW1lVG4KNWVWVm8vRHNNZUhqVUFUdUZBVFZzYm9ZTjk1eWt4MDJjd0lZcHdJREFRQUJvNEg0TUlIMU1CMEdBMVVkRGdRVwpCQlEzV0c3TmtKK1VpSnNZeExPaDl3aklqWW5PNERDQnVBWURWUjBqQklHd01JR3RnQlEzV0c3TmtKK1VpSnNZCnhMT2g5d2pJalluTzRLR0JrYVNCampDQml6RUxNQWtHQTFVRUJoTUNRa1V4R0RBV0JnTlZCQWdURDA5dmMzUXQKVm14aFlXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUVDaE1KUzFVZ1RHVjFkbVZ1TVNvdwpLQVlKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWc1pYVjJaVzR1WW1VeEV6QVJCZ05WCkJBTVRDbTl3Wlc1MmNHNHRZMkdDQVFBd0RBWURWUjBUQkFVd0F3RUIvekFMQmdOVkhROEVCQU1DQVFZd0RRWUoKS29aSWh2Y05BUUVMQlFBRGdnSUJBRU4vTFlIdTc5MjJBdFhJci9kWHBTVFIrSDc5QWkxanQ1bU1hTnI4cDJNUwpUN29ROXZPdlQ3dWU1T3I4Rzh5aC9wcDF0clVHQWVidkNvMXZFcm9rVFYxbjJQSG5xd1ZNaS9kZzRUZisyNlJwCkVnK09qTXBld2FyYlErMEN5UFQzWnVlNG14NTl2MGFUZnBCU05CdFFFSnhaZ3F0dElVdTVXa0VFcHlaNTVyZWcKb2xOaGcyNnV3ZlZ0NTJwbjgvRURTU1FSVzhjYjBIa2x5NXZpdFJJZWdTQXhvTEkzeUFWd2ozNk1PeHBFdFlnZwpIakt1bG0zRHlsdXMxRVVnQW1TU2s2RnpidEZIOFBUbUY4QnNiaW9sV2RUT21qZDhQcFFpaS9ZR2t5SXdhbGhKCnFIUnNaN0MvSEVVeFVhc0hiOU5XcVZ3OVRGZ0tBc2kzM05tNGJvZk9paUdhNUZVT08vMDJEOEltWHNkWHJ4UnUKU2YxeGNUazlBSzNuekgvWlBPWHFCcUlNRWVNT3BqRDlkcXE1NFVMRVRaRXN5TWl4QmJSQmR4ZmlqbGpqNHRsUApGU2ViRSs4RmFHUlpiZ2FFaWJpMFEvUG1GWTNGUTdhNUc3bXgwQzlVbE1veWNyUlJKekpsWENFaTdwYVRXQjh6ClNYNjB6aXZWOGdkUVVidGVGcVpYYTYwcjkwOWVZU25kemdsQlphMFhOZEJNdzZxWGhGVWR6V1FpY1ZHTGF4UWoKMk5Lb3hFVUhsVG9hc3ZiWWd2cFpseElsRDAvR2pidFZWVWxwUVBlalNOSEw2QUdqUGI0M043MVNtQUxPcHorRApUajFQZzBDSXoyZjJ6ZGVHMnlhUGpmQTRjdEpoNVdMc2NDaDVnRGw2bkYvMjhVQkdUbEwyS0xwQlh5WUprZmdsCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KPC9jYT4KPGNlcnQ+Ci0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlGekRDQ0E3U2dBd0lCQWdJQkFqQU5CZ2txaGtpRzl3MEJBUXNGQURDQml6RUxNQWtHQTFVRUJoTUNRa1V4CkdEQVdCZ05WQkFnVEQwOXZjM1F0Vm14aFlXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUUKQ2hNSlMxVWdUR1YxZG1WdU1Tb3dLQVlKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWcwpaWFYyWlc0dVltVXhFekFSQmdOVkJBTVRDbTl3Wlc1MmNHNHRZMkV3SGhjTk1UWXdPREV5TVRRME5qSTVXaGNOCk1qWXdPREV3TVRRME5qSTVXakNCaERFTE1Ba0dBMVVFQmhNQ1FrVXhHREFXQmdOVkJBZ1REMDl2YzNRdFZteGgKWVc1a1pYSmxiakVOTUFzR0ExVUVCeE1FUjJWdWRERVNNQkFHQTFVRUNoTUpTMVVnVEdWMWRtVnVNU293S0FZSgpLb1pJaHZjTkFRa0JGaHRxWVc0dWRtOXpjMkZsY25SQVkzTXVhM1ZzWlhWMlpXNHVZbVV4RERBS0JnTlZCQU1UCkEycGhiakNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFMUUdiS0xja1ZwM3N0WXUKckhTMXNaRmw4REJsUUZoRzhPaU1RTUwyRjVMMlRRZTE2NFlqcG56MmRveWVvdmh3dFgvQVEvM0RmWUU4aUJ6dwpwTCsyRHFFMTgyTm5kNHh0SDdrWEZuUHJuSDcvamJXVGtvM2l2d2plN0I3cnlxMy9uN25UcDVkZHpWMnVOTlhICm5BdDVUYmpIWVo2YUk1S1o2WUJhVFhRWE5SMEJwVlhZNjVqbGIrVHRQVDRxSlJlN094dVdpeGZkMnU2aG8yMkoKM3M2VHVTdTBmUS9uM0U0V01TalFiVzNlWnd2TCtRTWRLZjdEakJSbGU5NVVDdWhDek13VjhPZC9BalBqaWZXagpQaG9zN0xHNWlCemtWMU5rOFR5L1MrVFAzVUFEUko2UmhnclpxalVnQTNVT0FnaUd6THZhZldscU55TldUS3RNCkk0MWxFcGNDQXdFQUFhT0NBVDR3Z2dFNk1Ba0dBMVVkRXdRQ01BQXdDd1lEVlIwUEJBUURBZ1hnTURFR0NXQ0cKU0FHRytFSUJEUVFrRmlKUGNHVnVVMU5NSUVkbGJtVnlZWFJsWkNCVmMyVnlJRU5sY25ScFptbGpZWFJsTUIwRwpBMVVkRGdRV0JCVHJac2thRUhnOWtuUytIU1k4ZHdDN01zRVhJVENCdUFZRFZSMGpCSUd3TUlHdGdCUTNXRzdOCmtKK1VpSnNZeExPaDl3aklqWW5PNEtHQmthU0JqakNCaXpFTE1Ba0dBMVVFQmhNQ1FrVXhHREFXQmdOVkJBZ1QKRDA5dmMzUXRWbXhoWVc1a1pYSmxiakVOTUFzR0ExVUVCeE1FUjJWdWRERVNNQkFHQTFVRUNoTUpTMVVnVEdWMQpkbVZ1TVNvd0tBWUpLb1pJaHZjTkFRa0JGaHRxWVc0dWRtOXpjMkZsY25SQVkzTXVhM1ZzWlhWMlpXNHVZbVV4CkV6QVJCZ05WQkFNVENtOXdaVzUyY0c0dFkyR0NBUUF3RXdZRFZSMGxCQXd3Q2dZSUt3WUJCUVVIQXdJd0RRWUoKS29aSWh2Y05BUUVMQlFBRGdnSUJBS1NiSUJYZk8vaTUzNG9ZOExsYzhpMlNMSjI5d2xRWGFIdUNHVzUwR3JwaQpmMmN4bE1kb3hua0ZaNitLT0x2T25FOGl1V2taUFBzQjdBU0RjdWRRWmViYnhVVC9IUVd1WW5OM21LOHQ3TjdqCmhvYkxQbnQyc1VaVjNmcWovYkNaWmhWcWFzL1F4d1p4N3NIYklrelVSSG93Y1k4cUtjNU5GcE1PTU5pazFvaGoKVndFTnF1N0VLMWdRNW1QdVVuMk92MTIrdDhyUWlZNmhTdjlXV0R5a0FmTFhGdzIya2ZlUkJYZVFwNE5DT0trZwpaZ2piRVgxMDk2WnkvU0VRQWhxY0FuRDVHaFJlWGJDZlE1STd1Rys5SWZUNmJTK2ZpczU0a3dFcFRnR2h2RERICmQ0bVA5ckJLeW9tNUdhaENlSXdYeDhMTmJxaTAwWjF0bHk3TkR3SXdlN24yTkRKSm9WcE5XeWU4aDc4dDlORHAKdXNQNjNxYWF3N3diMlN4QlVwMitqTEh2QWphSmRjYTFIZVZmeGJoWXNhSUJjZk81VnNpYXlRWTNpT0trM3hiVApBYjFzM2JnYURkUDdCb0dRVUg3c1Btak9IYVYwU1FQMGt5Rnc5M3p1c1hEeG5VVWI2RkFPVEM4ZXpkZ1RaME9QCjZCZzdLbFZRcjJvM3ZtcmlCYTZHTjlJNkttdjlUZzFqb0JXVTFpUXhLUVB6Sjh0ZHhhSi9Za1FUT01aaWtXR0YKdWdSQ2RUYU5HekZtTHc3Z2lxaEE0TVhPVFhHdVlIRjYvUUNxZmoyUEdoVFYvVjZoTFl2NElOaEYxS215bnpqVwpTazBqaXJQcytVK2R4cThQWGlEMVVYbjBlYWVKcll6ZGQ0VmV4d1hpMXpMVkpFTzJCMFBiSStNY1ZuRFFYWnBOCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KPC9jZXJ0Pgo8a2V5PgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZBSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS1l3Z2dTaUFnRUFBb0lCQVFDMEJteWkzSkZhZDdMVwpMcXgwdGJHUlpmQXdaVUJZUnZEb2pFREM5aGVTOWswSHRldUdJNlo4OW5hTW5xTDRjTFYvd0VQOXczMkJQSWdjCjhLUy90ZzZoTmZOalozZU1iUis1RnhaejY1eCsvNDIxazVLTjRyOEkzdXdlNjhxdC81KzUwNmVYWGMxZHJqVFYKeDV3TGVVMjR4MkdlbWlPU21lbUFXazEwRnpVZEFhVlYyT3VZNVcvazdUMCtLaVVYdXpzYmxvc1gzZHJ1b2FOdAppZDdPazdrcnRIMFA1OXhPRmpFbzBHMXQzbWNMeS9rREhTbit3NHdVWlh2ZVZBcm9Rc3pNRmZEbmZ3SXo0NG4xCm96NGFMT3l4dVlnYzVGZFRaUEU4djB2a3o5MUFBMFNla1lZSzJhbzFJQU4xRGdJSWhzeTcybjFwYWpjalZreXIKVENPTlpSS1hBZ01CQUFFQ2dnRUFjeEdRS3VGVlAwQTNYVlBrQTQySGZHcHVCbUVScWR0ZWJTWUkxeFU1cUVRcQpwSDBSbUdIOUx1N1NnN3Q2YTlhUERLTTJVbU84T3ZrWC8zZUp0c2lGdldHZ3VxOE42UUp2UG4yVmFtNzFUdS9HCkFvUGJMem41NVkrbjJYUFp6eklQUkZZWFQxY3p4MmRzZWlEbWl5YjBHT0hJY2ZvUU5zcU9SKzV3aDMyMkExMVEKOFRQeEh6THVybDdZa3owRUNDazJ5OU5PR3MxQU5BUUhvVTkvWnlNSWFDblZiczF0MVNIQkMwaHFKNDlPQmM1Wgo3N0ZHaWhqa3dld3RERC9xelBVOFNnUWhxbWE3Ni82Q1BFOXdEOC9WR1BLUnhEcTJZVVFCKzZwRHpOQVQzcnd5CkdsaVlYL0RhanZxbk4wY0RLTHNTSDdneDZzL1poM2xuQTBhM1pNelhjUUtCZ1FEazdqZDdUSnorQ25sOWZ4cUsKVGZsUG1DcWFlYjhBVnhzb0dUcHI4OGRMNmxpNW11RVIrRVBJNHA5YVdxUjZDK3o2YjE2R3NHQk0wcDNWVmVYcwpSbmZCajVld3BjT1prMDV1b0lHZzVwVFJtdlhVeGFZeHU2YkFoYVhXajFIYUJ3L0MvUEJMRzk5QloyU0dyODNLClR3SzNKaGdLQk1tT3JMb2JWUkJXa0NWaHd3S0JnUURKVDlQNkJyWldmYzcvRHV6WkxUMnQxUlJYZm1IMkh4VmcKaEdsQ0dHV3Y5VlZHbk4zWFZpZUpMdG1kSS9aUVE0VXZaOXZKZ1Z2aVNhaGVBNm5obG1udHVUdTUxOEs2WGVmNQpUTldRUUJMdnJwNWhoTUdoOU9tZkV2WlBTbjRra3BwTk1ZNjdzYk1HNmkzTm1TYVZucEJmK251dlRUTFAvcmJNClVGZ1JOOWVLblFLQmdIa0dEa0thait3azhYRU12cVVhNzQvS2E4dGFUVVVLeDRwOU84dFNCcXYxYVk1RmVIS2QKZ29neWRmZTRMM2R0MG92YVVHaDMyWkVEVHZrMi9lUFlwUHFveEpKWUwzMkN5RlhuZUYvdFJnTkJIbkNyUkY3SwpTeTgvK0JuamptdnFpOFNUSHgwMTBPS3NuQkl2NG1UdlBBcENremJJclIxRjV1VUlKU2o5N3lVdEFvR0FRcTJSCkxaQkpscTVIQUk0bC91MFFMK0tTd1VRWUdGaEZVVm1xTDMwN09TUE9ZZndWTW56VHpZbGFCTVZqa1VRNytlNngKaXJXeEJHdVhPOFNFNFRaYjBhUjMxdDlUSFQxN1YwU092QitzNWZ4MWN5RndyVDhjbmxhK2hKYk0zem83cDNQUgpyZk4yczhTZTlTbFQ4Vnk2dnorTTlneGcxeW1yekZLVVNPZXhHYlVDZ1lBN25OWTkvMUFSWGdWWWlNVlExMm9nCjdMSGEzL2Q1aUNZRUgxaVVRMFdrKzFlUEtLc2Rhd1MwWTg5eWVxMXZOVVYvNlhnV2Q5Y2cyNHhyMXNEejIwQlQKQitMMW15c2tyTnNQSWhNKzVpaVVxaXRJbndjc3YzSXVRS3V6bEV1QW5vek5VblN0T29XTDgxK21VYU5PV3lGMgpDWVZqR2tzUmw4L2x5TkJtZUJIa0R3PT0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo8L2tleT4KPHRscy1hdXRoPgojDQojIDIwNDggYml0IE9wZW5WUE4gc3RhdGljIGtleQ0KIw0KLS0tLS1CRUdJTiBPcGVuVlBOIFN0YXRpYyBrZXkgVjEtLS0tLQ0KYTdkMzc1OTliOGY5MjViNjQ4Mzc3MWUyMWFlNTI5YjQNCjk2ZjZmZGJhZGY5YTkyZDg4NGNlOGI3ZTM1YTEyNTUxDQpkNGZjZWNjOTQyNzc3YjYxOTY5NzMzYzlkODE4MTBhMg0KODllZTcxOGM1M2YzMDlhZTA2N2NhZTg5MjMyOWY0MjcNCmRlMGZmYmMyMGFmOTAzNTQ4ODg3YWU5MDhiYzc5OGY0DQo3YzdiOGUwMzk3ZDA4ZGNkMjBjMGI2ODdiNjM3ZjgxNQ0KYjFiNzFkMWU0ZDAzNDE4Mjc5YjRkNjM5NWNiYjBhYmMNCjYzNWE0MWU1ZGEyM2FlN2IxOTIwOGZkZDczOGM3YmVjDQpmZWFkODgyYmI1ZjcwOWUzYzk3NWE2MDJlN2Q4MzNlYQ0KYjRkYjA3ZWY4ZTgxYTdjYjkyMzJiZjAzY2VlNDkxNWMNCmUyMzFlODYxNDViMmRkZWM0ODYzM2E4MWZkNzIzMjQwDQo2Yzc2OTU4YzU4MTQwZDk3MjZiMmQ4ODQ5ZWZmOTY4Mg0KYTdhMmUxNWI3OGE1ZWZlODliODFiY2I2ODFiOTM0MDANCjEyMTc3NGQ4NGJkNTdjY2VhOTVhZDFhOTMxYjQ2Y2RlDQowOTMwMjg4OTE3YzQ2Njc4ODNhOGY0NzNiY2Y3ZGYzOQ0KYzcwMGEwYjY0YTFkZmNlMzUxMWEwMDUxNjI4NDJlOTANCi0tLS0tRU5EIE9wZW5WUE4gU3RhdGljIGtleSBWMS0tLS0tCjwvdGxzLWF1dGg+CiBrZXktZGlyZWN0aW9uIDEK";
    //String profileString64 = "cGVyc2lzdC10dW4KcGVyc2lzdC1rZXkKY2lwaGVyIEFFUy0yNTYtQ0JDCmF1dGggU0hBMQp0bHMtY2xpZW50CmNsaWVudApyZW1vdGUgMTAuMTI5LjE4MC43MSAxMTk0IHVkcApscG9ydCAwCnZlcmlmeS14NTA5LW5hbWUgIm9wZW52cG4tY2EiIG5hbWUKYXV0aC11c2VyLXBhc3MKbnMtY2VydC10eXBlIHNlcnZlcgoKPGNhPgotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJR2pEQ0NCSFNnQXdJQkFnSUJBREFOQmdrcWhraUc5dzBCQVFzRkFEQ0JpekVMTUFrR0ExVUVCaE1DUWtVeApHREFXQmdOVkJBZ1REMDl2YzNRdFZteGhZVzVrWlhKbGJqRU5NQXNHQTFVRUJ4TUVSMlZ1ZERFU01CQUdBMVVFCkNoTUpTMVVnVEdWMWRtVnVNU293S0FZSktvWklodmNOQVFrQkZodHFZVzR1ZG05emMyRmxjblJBWTNNdWEzVnMKWlhWMlpXNHVZbVV4RXpBUkJnTlZCQU1UQ205d1pXNTJjRzR0WTJFd0hoY05NVFl3T0RFeU1UUXpPVFV3V2hjTgpNall3T0RFd01UUXpPVFV3V2pDQml6RUxNQWtHQTFVRUJoTUNRa1V4R0RBV0JnTlZCQWdURDA5dmMzUXRWbXhoCllXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUVDaE1KUzFVZ1RHVjFkbVZ1TVNvd0tBWUoKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWc1pYVjJaVzR1WW1VeEV6QVJCZ05WQkFNVApDbTl3Wlc1MmNHNHRZMkV3Z2dJaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQ0R3QXdnZ0lLQW9JQ0FRRGladFg4Cm1ZUGdCK0dWcmtUNDNmMkd4MlVHQWRuNmM1UHhpMjlqWEd4Mk40cUNBdkN1UHFKaXJkL0w1dklxVzNqZjdaK1IKMUhLT3VZdVdXUFlBUjZSUVJ1em1kcHNRb3hFcFVQZzlLUHNsZlloTDJ1dnYzbmUwS1NtNUVhb3JKVXF3c2lJKwo1cUF2Sy85QmRLSmhYMlV3SzlRczYzcmVicVdtc0kzbG51UEQ3UW5GMVl3Yjd4eHE1S0dtdU1QTi9KU2FkcEZlCjNjRjFkUk0wSTVTVkovazV4M3VpTlZNZ2cwV2pCV0NVQ1pEdUFLdXMrckk2K1JzZGVmTzA3eW1sbVF3b0pKS0cKaUZxMTVLV2JxczZEakVHaTkwVG9IVCtWeUlnU1JQUHh0bjN1S2NtdUh3ai8ramJmL0p2d3FkbnhVdXhNNEVnSgpIbENKaS9Ddm1WN2dENTlPaEwzK09EU0VvcmxjYmxGQlRRMTJFQzNEVUJVTVI2enVmNnF5eEtpY2VvazkrY3pyCk1KcjQvTFlGaW9FWVhYUGxiZWxDcy85YWRTREl6ZlBGdmMzUWx5ZVRJTXM3VGhvUGJjOCtLOEtmMGRXbDRWNVkKMkd6K3o3eXRJbU5TcTBJK05neVBRY1JxdXRIRWZaUXRmQUgwQWFNVUhFQ1JmZ0o2dGEyMUVWT1J0R2JxakFkbgpyZVdyMm8xa3dWL3cwaHlST0dkdTlySGdaMW5iSE91VXpXN0xMbCsxTGtDNTA2OWhMZklpZnd2WUtla3UralNjCkRpRVlhalpLbWsxNlk0YTlyUTdMN0J2NVhHRUdZbTN5eEtRcjNCblJraDVHWGIyRWl5VlJ0dXpENmpKTW1lVG4KNWVWVm8vRHNNZUhqVUFUdUZBVFZzYm9ZTjk1eWt4MDJjd0lZcHdJREFRQUJvNEg0TUlIMU1CMEdBMVVkRGdRVwpCQlEzV0c3TmtKK1VpSnNZeExPaDl3aklqWW5PNERDQnVBWURWUjBqQklHd01JR3RnQlEzV0c3TmtKK1VpSnNZCnhMT2g5d2pJalluTzRLR0JrYVNCampDQml6RUxNQWtHQTFVRUJoTUNRa1V4R0RBV0JnTlZCQWdURDA5dmMzUXQKVm14aFlXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUVDaE1KUzFVZ1RHVjFkbVZ1TVNvdwpLQVlKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWc1pYVjJaVzR1WW1VeEV6QVJCZ05WCkJBTVRDbTl3Wlc1MmNHNHRZMkdDQVFBd0RBWURWUjBUQkFVd0F3RUIvekFMQmdOVkhROEVCQU1DQVFZd0RRWUoKS29aSWh2Y05BUUVMQlFBRGdnSUJBRU4vTFlIdTc5MjJBdFhJci9kWHBTVFIrSDc5QWkxanQ1bU1hTnI4cDJNUwpUN29ROXZPdlQ3dWU1T3I4Rzh5aC9wcDF0clVHQWVidkNvMXZFcm9rVFYxbjJQSG5xd1ZNaS9kZzRUZisyNlJwCkVnK09qTXBld2FyYlErMEN5UFQzWnVlNG14NTl2MGFUZnBCU05CdFFFSnhaZ3F0dElVdTVXa0VFcHlaNTVyZWcKb2xOaGcyNnV3ZlZ0NTJwbjgvRURTU1FSVzhjYjBIa2x5NXZpdFJJZWdTQXhvTEkzeUFWd2ozNk1PeHBFdFlnZwpIakt1bG0zRHlsdXMxRVVnQW1TU2s2RnpidEZIOFBUbUY4QnNiaW9sV2RUT21qZDhQcFFpaS9ZR2t5SXdhbGhKCnFIUnNaN0MvSEVVeFVhc0hiOU5XcVZ3OVRGZ0tBc2kzM05tNGJvZk9paUdhNUZVT08vMDJEOEltWHNkWHJ4UnUKU2YxeGNUazlBSzNuekgvWlBPWHFCcUlNRWVNT3BqRDlkcXE1NFVMRVRaRXN5TWl4QmJSQmR4ZmlqbGpqNHRsUApGU2ViRSs4RmFHUlpiZ2FFaWJpMFEvUG1GWTNGUTdhNUc3bXgwQzlVbE1veWNyUlJKekpsWENFaTdwYVRXQjh6ClNYNjB6aXZWOGdkUVVidGVGcVpYYTYwcjkwOWVZU25kemdsQlphMFhOZEJNdzZxWGhGVWR6V1FpY1ZHTGF4UWoKMk5Lb3hFVUhsVG9hc3ZiWWd2cFpseElsRDAvR2pidFZWVWxwUVBlalNOSEw2QUdqUGI0M043MVNtQUxPcHorRApUajFQZzBDSXoyZjJ6ZGVHMnlhUGpmQTRjdEpoNVdMc2NDaDVnRGw2bkYvMjhVQkdUbEwyS0xwQlh5WUprZmdsCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KPC9jYT4KPGNlcnQ+Ci0tLS0tQkVHSU4gQ0VSVElGSUNBVEUtLS0tLQpNSUlGekRDQ0E3U2dBd0lCQWdJQkFqQU5CZ2txaGtpRzl3MEJBUXNGQURDQml6RUxNQWtHQTFVRUJoTUNRa1V4CkdEQVdCZ05WQkFnVEQwOXZjM1F0Vm14aFlXNWtaWEpsYmpFTk1Bc0dBMVVFQnhNRVIyVnVkREVTTUJBR0ExVUUKQ2hNSlMxVWdUR1YxZG1WdU1Tb3dLQVlKS29aSWh2Y05BUWtCRmh0cVlXNHVkbTl6YzJGbGNuUkFZM011YTNWcwpaWFYyWlc0dVltVXhFekFSQmdOVkJBTVRDbTl3Wlc1MmNHNHRZMkV3SGhjTk1UWXdPREV5TVRRME5qSTVXaGNOCk1qWXdPREV3TVRRME5qSTVXakNCaERFTE1Ba0dBMVVFQmhNQ1FrVXhHREFXQmdOVkJBZ1REMDl2YzNRdFZteGgKWVc1a1pYSmxiakVOTUFzR0ExVUVCeE1FUjJWdWRERVNNQkFHQTFVRUNoTUpTMVVnVEdWMWRtVnVNU293S0FZSgpLb1pJaHZjTkFRa0JGaHRxWVc0dWRtOXpjMkZsY25SQVkzTXVhM1ZzWlhWMlpXNHVZbVV4RERBS0JnTlZCQU1UCkEycGhiakNDQVNJd0RRWUpLb1pJaHZjTkFRRUJCUUFEZ2dFUEFEQ0NBUW9DZ2dFQkFMUUdiS0xja1ZwM3N0WXUKckhTMXNaRmw4REJsUUZoRzhPaU1RTUwyRjVMMlRRZTE2NFlqcG56MmRveWVvdmh3dFgvQVEvM0RmWUU4aUJ6dwpwTCsyRHFFMTgyTm5kNHh0SDdrWEZuUHJuSDcvamJXVGtvM2l2d2plN0I3cnlxMy9uN25UcDVkZHpWMnVOTlhICm5BdDVUYmpIWVo2YUk1S1o2WUJhVFhRWE5SMEJwVlhZNjVqbGIrVHRQVDRxSlJlN094dVdpeGZkMnU2aG8yMkoKM3M2VHVTdTBmUS9uM0U0V01TalFiVzNlWnd2TCtRTWRLZjdEakJSbGU5NVVDdWhDek13VjhPZC9BalBqaWZXagpQaG9zN0xHNWlCemtWMU5rOFR5L1MrVFAzVUFEUko2UmhnclpxalVnQTNVT0FnaUd6THZhZldscU55TldUS3RNCkk0MWxFcGNDQXdFQUFhT0NBVDR3Z2dFNk1Ba0dBMVVkRXdRQ01BQXdDd1lEVlIwUEJBUURBZ1hnTURFR0NXQ0cKU0FHRytFSUJEUVFrRmlKUGNHVnVVMU5NSUVkbGJtVnlZWFJsWkNCVmMyVnlJRU5sY25ScFptbGpZWFJsTUIwRwpBMVVkRGdRV0JCVHJac2thRUhnOWtuUytIU1k4ZHdDN01zRVhJVENCdUFZRFZSMGpCSUd3TUlHdGdCUTNXRzdOCmtKK1VpSnNZeExPaDl3aklqWW5PNEtHQmthU0JqakNCaXpFTE1Ba0dBMVVFQmhNQ1FrVXhHREFXQmdOVkJBZ1QKRDA5dmMzUXRWbXhoWVc1a1pYSmxiakVOTUFzR0ExVUVCeE1FUjJWdWRERVNNQkFHQTFVRUNoTUpTMVVnVEdWMQpkbVZ1TVNvd0tBWUpLb1pJaHZjTkFRa0JGaHRxWVc0dWRtOXpjMkZsY25SQVkzTXVhM1ZzWlhWMlpXNHVZbVV4CkV6QVJCZ05WQkFNVENtOXdaVzUyY0c0dFkyR0NBUUF3RXdZRFZSMGxCQXd3Q2dZSUt3WUJCUVVIQXdJd0RRWUoKS29aSWh2Y05BUUVMQlFBRGdnSUJBS1NiSUJYZk8vaTUzNG9ZOExsYzhpMlNMSjI5d2xRWGFIdUNHVzUwR3JwaQpmMmN4bE1kb3hua0ZaNitLT0x2T25FOGl1V2taUFBzQjdBU0RjdWRRWmViYnhVVC9IUVd1WW5OM21LOHQ3TjdqCmhvYkxQbnQyc1VaVjNmcWovYkNaWmhWcWFzL1F4d1p4N3NIYklrelVSSG93Y1k4cUtjNU5GcE1PTU5pazFvaGoKVndFTnF1N0VLMWdRNW1QdVVuMk92MTIrdDhyUWlZNmhTdjlXV0R5a0FmTFhGdzIya2ZlUkJYZVFwNE5DT0trZwpaZ2piRVgxMDk2WnkvU0VRQWhxY0FuRDVHaFJlWGJDZlE1STd1Rys5SWZUNmJTK2ZpczU0a3dFcFRnR2h2RERICmQ0bVA5ckJLeW9tNUdhaENlSXdYeDhMTmJxaTAwWjF0bHk3TkR3SXdlN24yTkRKSm9WcE5XeWU4aDc4dDlORHAKdXNQNjNxYWF3N3diMlN4QlVwMitqTEh2QWphSmRjYTFIZVZmeGJoWXNhSUJjZk81VnNpYXlRWTNpT0trM3hiVApBYjFzM2JnYURkUDdCb0dRVUg3c1Btak9IYVYwU1FQMGt5Rnc5M3p1c1hEeG5VVWI2RkFPVEM4ZXpkZ1RaME9QCjZCZzdLbFZRcjJvM3ZtcmlCYTZHTjlJNkttdjlUZzFqb0JXVTFpUXhLUVB6Sjh0ZHhhSi9Za1FUT01aaWtXR0YKdWdSQ2RUYU5HekZtTHc3Z2lxaEE0TVhPVFhHdVlIRjYvUUNxZmoyUEdoVFYvVjZoTFl2NElOaEYxS215bnpqVwpTazBqaXJQcytVK2R4cThQWGlEMVVYbjBlYWVKcll6ZGQ0VmV4d1hpMXpMVkpFTzJCMFBiSStNY1ZuRFFYWnBOCi0tLS0tRU5EIENFUlRJRklDQVRFLS0tLS0KPC9jZXJ0Pgo8a2V5PgotLS0tLUJFR0lOIFBSSVZBVEUgS0VZLS0tLS0KTUlJRXZBSUJBREFOQmdrcWhraUc5dzBCQVFFRkFBU0NCS1l3Z2dTaUFnRUFBb0lCQVFDMEJteWkzSkZhZDdMVwpMcXgwdGJHUlpmQXdaVUJZUnZEb2pFREM5aGVTOWswSHRldUdJNlo4OW5hTW5xTDRjTFYvd0VQOXczMkJQSWdjCjhLUy90ZzZoTmZOalozZU1iUis1RnhaejY1eCsvNDIxazVLTjRyOEkzdXdlNjhxdC81KzUwNmVYWGMxZHJqVFYKeDV3TGVVMjR4MkdlbWlPU21lbUFXazEwRnpVZEFhVlYyT3VZNVcvazdUMCtLaVVYdXpzYmxvc1gzZHJ1b2FOdAppZDdPazdrcnRIMFA1OXhPRmpFbzBHMXQzbWNMeS9rREhTbit3NHdVWlh2ZVZBcm9Rc3pNRmZEbmZ3SXo0NG4xCm96NGFMT3l4dVlnYzVGZFRaUEU4djB2a3o5MUFBMFNla1lZSzJhbzFJQU4xRGdJSWhzeTcybjFwYWpjalZreXIKVENPTlpSS1hBZ01CQUFFQ2dnRUFjeEdRS3VGVlAwQTNYVlBrQTQySGZHcHVCbUVScWR0ZWJTWUkxeFU1cUVRcQpwSDBSbUdIOUx1N1NnN3Q2YTlhUERLTTJVbU84T3ZrWC8zZUp0c2lGdldHZ3VxOE42UUp2UG4yVmFtNzFUdS9HCkFvUGJMem41NVkrbjJYUFp6eklQUkZZWFQxY3p4MmRzZWlEbWl5YjBHT0hJY2ZvUU5zcU9SKzV3aDMyMkExMVEKOFRQeEh6THVybDdZa3owRUNDazJ5OU5PR3MxQU5BUUhvVTkvWnlNSWFDblZiczF0MVNIQkMwaHFKNDlPQmM1Wgo3N0ZHaWhqa3dld3RERC9xelBVOFNnUWhxbWE3Ni82Q1BFOXdEOC9WR1BLUnhEcTJZVVFCKzZwRHpOQVQzcnd5CkdsaVlYL0RhanZxbk4wY0RLTHNTSDdneDZzL1poM2xuQTBhM1pNelhjUUtCZ1FEazdqZDdUSnorQ25sOWZ4cUsKVGZsUG1DcWFlYjhBVnhzb0dUcHI4OGRMNmxpNW11RVIrRVBJNHA5YVdxUjZDK3o2YjE2R3NHQk0wcDNWVmVYcwpSbmZCajVld3BjT1prMDV1b0lHZzVwVFJtdlhVeGFZeHU2YkFoYVhXajFIYUJ3L0MvUEJMRzk5QloyU0dyODNLClR3SzNKaGdLQk1tT3JMb2JWUkJXa0NWaHd3S0JnUURKVDlQNkJyWldmYzcvRHV6WkxUMnQxUlJYZm1IMkh4VmcKaEdsQ0dHV3Y5VlZHbk4zWFZpZUpMdG1kSS9aUVE0VXZaOXZKZ1Z2aVNhaGVBNm5obG1udHVUdTUxOEs2WGVmNQpUTldRUUJMdnJwNWhoTUdoOU9tZkV2WlBTbjRra3BwTk1ZNjdzYk1HNmkzTm1TYVZucEJmK251dlRUTFAvcmJNClVGZ1JOOWVLblFLQmdIa0dEa0thait3azhYRU12cVVhNzQvS2E4dGFUVVVLeDRwOU84dFNCcXYxYVk1RmVIS2QKZ29neWRmZTRMM2R0MG92YVVHaDMyWkVEVHZrMi9lUFlwUHFveEpKWUwzMkN5RlhuZUYvdFJnTkJIbkNyUkY3SwpTeTgvK0JuamptdnFpOFNUSHgwMTBPS3NuQkl2NG1UdlBBcENremJJclIxRjV1VUlKU2o5N3lVdEFvR0FRcTJSCkxaQkpscTVIQUk0bC91MFFMK0tTd1VRWUdGaEZVVm1xTDMwN09TUE9ZZndWTW56VHpZbGFCTVZqa1VRNytlNngKaXJXeEJHdVhPOFNFNFRaYjBhUjMxdDlUSFQxN1YwU092QitzNWZ4MWN5RndyVDhjbmxhK2hKYk0zem83cDNQUgpyZk4yczhTZTlTbFQ4Vnk2dnorTTlneGcxeW1yekZLVVNPZXhHYlVDZ1lBN25OWTkvMUFSWGdWWWlNVlExMm9nCjdMSGEzL2Q1aUNZRUgxaVVRMFdrKzFlUEtLc2Rhd1MwWTg5eWVxMXZOVVYvNlhnV2Q5Y2cyNHhyMXNEejIwQlQKQitMMW15c2tyTnNQSWhNKzVpaVVxaXRJbndjc3YzSXVRS3V6bEV1QW5vek5VblN0T29XTDgxK21VYU5PV3lGMgpDWVZqR2tzUmw4L2x5TkJtZUJIa0R3PT0KLS0tLS1FTkQgUFJJVkFURSBLRVktLS0tLQo8L2tleT4KPHRscy1hdXRoPgojCiMgMjA0OCBiaXQgT3BlblZQTiBzdGF0aWMga2V5CiMKLS0tLS1CRUdJTiBPcGVuVlBOIFN0YXRpYyBrZXkgVjEtLS0tLQphN2QzNzU5OWI4ZjkyNWI2NDgzNzcxZTIxYWU1MjliNAo5NmY2ZmRiYWRmOWE5MmQ4ODRjZThiN2UzNWExMjU1MQpkNGZjZWNjOTQyNzc3YjYxOTY5NzMzYzlkODE4MTBhMgo4OWVlNzE4YzUzZjMwOWFlMDY3Y2FlODkyMzI5ZjQyNwpkZTBmZmJjMjBhZjkwMzU0ODg4N2FlOTA4YmM3OThmNAo3YzdiOGUwMzk3ZDA4ZGNkMjBjMGI2ODdiNjM3ZjgxNQpiMWI3MWQxZTRkMDM0MTgyNzliNGQ2Mzk1Y2JiMGFiYwo2MzVhNDFlNWRhMjNhZTdiMTkyMDhmZGQ3MzhjN2JlYwpmZWFkODgyYmI1ZjcwOWUzYzk3NWE2MDJlN2Q4MzNlYQpiNGRiMDdlZjhlODFhN2NiOTIzMmJmMDNjZWU0OTE1YwplMjMxZTg2MTQ1YjJkZGVjNDg2MzNhODFmZDcyMzI0MAo2Yzc2OTU4YzU4MTQwZDk3MjZiMmQ4ODQ5ZWZmOTY4MgphN2EyZTE1Yjc4YTVlZmU4OWI4MWJjYjY4MWI5MzQwMAoxMjE3NzRkODRiZDU3Y2NlYTk1YWQxYTkzMWI0NmNkZQowOTMwMjg4OTE3YzQ2Njc4ODNhOGY0NzNiY2Y3ZGYzOQpjNzAwYTBiNjRhMWRmY2UzNTExYTAwNTE2Mjg0MmU5MAotLS0tLUVORCBPcGVuVlBOIFN0YXRpYyBrZXkgVjEtLS0tLQo8L3Rscy1hdXRoPgoga2V5LWRpcmVjdGlvbiAxCg==";
    //String profileString = new String(Base64.decode(profileString64, Base64.DEFAULT));
    //Reader reader = new StringReader(profileString);

//            VpnProfile profile = pm.getProfileByName("afw_vpn");
//
//            if(profile != null ) {
//                profile.mAllowedAppsVpnAreDisallowed = false;
//                profile.mAllowedAppsVpn.add("ua.com.streamsoft.pingtools");
//                profile.writeConfigFile(this);
//            }else{
//                System.out.println("Profile doesn't exists, cant update settings");
//
//            }


    public void onResume()
    {
        super.onResume();

        try {

            AppConfiguration appConf = getManagedConfiguration();

            if(appConf != null) {
                String commonConfStr = new String(Base64.decode(appConf.getCommonConfiguration(), Base64.DEFAULT));
                String userConfStr = new String(Base64.decode(appConf.getUserConfiguration(), Base64.DEFAULT));

                Reader reader = new StringReader((commonConfStr + userConfStr));

                ProfileManager pm = ProfileManager.getInstance(this);

                if (pm.getProfileByName("afw_vpn") == null) {

                    ConfigParser confParser = new ConfigParser();
                    confParser.parseConfig(reader);
                    VpnProfile profile = confParser.convertProfile();
                    //profile.mUsername = "jan";
                    //profile.mPassword = "jan";
                    profile.mName = "afw_vpn";

                    pm.addProfile(profile);

                    profile.mAllowedAppsVpnAreDisallowed = false;
                    String allowedApps = appConf.getAllowedApps();
                    String[] allowedAppsArray = allowedApps.split(",");

                for(String allowedApp : allowedAppsArray)
                    profile.mAllowedAppsVpn.add(allowedApp);
                profile.writeConfigFile(this);

                } else {
                    System.out.println("Profile already exists, not creating new");
                }

            }else{
                System.out.println("EMPTY CONF");
            }




        }catch(Exception e){
            System.out.println("Exception managed profile: " + e);
        }
    }

    private void editPreferences()
    {
        try {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            //Set<String> s = new HashSet<String>();
            //s.add(profile.getName());
            //editor.putString("alwaysOnVpn", "afw_vpn");
            // editor.putStringSet("alwaysOnVpn", s);
            editor.putBoolean("restartvpnonboot", true);
            editor.apply();
        }catch(Exception e){
            System.out.println("Exception managed profile, edit prefs: " + e);
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    private void requestDozeDisable() {
        Intent intent = new Intent();
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm.isIgnoringBatteryOptimizations(packageName))
            intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        else {
            intent.setAction(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + packageName));
        }
        startActivity(intent);
    }

    private static final String FEATURE_TELEVISION = "android.hardware.type.television";
    private static final String FEATURE_LEANBACK = "android.software.leanback";

    private boolean isDirectToTV() {
        return(getPackageManager().hasSystemFeature(FEATURE_TELEVISION)
                || getPackageManager().hasSystemFeature(FEATURE_LEANBACK));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void disableToolbarElevation() {
        ActionBar toolbar = getActionBar();
        toolbar.setElevation(0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.show_log){
            Intent showLog = new Intent(this, LogWindow.class);
            startActivity(showLog);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		System.out.println(data);


	}


}
