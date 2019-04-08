package com.example.eliezer.onlineshop;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by verdotte on 3/19/2018.
 */

public class SessionsPager extends FragmentPagerAdapter {
    public SessionsPager(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                HomeFragment home = new HomeFragment();
                return home;
            case 1:
                ShopFragment shopFragment = new ShopFragment();
                return shopFragment;
            case 2:
                AboutUsFragment aboutUsFragment = new AboutUsFragment();
                return aboutUsFragment;

                default:

                    return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position){
            case 0:
                return "HOME";
            case 1:
                return "SHOPS";
            case 2:
                return "ABOUT US";
            default:
                return null;
        }
    }

}
