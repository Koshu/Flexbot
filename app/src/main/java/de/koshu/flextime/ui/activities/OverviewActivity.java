package de.koshu.flextime.ui.activities;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import de.koshu.flextime.R;
import de.koshu.flextime.data.DataManager;
import de.koshu.flextime.data.Day;
import de.koshu.flextime.data.Month;
import de.koshu.flextime.data.Year;
import de.koshu.flextime.ui.fragments.DayFragment;
import de.koshu.flextime.ui.fragments.MonthFragment;
import de.koshu.flextime.ui.fragments.YearFragment;
import de.koshu.flextime.ui.fragments.ZoomOutPageTransformer;
import io.realm.Realm;
import io.realm.RealmResults;


public class OverviewActivity extends AppCompatActivity {
    private Realm realm;
    private ViewPager2 viewPager;
    private FragmentStateAdapter pagerAdapter;
    private Menu menu;
    private int currentPageType = 0;

    private final RealmResults<Year> years = DataManager.getManager().getAllYearsOrdered();
    private final RealmResults<Month> months = DataManager.getManager().getAllMonthsOrdered();
    private final RealmResults<Day> days = DataManager.getManager().getAllDaysOrdered();

    public static final int PAGETYPE_YEAR = 0;
    public static final int PAGETYPE_MONTH = 1;
    public static final int PAGETYPE_DAY = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_year);

        realm = DataManager.getManager().getRealm();

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Jahr");

        viewPager = findViewById(R.id.pager);
        pagerAdapter = new YearSlidePagerAdapter(this);
        viewPager.setPageTransformer(new ZoomOutPageTransformer());
        viewPager.setOffscreenPageLimit(3);

        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(viewPager.getChildCount()-1);
        Bundle bundle = getIntent().getExtras();

        if(bundle != null) {
            int type = bundle.getInt("pageType",PAGETYPE_YEAR);
            int yearInt = bundle.getInt("yearInt",-1);
            int monthInt = bundle.getInt("monthInt",-1);
            int dayInt = bundle.getInt("dayInt",-1);

            switchPageAdapter(type,yearInt,monthInt,dayInt);
        } else {
            switchPageAdapter(PAGETYPE_YEAR,-1,-1,-1);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dateviewmenu, menu);
        this.menu = menu;
        updateMenuIconAccentColors();
        return true;
    }

    public void updateMenuIconAccentColors(){
        if(menu == null) return;

        int normal = ContextCompat.getColor(this,R.color.colorPrimary);
        int accent = ContextCompat.getColor(this,R.color.colorAccent);

        Drawable yearIcon = menu.findItem(R.id.action_yearview).getIcon().mutate();

        if(currentPageType == PAGETYPE_YEAR){
            yearIcon.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        } else {
            yearIcon.setColorFilter(normal, PorterDuff.Mode.SRC_IN);
        }

        Drawable monthIcon = menu.findItem(R.id.action_monthview).getIcon().mutate();

        if(currentPageType == PAGETYPE_MONTH){
            monthIcon.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        } else {
            monthIcon.setColorFilter(normal, PorterDuff.Mode.SRC_IN);
        }

        Drawable dayIcon = menu.findItem(R.id.action_dayview).getIcon().mutate();

        if(currentPageType == PAGETYPE_DAY){
            dayIcon.setColorFilter(accent, PorterDuff.Mode.SRC_IN);
        } else {
            dayIcon.setColorFilter(normal, PorterDuff.Mode.SRC_IN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_dayview) {
            switchPageAdapter(PAGETYPE_DAY,-1,-1,-1);
            return true;
        }

        if (id == R.id.action_monthview) {
            switchPageAdapter(PAGETYPE_MONTH,-1,-1,-1);
            return true;
        }

        if(id == R.id.action_yearview) {
            switchPageAdapter(PAGETYPE_YEAR,-1,-1,-1);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void switchPageAdapter(int type, int yearInt, int monthInt, int dayInt){
        switch (type){
            case PAGETYPE_YEAR: {
                getSupportActionBar().setTitle("Jahre");
                currentPageType = type;
                updateMenuIconAccentColors();

                pagerAdapter = new YearSlidePagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);

                if(yearInt == -1){
                    viewPager.setCurrentItem(years.size()-1,false);
                } else {
                    Year searched = years.where()
                            .equalTo("yearInt", yearInt)
                            .findFirst();

                    int idx = years.indexOf(searched);

                    viewPager.setCurrentItem(idx,false);
                }
            } break;
            case PAGETYPE_MONTH: {
                getSupportActionBar().setTitle("Monate");
                currentPageType = type;
                updateMenuIconAccentColors();

                pagerAdapter = new MonthSlidePagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);

                if(yearInt == -1){
                    viewPager.setCurrentItem(months.size()-1,false);
                } else {
                    Month searched = months.where()
                            .equalTo("yearInt", yearInt)
                            .equalTo("monthInt", monthInt)
                            .findFirst();

                    int idx = months.indexOf(searched);

                    viewPager.setCurrentItem(idx,false);
                }
            } break;
            case PAGETYPE_DAY: {
                getSupportActionBar().setTitle("Tage");
                currentPageType = type;
                updateMenuIconAccentColors();

                pagerAdapter = new DaySlidePagerAdapter(this);
                viewPager.setAdapter(pagerAdapter);

                if(yearInt == -1){
                    viewPager.setCurrentItem(days.size()-1,false);
                } else {
                    Day searched = days.where()
                            .equalTo("yearInt", yearInt)
                            .equalTo("monthInt", monthInt)
                            .equalTo("dayInt", dayInt)
                            .findFirst();

                    int idx = days.indexOf(searched);

                    viewPager.setCurrentItem(idx,false);
                }
            } break;
        }
    }

    public Realm getRealm(){
        return realm;
    }

    private class YearSlidePagerAdapter extends FragmentStateAdapter {
        public YearSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            YearFragment fragment = new YearFragment();
            Bundle args = new Bundle();
            args.putInt("yearInt", years.get(position).getYearInt());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return years.size();
        }
    }

    private class MonthSlidePagerAdapter extends FragmentStateAdapter {
        public MonthSlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            MonthFragment fragment = new MonthFragment();
            Bundle args = new Bundle();
            args.putInt("yearInt", months.get(position).getYearInt());
            args.putInt("monthInt", months.get(position).getMonthInt());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return months.size();
        }
    }

    private class DaySlidePagerAdapter extends FragmentStateAdapter {
        public DaySlidePagerAdapter(FragmentActivity fa) {
            super(fa);
        }

        @Override
        public Fragment createFragment(int position) {
            DayFragment fragment = new DayFragment();
            Bundle args = new Bundle();
            args.putInt("monthInt", days.get(position).getMonthInt());
            args.putInt("yearInt", days.get(position).getYearInt());
            args.putInt("dayInt", days.get(position).getDayInt());
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return days.size();
        }
    }
}